package server;

import server.threads.ConnectionThread;

import java.net.Socket;

public class Server {
    private ConnectionThread connectionThread;
    private Clients clients;
    private SpeedLogger logger;

    public Server(String port){
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
