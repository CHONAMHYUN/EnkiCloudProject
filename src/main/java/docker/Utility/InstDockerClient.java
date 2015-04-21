package docker.Utility;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl;

/**
 * Created by nhcho on 2015-04-15.
 */
public class InstDockerClient {
    public DockerClient GetDockerClient(String url, String port) {
        DockerClient dockerClient = null;

        DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder()
                .withUri(String.format("http://%s:%s", url, port))
                .build();
        dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(new DockerCmdExecFactoryImpl())
                .build();

        return dockerClient;
    }

    public static boolean PingDockerDaemon(String ip, String port) {
        DockerClient dockerClient = (new InstDockerClient()).GetDockerClient(ip, port);
        try {
            dockerClient.pingCmd().exec();
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }
}
