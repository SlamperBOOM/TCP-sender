package client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ServerListener extends Thread{
    private InputStream inputStream;
    private Client client;

    public ServerListener(InputStream inputStream, Client client){
        this.inputStream = inputStream;
        this.client = client;
    }

    @Override
    public void run(){
        byte[] message = new byte[Integer.BYTES];
        while (true){
            try{
                inputStream.read(message);
                int code = ByteBuffer.wrap(message).getInt();
                if(code == 1){
                    System.out.println("Error occurred while sending to server");
                    client.disconnect();
                    break;
                }else if(code == 0){
                    System.out.println("File was successfully sent");
                    client.disconnect();
                    break;
                }
            }catch (IOException e){
                System.out.println("Disconnected from server");
                break;
            }
        }
    }
}
