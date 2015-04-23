package utility;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import docker.component.DBContainer;
import docker.component.DBNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nhcho on 2015-04-15.
 */
public class SV {
    public static List<DBNode> NodeList = new ArrayList<DBNode>();
    public static List<DBContainer> ContainerDBList = new ArrayList<DBContainer>();

    // ip, Docker Container list
    public static Map<String, List<Container>> ContainerRunList = new HashMap<String, List<Container>>();

    // ip, List of observing for Docker Event
    public static Map<String, DockerClient> ContainerEventList = new HashMap<String, DockerClient>();

    // reload flag
    public static boolean setReloadFlag = false;

    public static final String NodeListQuery = "select * from node";
    public static final String ContainerDBQuery =
            "select A.* from container_status A, node N " +
                    "where A.ip = N.ip and N.daemonStatus = 'run' ";
    public static final String NodeDaemonUpdateQuery =
            "update node set nodeStatus = ?, daemonStatus = ?, modifyDate = now()  where ip = ?";

    public static final String NewContainerInsert =
            "insert into container_status " +
            "(ip, imageName, containerId, seq, curStatus, cmdStatus, modifyDate, useContainer) " +
            "values (?,?,?,?,?,?, now(), 'Y') ";

    public static final String ContainerChangeQuery =
            "update container_status set curStatus = ?, modifyDate = now() where ip = ? and containerId = ? ";

    public static final String ContainerImageChangeQuery =
            "update container_status set imageName = ?, modifyDate = now() where ip = ? and containerId = ? ";

    public static final String ContainerUse =
            "update container_status set useContainer = ?, modifyDate = now(), curStatus = ? " +
                    "where ip = ? and containerId = ? ";
}