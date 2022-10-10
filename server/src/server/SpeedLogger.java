package server;

import java.util.List;

public class SpeedLogger extends Thread{
    private Boolean isRunning = true;
    private Server server;

    public SpeedLogger(Server server){
        this.server = server;
    }

    @Override
    public void run(){
        while(isRunning){
            try{
                sleep(3000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if(server.getClients().getSize() > 0){
                List<ClientInfo> clients = server.getClients().getClients();
                System.out.println();
                System.out.println("Stats:");
                synchronized (clients) {
                    for (ClientInfo client : clients) {
                        System.out.println("--------------------------------------------------");
                        System.out.println("Client #" + client.getID()
                                + ":  Speed in moment: " + client.getMomentSpeed() + " Kb/s,  Average Speed: "
                                + client.getAverageSpeed() + " Kb/s,  " + client.getPercentage() + "% finished");
                    }
                    System.out.println("--------------------------------------------------");
                    clients.notifyAll();
                }
            }
        }
    }

    public void setStopped(){
        isRunning = false;
    }
}
