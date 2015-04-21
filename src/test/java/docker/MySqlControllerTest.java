package docker;

import docker.controller.ListBuilder;
import docker.haservice.HAServer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Handler;

/**
 * Created by nhcho on 2015-04-15.
 */
public class MySqlControllerTest {
    public static void main(String[] args) {
        ListBuilder listBuilder = new ListBuilder();
        listBuilder.MakeNodeList();

        HAServer haServer = new HAServer();
        haServer.ServerSideThreadRunning();
    }
}
