import client.Client;

import java.io.IOException;

public class Main {
    public static void main(String[] args){
        if(args.length != 3){
            System.out.println("Please write IP and port of server and filename as arguments");
            return;
        }
        Client client = new Client();
        if(!client.checkFile(args[2])){
            System.out.println("No such file. Exiting");
            return;
        }
        try {
            client.connect(args[0], Integer.parseInt(args[1]));
        }catch (IOException e){
            System.out.println("Error while connecting to server. Exiting");
            return;
        }
        client.sendFile();
        System.out.println("Program will automatically exit when sending over");
    }
}
