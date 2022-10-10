package server.threads;

import server.Server;

import java.io.IOException;
import java.net.*;

public class ConnectionThread extends Thread{
    ServerSocket socket;
    Boolean isRunning = true;
    Server server;

    public ConnectionThread(Server server, int port){
        this.server = server;
        try {
            socket = new ServerSocket(port);
            socket.setSoTimeout(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        Socket clientSocket;
        while (isRunning){
            clientSocket = null;
            try {
                clientSocket = socket.accept();
            } catch (IOException e) {
                //getting here when accept timeout
            }
            if(clientSocket != null){
                server.connectClient(clientSocket);
            }
        }
    }

    public void setStopped(){
        isRunning = false;
    }
}
