package server;

import server.threads.ClientThread;

public class ClientInfo {
    private final int ID;
    private final ClientThread thread;

    public ClientInfo(int ID, ClientThread thread){
        this.ID = ID;
        this.thread = thread;
        thread.start();
    }

    public Double getMomentSpeed(){
        return thread.getMomentSpeed();
    }

    public Double getAverageSpeed(){
        return thread.getAverageSpeed();
    }

    public Double getPercentage(){
        return thread.getPercentage();
    }

    public int getID(){
        return ID;
    }

    public void stop(){
        thread.setStopped();
    }
}
