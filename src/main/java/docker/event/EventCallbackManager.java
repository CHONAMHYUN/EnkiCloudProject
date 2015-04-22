package docker.event;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.EventCallback;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Event;

import docker.Utility.DockerUtility;
import docker.Utility.InstDockerClient;
import docker.component.DBContainer;
import docker.component.DBNode;
import docker.controller.ListBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utility.CommonUtility;
import utility.Pair;
import utility.SV;
import mysql.manager.MySqlManager;

/**
 * Created by nhcho on 2015-04-14.
 */
public class EventCallbackManager {
    private static List<DBNode> NodeList = SV.NodeList;
    private static List<DBContainer> ContainerDBList = SV.ContainerDBList;
    private static List<Pair<String, List<Container>>> ContainerRunList = SV.ContainerRunList;
    private static List<Pair<String,DockerClient>> ContainerEventList = SV.ContainerEventList;
    private static MySqlManager mySqlManager = MySqlManager.getInstance();

    public EventCallback SetCallback(final String ip) {
        EventCallback eventCallback = new EventCallback() {
            public void onEvent(Event event) {
                System.out.println("IP=" + ip);
                System.out.print("Status=" + event.getStatus());
                System.out.print(",Id=" + event.getId());
                System.out.print(",From=" + event.getFrom());
                System.out.println(",Time=" + event.getTime());
                if(event.getStatus().equals("stop")) {
                    mySqlManager.UpdateDatabase(ip, "die");
                    UpdateContainerDBList(ip, "die");
                }
                else if(event.getStatus().equals("start")) {
                    mySqlManager.UpdateDatabase(ip, "run");
                    UpdateContainerDBList(ip, "run");
                }
                else if(event.getStatus().equals("create")) {
                }
                else if(event.getStatus().equals("destroy")) {
                    mySqlManager.UpdateContainerUse(ip, event.getId(), "N", "die");
                }
            }

            public void onException(Throwable throwable) {
                System.out.println("onException");
            }

            public void onCompletion(int numEvents) {
                // disconnect from docker daemon
                System.out.println("onCompletion ip = " + ip);
                if(!SV.setReloadFlag) {
                    final int numEvent = numEvents;
                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            mySqlManager.UpdateDatabase(ip, "die");

                            UpdateContainerDBList(ip, "die");
                            ClearContainerEventIP(ip);
                            ClearContainerRunIP(ip);

                            TryToReConnect(ip);
                        }
                    });
                    t.start();
                }
            }

            public boolean isReceiving() {
                System.out.println("isReceiving");
                return true;
            }
        };

        return eventCallback;
    }

    public void UpdateContainerDBList(String ip, String status) {
        List<DBContainer> updateList = new ArrayList<DBContainer>();

        for(DBContainer dbContainer : ContainerDBList) {
            if(ip.equals(dbContainer.getIp())) {
                updateList.add(dbContainer);
            }
        }

        for(DBContainer dbContainer : updateList) {
            ContainerDBList.remove(dbContainer);
            dbContainer.setCurrentStatus(status);
            ContainerDBList.add(dbContainer);
        }
    }

    public void ClearContainerEventIP(String ip) {
        List<Pair<String, DockerClient>> updateList = new ArrayList<Pair<String, DockerClient>>();
        for(Pair<String, DockerClient> pair : ContainerEventList) {
            if(pair.getL().equals(ip)) {
                updateList.add(pair);
                break;
            }
        }

        for(Pair<String, DockerClient> client : updateList) {
            ContainerEventList.remove(client);
        }

    }

    public void ClearContainerRunIP(String ip) {
        List<Pair<String, List<Container>>> updateList = new ArrayList<Pair<String, List<Container>>>();
        for(Pair<String, List<Container>> pair : ContainerRunList) {
            if(pair.getL().equals(ip)) {
                updateList.add(pair);
                break;
            }
        }

        for(Pair<String, List<Container>> pair : updateList) {
            ContainerRunList.remove(pair);
        }
    }

    public static  void MakeContainerEventList() {
        for(DBNode dbNode : NodeList) {
            String ip = dbNode.getIp();
            String port = dbNode.getDaemonPort();

            SetContainerEvent(ip, port);
        }
    }

    public static void SetContainerEvent(String ip, String port) {
        EventCallbackManager eventCallbackManager = new EventCallbackManager();
        Date date = new Date();
        long currentDate = date.getTime();
        currentDate = currentDate / 1000;
        String sinceTime = String.format("%d", currentDate);

        DockerClient dockerClient = (new InstDockerClient()).GetDockerClient(ip, port);
        dockerClient.eventsCmd(eventCallbackManager.SetCallback(ip)).withSince(sinceTime).exec();

        Pair<String, DockerClient> dockerClientList = new Pair<String, DockerClient>(ip, dockerClient);

        ContainerEventList.add(dockerClientList);
    }

    public void TryToReConnect(String ip) {
        String port = DockerUtility.GetPort(ip);

        while(!CommonUtility.isReachable(ip)) {
            try {
                System.out.println("STILL NODE DIE:" + ip);
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                System.out.println("TryToReConnect Ping Mode sleep Exception");
                return;
            }
        }

        System.out.println("TryToReConnect NODE ALIVE !!!! :" + ip);
        // node가 살아 있다.
        while(!InstDockerClient.PingDockerDaemon(ip, port)) {
            try {
                System.out.println("STILL DOCKER DAEMON DIE :" + ip);
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                System.out.println("TryToReConnect Ping Docker sleep Exception");
                return;
            }
        }
        // Docker Daemon이 살아났다.Container Run list에 다시 등록
        List<Container> containers = ListBuilder.GetContainerRun(ip, port);
        Pair<String, List<Container>> cont = new Pair<String, List<Container>>(ip, containers);
        ContainerRunList.add(cont);

        // DB 업데이트
        UpdateContainerDBList(ip, "run");
        // Event Callback 등록
        SetContainerEvent(ip, port);
    }
}
