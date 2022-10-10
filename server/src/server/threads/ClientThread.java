package server.threads;

import server.Server;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientThread extends Thread{
    private int ID;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket clientSocket;
    private Server server;
    private Boolean isRunning = true;
    private Double momentSpeed = 0.0;
    private Double averageSpeed = 0.0;
    private Long bytesCount = 0L;
    private long size;
    private Boolean isAsked = false;

    public ClientThread(Socket socket, Server server, int ID){
        this.ID = ID;
        this.server = server;
        clientSocket = socket;
        try{
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
        }catch (IOException e){
        }
    }

    @Override
    public void run(){
        OutputStream fileOutput = null;
        String filename;
        int bufferSize = 8192;
        try {
            byte[] sizeBytes = new byte[Integer.BYTES];
            inputStream.read(sizeBytes);
            int nameLength = ByteBuffer.wrap(sizeBytes).getInt();
            byte[] nameBytes = new byte[nameLength];
            inputStream.read(nameBytes);
            filename = new String(nameBytes, StandardCharsets.UTF_8);
            sizeBytes = new byte[Long.BYTES];
            inputStream.read(sizeBytes);
            size = ByteBuffer.wrap(sizeBytes).getLong();
            File dir = new File("uploads");
            dir.mkdir();
            File file = new File("uploads\\" + filename);
            file.createNewFile();
            fileOutput = new FileOutputStream(file);
        } catch (IOException e) {
            sendExitMessage(1);
            System.out.println("Client #" + ID +" Error occurred while initiating receiving");
        }
        Date startDate = new Date();
        bytesCount = 0L;
        int momentSpeedTicks = 0;
        Date momentStart = new Date();
        while(isRunning){
            if(fileOutput == null){
                break;
            }
            try{
                byte[] buffer = new byte[bufferSize];
                int readBytes = inputStream.read(buffer);
                if(readBytes < bufferSize){

                }
                fileOutput.write(buffer);
                bytesCount += readBytes;
                if(momentSpeedTicks == 100) {
                    Date momentEnd = new Date();
                    synchronized (momentSpeed) {
                        momentSpeed = bufferSize*100 / ((momentEnd.getTime() - momentStart.getTime()) / 1000.0) / 1024.0;
                    }
                    momentSpeedTicks = 0;
                    synchronized (averageSpeed){
                        synchronized (bytesCount) {
                            averageSpeed = bytesCount / ((momentEnd.getTime() - startDate.getTime()) / 1000.0) / 1024.0;
                        }
                    }
                    momentStart = new Date();
                }
                momentSpeedTicks++;
            }catch (IOException e){
                System.out.println("Client #" + ID +" Error occurred while reading file");
                break;
            }
            if(bytesCount >= size){
                break;
            }
        }
        if(isRunning){
            sendExitMessage(0);
        }else{
            sendExitMessage(1);
        }
        if(fileOutput != null){
            try{
                fileOutput.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        try{
            clientSocket.close();
            inputStream.close();
            outputStream.close();
        }catch (IOException e){

        }
        System.out.println("Client #" + ID + " finished transmission");
        while (true){
            synchronized (isAsked){
                if(isAsked){
                    break;
                }
            }
            try{
                sleep(100);
            }catch (InterruptedException e){

            }
        }
        server.disconnectClient(ID);
    }

    public void sendExitMessage(int message){
        try {
            outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(message).array());
        }catch (IOException e){
        }
    }

    public Double getMomentSpeed(){
        Double time;
        synchronized (momentSpeed){
            time = momentSpeed;
        }

        return time;
    }

    public Double getAverageSpeed(){
        Double time;
        synchronized (averageSpeed){
            time = averageSpeed;
        }
        return time;
    }

    public Double getPercentage(){
        double percent;
        synchronized (bytesCount){
            percent = bytesCount * 1.0 / size * 100.0;
        }
        synchronized (isAsked){
            isAsked = true;
        }
        return percent;
    }

    public void setStopped(){
        isRunning = false;
    }
}
