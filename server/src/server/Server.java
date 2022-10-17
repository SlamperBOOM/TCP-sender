package server;

import server.threads.ConnectionThread;

import java.io.IOException;
import java.net.Socket;

public class Server {
    private final ConnectionThread connectionThread;
    private final Clients clients;
    private final SpeedLogger logger;

    public Server(String port) throws IOException {
        connectionThread = new ConnectionThread(this, Integer.parseInt(port));
        connectionThread.start();
        clients = new Clients();
        System.out.println("Server started at port " + port);
        logger = new SpeedLogger(this);
        logger.start();
    }

    public void connectClient(Socket clientSocket){
        clients.addClient(this, clientSocket);
    }
    public void disconnectClient(int ID){
        clients.removeClient(ID);
    }

    public Clients getClients(){
        return clients;
    }

    public void stop(){
        connectionThread.setStopped();
        logger.setStopped();
        clients.stop();
    }
}
