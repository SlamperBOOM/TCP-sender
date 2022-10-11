package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientThread extends Thread{
    private OutputStream outputStream;
    private FileInputStream fileStream;
    private File file;
    private boolean isRunning = true;
    private int bufferSize = 8192;

    public ClientThread(OutputStream outputStream, File file) {
        this.outputStream = outputStream;
        this.file = file;
        try {
            fileStream = new FileInputStream(file);
        }catch (IOException e){
        }
    }

    @Override
    public void run(){
        //showing info
        String filename = file.getName();
        long length = file.length();
        System.out.println("Filename: " + filename);
        String shortFileLength = "";
        if(length >= Math.pow(2, 10) && length < (Math.pow(2, 20))){
            shortFileLength =" (" + length / Math.pow(2, 10) + " Kb)";
        }else if(length >= Math.pow(2, 20) && length < Math.pow(2, 30)){
            shortFileLength =" (" + length / Math.pow(2, 20) + " Mb)";
        }else if(length >= Math.pow(2, 30) && length < Math.pow(2, 40)){
            shortFileLength =" (" + length / Math.pow(2, 30) + " Gb)";
        }
        System.out.print("Length of file: " + length + " bytes");
        System.out.println(shortFileLength);
        //sending file info
        try{
            outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(filename.length()).array());
            outputStream.write(filename.getBytes());
            outputStream.write(ByteBuffer.allocate(Long.BYTES).putLong(length).array());
        }catch (IOException e){
            e.printStackTrace();
            return;
        }
        if(length < 8192){
            bufferSize = Integer.parseInt(String.valueOf(length));
        }
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
                System.out.println("Cannot calc hash sum");
                return;
            }
        }
        while(isRunning){
            try{
                if(fileStream.available() >= bufferSize) {
                    byte[] buffer = new byte[bufferSize];
                    fileStream.read(buffer);
                    outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(buffer.length).array());
                    outputStream.write(buffer);
                    hashSum.update(buffer);
                    length -= bufferSize;
                }else {
                    byte[] buffer = new byte[fileStream.available()];
                    fileStream.read(buffer);
                    outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(buffer.length).array());
                    outputStream.write(buffer);
                    hashSum.update(buffer);
                    length -= buffer.length;
                }
            }catch (IOException e){
                break;
            }
            if(length == 0){
                break;
            }
        }
        byte[] finalHashSum = hashSum.digest();
        try{
            outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(finalHashSum.length).array());
            outputStream.write(finalHashSum);
        }catch (IOException e){
        }
    }

    public void setStopped(){
        isRunning = false;
    }
}
