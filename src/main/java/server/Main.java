package server;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    private static final int PORT = 34522;
   // private static final String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) {
        // todo - your java-code
        try(ServerSocket server = new ServerSocket(PORT)){
            System.out.println("Server is run");
            while(true){
                Session session = new Session(server.accept());
                session.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
