import server.Server;

import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        if(args.length == 0){
            System.out.println("Please write port as argument");
            return;
        }
        Server server = new Server(args[0]);
        System.out.println("Write \"stop\" to stop the server");
        Scanner reader = new Scanner(System.in);
        while (true){
            String line = reader.nextLine();
            if(line.equals("stop")){
                server.stop();
                break;
            }
        }
        System.out.println("Wait a few seconds and let server finish");
    }
}
