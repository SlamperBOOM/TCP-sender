package server.threads;

import server.Server;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ClientThread extends Thread{
    private final int ID;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final Socket clientSocket;
    private final Server server;
    private Boolean isRunning = true;
    private Double momentSpeed = 0.0;
    private Double averageSpeed = 0.0;
    private Long bytesCount = 0L;
    private long size;
    private Boolean isAsked = false;
    private File file;

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
            file = new File("uploads\\" + filename);
            file.createNewFile();
            fileOutput = new FileOutputStream(file);
        } catch (IOException e) {
            sendExitMessage(1);
            System.out.println("Client #" + ID +": Error occurred while initiating receiving");
        }
        Date startDate = new Date();
        bytesCount = 0L;
        int momentSpeedTicks = 0;


        MessageDigest hashSum = null;
        int tryCount = 0;
        while(hashSum == null) {
            try {
                hashSum = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            tryCount++;
            if(tryCount == 100){
                System.out.println("Client # " + ID + ": Cannot calc hash sum");
                return;
            }
        }
        Date momentStart = new Date();
        while(isRunning){
            if(fileOutput == null){
                break;
            }
            try{
                byte[] buffer = new byte[4];
                inputStream.read(buffer);
                int bufferSize = ByteBuffer.wrap(buffer).getInt();
                buffer = new byte[bufferSize];
                int readBytes = inputStream.read(buffer);
                fileOutput.write(buffer);
                hashSum.update(buffer);
                bytesCount += readBytes;
                if(momentSpeedTicks == 50) {
                    Date momentEnd = new Date();
                    synchronized (momentSpeed) {
                        momentSpeed = bufferSize*50 / ((momentEnd.getTime() - momentStart.getTime()) / 1000.0) / 1024.0;
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
                System.out.println("Client #" + ID +": Error occurred while reading file");
                break;
            }
            if(bytesCount >= size){
                break;
            }
        }
        if(fileOutput != null){
            try{
                fileOutput.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        if(isRunning){//indicate that program finished by itself
            if(fileOutput == null){
                System.out.println("Client #" + ID +": Error occurred while writing file");
                sendExitMessage(1);
            }else {
                byte[] finalHashSum = null;
                try {
                    byte[] lengthBytes = new byte[Integer.BYTES];
                    inputStream.read(lengthBytes);
                    finalHashSum = new byte[ByteBuffer.wrap(lengthBytes).getInt()];
                    inputStream.read(finalHashSum);
                } catch (IOException e) {
                }

                byte[] hashSumLocal = hashSum.digest();
                if (Arrays.equals(hashSumLocal, finalHashSum)) {
                    System.out.println("Client #" + ID + ": finished transmission");
                    sendExitMessage(0);
                } else {
                    System.out.println("Client #" + ID + ": file was modified while transmission. Deleting");
                    file.delete();
                    sendExitMessage(1);
                }
            }
        }else{//server stopped
            sendExitMessage(1);
        }

        try{
            clientSocket.close();
            inputStream.close();
            outputStream.close();
        }catch (IOException e){
        }
        while (true){
            synchronized (isAsked){//in case transmission was too fast. it keeps this thread alive until SpeedLogger read the speeds
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
