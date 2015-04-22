package controller.haserver;

import docker.controller.ListBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import utility.SV;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by nhcho on 2015-04-15.
 */
@Component
public class HAServer {
    @Value("${ha.ip}")
    private String ip;
    @Value("${ha.serverPort}")
    private String serverPort;
    @Value("${ha.destPort}")
    private String destPort; // 테스트 용도

    public static HAServer haServer = null;

    public HAServer() {
        ip = "192.168.10.155";
        serverPort = "5014";
        destPort = "5014";
    }

    public static HAServer getInstance() {
        if(haServer == null) {
            haServer = new HAServer();
        }
        return haServer;
    }

    public void ServerSideThreadRunning() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                int port = Integer.parseInt(serverPort);
                try {
                    ServerSocket welcomeSocket = new ServerSocket(port);
                    while (true) {
                        Socket connectionSocket = welcomeSocket.accept();
                        BufferedReader inFromClient =
                                new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                        String clientSentence = inFromClient.readLine();
                        //System.out.println("FROM CLIENT :" + clientSentence);
                        SocketAddress remoteIp = connectionSocket.getRemoteSocketAddress();
                        System.out.print("From " + remoteIp.toString() + " : ");
                        
                        String[] recvMsg = clientSentence.split(" ");
                        String header = recvMsg[0];
                        if (header != null) {
                            if (header.equals("reload")) {
                                // DB & Running Container list restart
                                outToClient.writeBytes("OK\n");
                                SV.setReloadFlag = true;
                                ListBuilder.ReloadList();
                            } else if (header.equals("stop")) {
                                System.out.println("SERVER : stop signal from self client");
                                outToClient.writeBytes("I get a stop signal from client\n");
                                connectionSocket.close();
                                welcomeSocket.close();
                                break;
                            }
                            else if(header.equals("resource")){
                                System.out.println(clientSentence);
                                //String[] realIp = remoteIp.toString().split("/");
                                //if(realIp[1] != null) {
                                //    String[] pureIp = realIp[1].split(":");
                                //    System.out.println(pureIp[0]);
                               // }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("HAServer::ServerSideThreadRunning::" + e);
                }
            }
        });
        t.start();
    }
}
