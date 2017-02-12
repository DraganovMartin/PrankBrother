import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Created by DevM on 2/10/2017.
 */
public class Client {
    public static void main(String[] args) {
        boolean stop = false;
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter IP to connect to");
            String ip = sc.nextLine();
            Socket socket = new Socket(ip,9999);
            ObjectOutputStream toSer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream fromSer = new ObjectInputStream(socket.getInputStream());

            while (!stop){
                System.out.println("Enter command, input 'help' for details.");
                String command = sc.nextLine().toUpperCase();
                switch (command){
                    case StatusCode.HELP :
                        for (int i = 0; i < StatusCode.statusCodes.length ; i++) {
                            String code = StatusCode.statusCodes[i];
                            System.out.println(code);
                        }
                        System.out.println();
                        System.out.println("input 'exit' to stop");
                        break;

                    case StatusCode.GET_LIST_PROCESSES:
                        toSer.writeUTF(StatusCode.GET_LIST_PROCESSES);
                        toSer.flush();
                        ArrayList<String> processes = (ArrayList<String>) fromSer.readObject();
                        System.out.println("Below is list of processes");
                        for (int i = 0; i < processes.size() ; i++) {
                            System.out.println(processes.get(i));
                        }
                        System.out.println("\nEnd of list");
                        break;

                    case StatusCode.KILL_A_PROCESS :
                        System.out.println("Enter the process to kill ...");
                        String prcsToKill = sc.nextLine().trim();
                        System.out.println("Sending to server : " +prcsToKill);
                        toSer.writeUTF(StatusCode.KILL_A_PROCESS + " " +prcsToKill);
                        toSer.flush();
                        System.out.println(fromSer.readUTF());
                        break;

                    case StatusCode.SLEEP_PC:
                        toSer.writeUTF(StatusCode.SLEEP_PC);
                        toSer.flush();
                        System.out.println("Send to server : " + command);
                        System.out.println(fromSer.readUTF());
//                        ExecutorService service = Executors.newFixedThreadPool(1);
//                        Future<?> future = service.submit(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    System.out.println(fromSer.readUTF());
//                                } catch (IOException e) {
//                                    System.err.println("Something went wrong getting second response from sleeping request");
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                        try {
//                            future.get(5, TimeUnit.SECONDS);
//
//                        } catch (InterruptedException e) {
//                            System.err.println("Something interrupted thread while waiting");
//                            e.printStackTrace();
//                        } catch (ExecutionException e) {
//                            System.err.println("Exception from the real thread");
//                            e.printStackTrace();
//                        } catch (TimeoutException e) {
//                            System.err.println("Timeout of 5 secs exceeded terminating waiting for second sleep response, probably PC is sleeping");
//                            future.cancel(true);
//                            e.printStackTrace();
//                        }
                        System.out.println(fromSer.readUTF()); // delete if uncommenting upper code
                        break;

                    case StatusCode.CHECK_CONNECTION:
                        toSer.writeUTF(StatusCode.CHECK_CONNECTION);
                        try {
                            toSer.flush();
                            System.out.println(fromSer.readUTF());
                        }
                        catch (IOException ex){
                            System.err.println("Server not responding !!!");
                        }
                        break;

                    case StatusCode.EXIT:
                        stop = true;
                        break;
                    case StatusCode.KILL_SERVER:
                        toSer.writeUTF(StatusCode.KILL_SERVER);
                        toSer.flush();
                        break;
                    default :
                        System.out.println("Please enter valid command");
                }
            }
            System.out.println("Exiting");
            socket.close();

        } catch (IOException e) {
            System.err.println("Problem connecting to server or server shut down");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
