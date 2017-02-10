import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by DevM on 2/10/2017.
 */
public class Server {
    public static void main(String[] args) {
        boolean stopServer = false;
        try {
            ServerSocket socket = new ServerSocket(9999);
            Socket client = socket.accept();
            System.out.println("Client connected !");
            ObjectOutputStream toClient = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream fromClient = new ObjectInputStream(client.getInputStream());
            while(!stopServer){
                String code = fromClient.readUTF();
                System.out.println("Received : " + code);
                switch (code){
                    case StatusCode.SLEEP_PC :
                        toClient.writeUTF("Trying to sleep PC, if you see ' block of code executed ' and connection is available something is wrong ");
                        toClient.flush();
                        Runtime.getRuntime().exec("Rundll32.exe powrprof.dll,SetSuspendState Sleep");
                        toClient.writeUTF("block of code executed .. check connection");
                        toClient.flush();
                        break;
                    case StatusCode.CHECK_CONNECTION:
                        toClient.writeUTF("Alive");
                        toClient.flush();
                        break;
                    case StatusCode.EXIT:
                        //System.exit(3);
                        break;
                    case StatusCode.KILL_SERVER:
                        toClient.writeUTF("Server stops");
                        toClient.flush();
                        System.exit(3);
                }
            }

        } catch (IOException e) {
            System.err.println("Problem in starting server, connecting with client or transferring data.");
            e.printStackTrace();
            Server.main(new String[]{});
        }
    }
}
