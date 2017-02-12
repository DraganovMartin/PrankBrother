import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
                String[] code = fromClient.readUTF().split("\\s+");
                String requestCode = code[0];
                String data = "";
                if(code.length >= 2){
                    data = code[1];
                }
                System.out.println("Received : " + requestCode + " " +data);
                switch (requestCode){
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
                        break;
                    case StatusCode.GET_LIST_PROCESSES:
                        ArrayList<String> processes = new ArrayList<>();
                        String curProcess;
                        Process p = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe /fo csv /nh");
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while((curProcess = input.readLine()) != null){
                            processes.add(curProcess);
                        }
                        input.close();
                        toClient.writeObject(processes);
                        toClient.flush();
                        break;
                    case StatusCode.KILL_A_PROCESS:
                        Runtime rt = Runtime.getRuntime();
                        if(data.isEmpty()){
                            System.out.println("No process received");
                            break;
                        }
                        else {
                            System.out.println(data);
                            rt.exec("taskkill /F /IM " + data);
                            toClient.writeUTF("Attempt to kill it.");
                            toClient.flush();
                        }
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("Problem in starting server, connecting with client or transferring data.");
            e.printStackTrace();
            //Server.main(new String[]{});
        }
    }
}
