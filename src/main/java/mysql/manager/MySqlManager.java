package mysql.manager;

import com.github.dockerjava.api.model.Container;

import docker.Utility.DockerUtility;
import docker.Utility.InstDockerClient;
import docker.component.DBContainer;
import docker.component.DBNode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import utility.CommonUtility;
import utility.Pair;
import utility.SV;

/**
 * Created by nhcho on 2015-04-15.
 */
public class MySqlManager {
    private static List<DBNode> NodeList = SV.NodeList;
    private static List<DBContainer> ContainerDBList = SV.ContainerDBList;
    private static List<Pair<String,List<Container>>> ContainerRunList = SV.ContainerRunList;

    private static Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;
    private PreparedStatement preparedStatement = null;

    private static MySqlManager mySqlManager = null;

    private enum NODESTATUS  {allrun, alldie, onlydaemonrun};

    public MySqlManager() {
        intConnection();
    }

    private void intConnection() {
        try {
            /*
            Class.forName(className);
            String connString = url + "?user=" + userId + "&password=" + password;
            connection = DriverManager.getConnection(url);
            */

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://192.168.10.155/oce?user=root&password=1111");
        }
        catch (SQLException e) {
            System.out.println("Connection fail : "  + e.toString());
        }
        catch (ClassNotFoundException e1) {
            System.out.println("Class Not Found : " + e1.toString());
        }
    }

    public static MySqlManager getInstance() {
        if(mySqlManager == null) mySqlManager = new MySqlManager();
        return mySqlManager;
    }


    public void MakeNodeList() {
        List<Pair<String,NODESTATUS >> nodeStatus = new ArrayList<Pair<String, NODESTATUS>>();
        try {
            NodeList.clear();

            statement = connection.createStatement();
            resultSet = statement.executeQuery(SV.NodeListQuery);
            while(resultSet.next()) {
                DBNode dbNode = new DBNode();
                String ip = resultSet.getString("ip");
                String port = resultSet.getString("port");
                String usable = resultSet.getString("useNode");

                dbNode.setIp(ip);
                dbNode.setNodeName(resultSet.getString("nodeName"));
                dbNode.setLastNodeStatus(resultSet.getString("nodeStatus"));
                dbNode.setLastDaemonStatus(resultSet.getString("daemonStatus"));
                dbNode.setCommandStatus(resultSet.getString("cmdStatus"));
                dbNode.setDaemonPort(port);
                dbNode.setUseNode(usable);

                // useless node is not checked
                if(usable != null && usable.equals("N")) {
                    continue;
                }
                // if alive node
                else if(CommonUtility.isReachable(ip)) {
                    // is docker daemon alive?
                    if(InstDockerClient.PingDockerDaemon(ip, port)) {
                        Pair<String,NODESTATUS> pair = new Pair<String, NODESTATUS>(ip, NODESTATUS.allrun);
                        nodeStatus.add(pair);
                    }
                    // node is alive but docker daemon die
                    else {
                        Pair<String,NODESTATUS> pair = new Pair<String, NODESTATUS>(ip, NODESTATUS.onlydaemonrun);
                        nodeStatus.add(pair);
                        continue;
                    }
                }
                else {
                    // useNode "Y", but node die and docker daemon die too
                    Pair<String,NODESTATUS> pair = new Pair<String, NODESTATUS>(ip, NODESTATUS.alldie);
                    nodeStatus.add(pair);
                    continue;
                }
                NodeList.add(dbNode);
            }
            resultSet.close();
            statement.close();
        }
        catch (SQLException e) {
            System.out.println("MakeNodeList fail :" + e.toString());
            TryToReconnectMySQL();
            return;
        }
        for(Pair<String,NODESTATUS> pair : nodeStatus) {
            if(pair.getR() == NODESTATUS.allrun) {
                UpdateNodeDaemon(pair.getL(), "run", "run");
            }
            else if(pair.getR() == NODESTATUS.onlydaemonrun) {
                UpdateNodeDaemon(pair.getL(), "run", "die");
            }
            else if(pair.getR() == NODESTATUS.alldie) {
                UpdateNodeDaemon(pair.getL(), "die", "die");
            }
        }

    }

    private void UpdateNodeDaemon(String ip, String node, String daemon) {
      try {
            preparedStatement = connection.prepareStatement(SV.NodeDaemonUpdateQuery);
            preparedStatement.setString(1, node);
            preparedStatement.setString(2, daemon);
            preparedStatement.setString(3, ip);

            preparedStatement.execute();
            preparedStatement.close();
            //connection.commit();
        }
        catch(SQLException e) {
            System.out.println("UpdateNodeDaemon SQLException :" + e.toString());
            TryToReconnectMySQL();
        }
    }

    public void MakeContainerDBList() {
        try {
            ContainerDBList.clear();

            statement = connection.createStatement();
            resultSet = statement.executeQuery(SV.ContainerDBQuery);

            while (resultSet.next()) {
                String ip =  resultSet.getString("ip");
                DBContainer dbCont = new DBContainer();
                dbCont.setIp(resultSet.getString("ip"));
                dbCont.setImageName(resultSet.getString("imageName"));
                dbCont.setContainerId(resultSet.getString("containerId"));
                dbCont.setSeq(resultSet.getString("seq"));
                dbCont.setCurrentStatus(resultSet.getString("curStatus"));
                dbCont.setCommandStatus(resultSet.getString("cmdStatus"));
                dbCont.setUseContainer(resultSet.getString("useContainer"));

                ContainerDBList.add(dbCont);
            }
            resultSet.close();
            statement.close();
        }
        catch (SQLException e) {
            System.out.println("MakeContainerDBList fail :" + e.toString());
            TryToReconnectMySQL();
        }
    }

    public void InsertContainer(String ip, Container container) {
        String seq = "1";
        String status = DockerUtility.GetStatus(container.getStatus());
        String id = container.getId();
        String imageName = container.getImage();

        try {
            preparedStatement = connection.prepareStatement(SV.NewContainerInsert);
            preparedStatement.setString(1, ip);
            preparedStatement.setString(2, imageName);
            preparedStatement.setString(3, id);
            preparedStatement.setString(4, seq);
            preparedStatement.setString(5, status);
            preparedStatement.setString(6, status);

            preparedStatement.execute();
            preparedStatement.close();
            //connection.commit();
        }
        catch (SQLException e) {
            System.out.println("InsertContainer ERROR" + e.toString());
            TryToReconnectMySQL();
        }
    }

    public void UpdateContainerStatus(String ip, String id, String status) {
        try {
            preparedStatement = connection.prepareStatement(SV.ContainerChangeQuery);
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, ip);
            preparedStatement.setString(3, id);

            preparedStatement.execute();
            preparedStatement.close();
            //connection.commit();
        }
        catch(SQLException e) {
            System.out.println("UpdateContainerUse ERROR" + e.toString());
            TryToReconnectMySQL();
        }
    }

    public void UpdateContainerStatus(String ip, Container container) {
        String id = container.getId();
        String status = DockerUtility.GetStatus(container.getStatus());

        UpdateContainerStatus(ip, id, status);
    }

    public void UpdateContainerImageName(String ip, Container container) {
        String id = container.getId();
        String image = container.getImage();

        try {
            preparedStatement = connection.prepareStatement(SV.ContainerImageChangeQuery);
            preparedStatement.setString(1, image);
            preparedStatement.setString(2, ip);
            preparedStatement.setString(3, id);

            preparedStatement.execute();
            preparedStatement.close();
            //connection.commit();
        }
        catch(SQLException e) {
            System.out.println("UpdateContainerUse ERROR" + e.toString());
            TryToReconnectMySQL();
        }
    }

    public void UpdateContainerUse(String ip, String id, String use, String status) {
        try {
            preparedStatement = connection.prepareStatement(SV.ContainerUse);
            preparedStatement.setString(1, use);
            preparedStatement.setString(2, ip);
            preparedStatement.setString(3, id);
            preparedStatement.setString(4, status);

            preparedStatement.execute();
            preparedStatement.close();
            //connection.commit();
        }
        catch(SQLException e) {
            System.out.println("UpdateContainerUse ERROR" + e.toString());
            TryToReconnectMySQL();
        }
    }

    public void UpdateDatabase(String ip, String status) {
        for(DBContainer dbContainer : ContainerDBList) {
            if(ip.equals(dbContainer.getIp())) {
                UpdateContainerStatus(ip, dbContainer.getContainerId(), status);
            }
        }
    }

    private void TryToReconnectMySQL() {
        while(true) {
            try {
                if (connection.isClosed()) intConnection();
            } catch (SQLException e) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e1) {

                }
                continue;
            }
            //System.out.println("reconnect mysql");
            break;
        }
    }
}
