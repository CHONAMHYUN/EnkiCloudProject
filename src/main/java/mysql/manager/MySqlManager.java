package mysql.manager;

import com.github.dockerjava.api.model.Container;

import database.DatabaseManagerParent;
import docker.Utility.DockerUtility;
import docker.Utility.InstDockerClient;
import docker.component.DBContainer;
import docker.component.DBNode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utility.CommonUtility;
import utility.Pair;
import utility.SV;

/**
 * Created by nhcho on 2015-04-15.
 */
public class MySqlManager extends DatabaseManagerParent{
	public static MySqlManager mySqlManager = null;
	
    public MySqlManager() {
		super();
	}
	
    public static DatabaseManagerParent getInstance() {
    	if(mySqlManager == null) mySqlManager = new MySqlManager();
    	return mySqlManager;
    }
	
    @Override
    public void initConnection() {
        try {
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
}
