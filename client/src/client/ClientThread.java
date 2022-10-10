package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
        String filename = file.getName();
        long length = file.length();
        System.out.println("Filename: " + filename);
        System.out.println("Length of file: " + length + " bytes");
        try{
            outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(filename.length()).array());
            outputStream.write(filename.getBytes());
            outputStream.write(ByteBuffer.allocate(Long.BYTES).putLong(length).array());
        }catch (IOException e){
            e.printStackTrace();
            return;
        }
        if(length < 1024){
            bufferSize = Integer.parseInt(String.valueOf(length));
        }
        while(isRunning){
            try{
                byte[] buffer = new byte[bufferSize];
                fileStream.read(buffer);
                outputStream.write(buffer);
                length -= bufferSize;
            }catch (IOException e){
                break;
            }
            if(length == 0){
                break;
            }
        }
    }

    public void setStopped(){
        isRunning = false;
    }
}
