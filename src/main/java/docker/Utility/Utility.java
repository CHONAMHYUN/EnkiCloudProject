package docker.Utility;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import docker.component.DBContainer;
import docker.component.DBNode;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by nhcho on 2015-04-15.
 */
public class Utility {
    private static List<DBNode> NodeList = SV.NodeList;
    private static List<DBContainer> ContainerDBList = SV.ContainerDBList;
    private static List<Pair<String,List<Container>>> ContainerRunList = SV.ContainerRunList;
    private static List<Pair<String,DockerClient>> ContainerEventList = SV.ContainerEventList;

    public static String GetStatus(String status) {
        String current = "die";

        String[] statusArr = status.split(" ");

        if(statusArr[0] != null && statusArr[0].equals("Up")) {
            current = "run";
        }
        else if(statusArr[0] != null && statusArr[0].equals("Exited")) {
            current = "die";
        }

        return current;
    }

    // Node ping
    public static boolean isReachable(String ip)
    {
        InetAddress id = null;
        try {
            InetAddress[] addresses = InetAddress.getAllByName(ip);
            for (InetAddress address : addresses) {
                if (address.isReachable(5000)) return true;
                else return false;
            }
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }

    public static void ClearContainerEventList() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                for(Pair<String, DockerClient> dockerClientPair : ContainerEventList) {
                    try {
                        dockerClientPair.getR().close();
                    }
                    catch (IOException e) {
                        System.out.println("close docker's client event listener ERROR:" + e.toString());
                    }
                }
                ContainerEventList.clear();
            }
        });
        t.start();
    }

    public static void ClearContainerRunList() {
        for(Pair<String,List<Container>> containerRunListPair : ContainerRunList) {
            containerRunListPair.getR().clear();
        }
        ContainerRunList.clear();
    }


    public static DBContainer findDBContainer(String ip, String containerId) {
        for(DBContainer db: ContainerDBList) {
            if(db.getIp().equals(ip) && db.getContainerId().equals(containerId)) {
                return db;
            }
        }
        return null;
    }

    public static Container findRunContainer(DBContainer dbc) {
        Container container = null;
        String ip = dbc.getIp();
        String containerId = dbc.getContainerId();

        for(Pair<String,List<Container>> container1 : ContainerRunList) {
            if(ip.equals(container1.getL())) {
                for(Container container2 : container1.getR()) {
                    if(container2.getId().equals(containerId)) {
                        return container2;
                    }
                }
            }
        }
        return null;
    }

    public static String GetPort(String ip) {
        String port = null;

        for(DBNode listPair : NodeList) {
            if(ip.equals(listPair.getIp())) {
                return listPair.getDaemonPort();
            }
        }

        return port;
    }
}
