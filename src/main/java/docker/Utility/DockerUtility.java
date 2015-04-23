package docker.Utility;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import utility.SV;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import docker.component.DBContainer;
import docker.component.DBNode;

public class DockerUtility {
	
    private static List<DBNode> NodeList = SV.NodeList;
    private static List<DBContainer> ContainerDBList = SV.ContainerDBList;
    private static Map<String, List<Container>> ContainerRunList = SV.ContainerRunList;
    private static Map<String, DockerClient> ContainerEventList = SV.ContainerEventList;
    
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
    
    public static void ClearContainerEventList() {
        Thread t = new Thread(new Runnable() {
            public void run() {
            	for(DBNode dbNode : NodeList) {
            		String ip = dbNode.getIp();
            		DockerClient dockerClient = ContainerEventList.get(ip);
                    try {
                        dockerClient.close();
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
    	for(DBNode dbNode : NodeList) {
    		String ip = dbNode.getIp();
    		ContainerRunList.get(ip).clear();
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
        String ip = dbc.getIp();
        String containerId = dbc.getContainerId();

        List<Container> containers = ContainerRunList.get(ip);
        for(Container container : containers) {
        	if(containerId.equals(container.getId())) {
        		return container;
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
