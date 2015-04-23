package docker.listbuild;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import docker.Utility.DockerUtility;
import docker.Utility.InstDockerClient;
import docker.component.DBContainer;
import docker.component.DBNode;
import docker.event.EventCallbackManager;

import javax.ws.rs.ProcessingException;

import utility.SV;
import mysql.manager.MySqlManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nhcho on 2015-04-15.
 */
public class ListBuilder {
    private static List<DBNode> NodeList = SV.NodeList;
    private static List<DBContainer> ContainerDBList = SV.ContainerDBList;
    private static Map<String,List<Container>> ContainerRunList = SV.ContainerRunList;
    private static Map<String,DockerClient> ContainerEventList = SV.ContainerEventList;
    private MySqlManager mySqlManager = MySqlManager.getInstance();
    private static ListBuilder listBuilder = null;

    public ListBuilder() {
        this.listBuilder = this;
    }

    public static ListBuilder getInstance() {
        if(listBuilder == null) listBuilder = new ListBuilder();
        return listBuilder;
    }

    public void MakeNodeList() {
        ClearAll();

        mySqlManager.MakeNodeList();
        mySqlManager.MakeContainerDBList();

        MakeContainerRunList();
        SyncContainerLists();

        EventCallbackManager.MakeContainerEventList();

        SV.setReloadFlag = false;
    }

    public void ClearAll() {
        DockerUtility.ClearContainerEventList();
        DockerUtility.ClearContainerRunList();
        ContainerDBList.clear();
        NodeList.clear();
    }

    public static void ReloadList() {
        listBuilder.MakeNodeList();
    }
    
    public void MakeContainerRunList() {
    	DockerUtility.ClearContainerRunList();

        String ip = "";
        String port = "";

        for(DBNode dbNode : NodeList) {
            ip = dbNode.getIp();
            port = dbNode.getDaemonPort();

            List<Container> containers = GetContainerRun(ip, port);
            if(containers == null) continue;

            ContainerRunList.put(ip, containers);

            System.out.println("NODE IP = " + ip + " / Container info");
            for(Container co : containers) {
                System.out.println("\t\tid = " + co.getId());
                System.out.println("\t\tstatus = " + co.getStatus());
                System.out.println("\t\timage = " + co.getImage());
                System.out.println("\t\tcommand = " + co.getCommand());
                System.out.println("\t\tnames");
                for (int i = 0; i < co.getNames().length; i++) {
                    System.out.println("\t\t\t\tname["+(i+1) + "] = " + co.getNames()[i]);
                }
                System.out.println("\t\tports");
                for(Container.Port po : co.getPorts()) {
                    System.out.println("\t\t\t\tip = " + po.getIp());
                    System.out.println("\t\t\t\ttype = " + po.getType());
                    System.out.println("\t\t\t\tprivatePort = " + po.getPrivatePort());
                    System.out.println("\t\t\t\tPublicPort = " + po.getPublicPort());
                }
            }
            System.out.println("============================================");
        }
    }

    public static List<Container> GetContainerRun(String ip, String port) {
        List<Container> containers = null;
        DockerClient dockerClient = (new InstDockerClient()).GetDockerClient(ip, port);
        try {
            containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        }
        catch (ProcessingException e) {
            System.out.println(e);
            return null;
        }
        return containers;
    }

    public void SyncContainerLists() {
        boolean reloadContainerListFlag = false;
        boolean reloadContainerDBListFlag = false;

        // diff run container list with database
        for( DBNode dbNode : NodeList ) {
            String ip = dbNode.getIp();
            List<Container> containers = ContainerRunList.get(ip);
            
            for(Container container : containers) {
                // not find running Container in Database
                DBContainer findContainer = DockerUtility.findDBContainer(ip, container.getId());
                if(findContainer == null) {
                    // NOT FOUND DB Container
                    // Insert Row into CONTAINER_STATUS
                    DBListAdd(ip, container);
                    mySqlManager.InsertContainer(ip, container);

                }
                else {
                    // compare db data and update, list update
                    // status info is different each other, update DB
                    String status = DockerUtility.GetStatus(container.getStatus());
                    if(!findContainer.getCurrentStatus().equals(status)) {
                        DBListUpdate(ip, container);
                        mySqlManager.UpdateContainerStatus(ip, container);
                    }

                    if(findContainer.getImageName() == null || findContainer.getImageName().trim().length() == 0) {
                        DBListUpdate(ip, container);
                        mySqlManager.UpdateContainerImageName(ip, container);
                    }
                }
            }
        }

        List<DBContainer> removeDBContainer = new ArrayList<DBContainer>();
        for(DBContainer dbContainer : ContainerDBList) {
            Container findContainer = DockerUtility.findRunContainer(dbContainer);

            // DB에는 있는데 실제(running container list)는 없다
            if(findContainer == null) {
                // Deleted Container so it will be update useContainer field to "N" and remove DBContainer in ContainerDBList
                // 사실 N로 할건지 새로 띄울건지는 정해 지지 않음. DB에 있는것을 기준으로 띄울 것이냐? DB를 삭제 할 것이냐?
                removeDBContainer.add(dbContainer);
            }
        }

        for(DBContainer dbContainer : removeDBContainer) {
            ContainerDBList.remove(dbContainer);
            mySqlManager.UpdateContainerUse(dbContainer.getIp(), dbContainer.getContainerId(), "N", "die");
        }

    }

    public void DBListAdd(String ip, Container container) {
        String seq = "1";
        String status = DockerUtility.GetStatus(container.getStatus());
        String id = container.getId();
        String imageName = container.getImage();

        DBContainer dbCont = new DBContainer();

        dbCont.setIp(ip);
        dbCont.setImageName(imageName);
        dbCont.setContainerId(id);
        dbCont.setSeq(seq);
        dbCont.setCurrentStatus(status);
        dbCont.setCommandStatus(status);
        dbCont.setUseContainer("Y");

        ContainerDBList.add(dbCont);
    }

    public void DBListUpdate(String ip, Container container) {
        for(DBContainer dbContainer : ContainerDBList) {
            if(dbContainer.getIp().equals(ip) && dbContainer.getContainerId().equals(container.getId())) {
                ContainerDBList.remove(dbContainer);
                DBListAdd(ip, container);
                break;
            }
        }
    }
}
