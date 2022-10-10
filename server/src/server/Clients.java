package server;

import server.threads.ClientThread;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Clients {
    private List<ClientInfo> clients;
    private int nextID = 0;

    public Clients(){
        clients = new ArrayList<>();
    }

    public void addClient(Server server, Socket clientSocket){
        synchronized (clients) {
            clients.add(new ClientInfo(nextID, new ClientThread(clientSocket, server, nextID)));
            clients.notifyAll();
        }
        nextID++;
    }

    public void removeClient(int ID){
        synchronized (clients) {
            for (ClientInfo client : clients) {
                if(client.getID() == ID){
                    clients.remove(client);
                    break;
                }
            }
            clients.notifyAll();
        }
    }

    public int getSize(){
        return clients.size();
    }

    public List<ClientInfo> getClients(){
        return clients;
    }

    public void stop(){
        for(ClientInfo client : clients){
            client.stop();
        }
    }
}
