package database;

import com.github.dockerjava.api.model.Container;

public interface CloudDatabaseManager {
    abstract void intConnection();
    abstract void MakeNodeList();
    abstract void UpdateNodeDaemon(String ip, String node, String daemon);
    abstract void MakeContainerDBList();
    abstract void InsertContainer(String ip, Container container);
    abstract void UpdateContainerStatus(String ip, String id, String status);
    abstract void UpdateContainerStatus(String ip, Container container);
    abstract void UpdateContainerImageName(String ip, Container container);
    abstract void UpdateContainerUse(String ip, String id, String use, String status);
    abstract void UpdateDatabase(String ip, String status);
    abstract void TryToReconnectDatabase();
}