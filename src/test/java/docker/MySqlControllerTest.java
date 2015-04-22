package docker;

import docker.controller.ListBuilder;
import docker.haservice.HAServer;

/**
 * Created by nhcho on 2015-04-15.
 */
public class MySqlControllerTest {
    public static void main(String[] args) {
        ListBuilder.getInstance().MakeNodeList();
        HAServer.getInstance().ServerSideThreadRunning();
    }
}
