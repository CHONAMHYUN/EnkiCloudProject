package docker.component;

/**
 * Created by nhcho on 2015-04-14.
 */
public class DBContainer {
    private String ip;
    private String imageName;
    private String containerId;
    private String seq;
    private String currentStatus;
    private String commandStatus;
    private String useContainer;

    public void setIp(String ip) { this.ip = ip; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public void setContainerId(String containerId) { this.containerId = containerId; }
    public void setSeq(String seq) { this.seq = seq; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public void setCommandStatus(String commandStatus) { this.commandStatus = commandStatus; }
    public void setUseContainer(String useContainer) { this.useContainer = useContainer; }

    public String getIp() { return ip; }
    public String getImageName() { return imageName; }
    public String getContainerId() { return containerId; }
    public String getSeq() { return seq; }
    public String getCurrentStatus() { return currentStatus; }
    public String getCommandStatus() { return commandStatus; }
    public String getUseContainer() { return useContainer; }
}
