package docker.component;

/**
 * Created by nhcho on 2015-04-14.
 */
public class DBNode {
    private String ip;
    private String nodeName;
    private String lastNodeStatus;
    private String lastDaemonStatus;
    private String commandStatus;
    private String daemonPort;
    private String useNode;

    public String getIp() {return ip;}
    public String getNodeName() {return nodeName;}
    public String getLastNodeStatus() {return lastNodeStatus;}
    public String getLastDaemonStatus() {return lastDaemonStatus;}
    public String getCommandStatus() {return  commandStatus;}
    public String getDaemonPort() {return daemonPort;}
    public String getUseNode() {return useNode;}

    public void setIp(String ip) {this.ip = ip;}
    public void setNodeName(String nodeName) {this.nodeName = nodeName;}
    public void setLastNodeStatus(String lastNodeStatus) {this.lastNodeStatus = lastNodeStatus;}
    public void setLastDaemonStatus(String lastDaemonStatus) { this.lastDaemonStatus = lastDaemonStatus;}
    public void setCommandStatus(String commandStatus) { this.commandStatus = commandStatus;}
    public void setDaemonPort(String daemonPort) {this.daemonPort = daemonPort;}
    public void setUseNode(String useNode) {this.useNode = useNode;}
}
