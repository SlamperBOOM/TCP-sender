package client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;
    private File file;
    private ClientThread thread;
    private ServerListener listener;

    public Client(){

    }

    public void connect(String IP, int port) throws IOException {
        socket = new Socket(IP, port);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public boolean checkFile(String filename){
        file = new File(filename);
        return file.exists();
    }

    public void sendFile(){
        thread = new ClientThread(outputStream, file);
        listener = new ServerListener(inputStream, this);
        thread.start();
        listener.start();
    }

    public void disconnect(){
        try{
            socket.close();
            inputStream.close();
            outputStream.close();
            thread.setStopped();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
