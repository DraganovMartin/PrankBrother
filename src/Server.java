import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by DevM on 2/10/2017.
 */
public class Server {
    private static Socket fileSocket = null;
    public static void main(String[] args) {
        boolean stopServer = false;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(9999);
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

                    case StatusCode.SEND_KEYSTROKES :
                        int charCode = fromClient.read();
                        try {
                            Robot robo = new Robot();
                            robo.setAutoWaitForIdle(true);
                            try {
                                robo.keyPress(charCode);
                            }catch(IllegalArgumentException ex){
                                System.err.println("Inalid keyCode : " + charCode);
                            }
                            robo.setAutoDelay(100);
                            try {
                                robo.keyRelease(charCode);
                            }catch(IllegalArgumentException ex){
                                System.err.println("Invalid keyCode : " + charCode);
                            }
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }

                        break;

                    case StatusCode.SEND_FILE:
                        final String name = data;
                        final ServerSocket tempSocket = new ServerSocket(0);
                        while(!tempSocket.isBound()){

                        }
                        toClient.writeUTF(Integer.toString(tempSocket.getLocalPort()));
                        toClient.flush();

                        Thread t1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    fileSocket = tempSocket.accept();
                                    File file = new File(name);
                                    DataOutputStream outputStream = new DataOutputStream(fileSocket.getOutputStream());
                                    BufferedInputStream bis = new BufferedInputStream(fileSocket.getInputStream());
                                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                    byte[] bytes = new byte[8192];
                                    int data;
                                    while((data = bis.read(bytes)) != -1){
                                        bos.write(bytes,0,data);
                                        bos.flush();
                                    }

                                    bos.close();
//                                    System.out.println("File written !");
//                                    toClient.writeUTF("File written !");
                                    bis.close();

                                    fileSocket.close();
                                    tempSocket.close();
                                } catch (IOException e) {
                                    System.err.println("Cannot connect with file client !");
                                    e.printStackTrace();
                                }
                            }
                        });
                        t1.start();
                        toClient.writeUTF("File agent started...");
                        toClient.flush();
                        try {
                            t1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;

                    case StatusCode.GET_FILES_TO_OPEN:;
                        final File files = new File(".");
                        toClient.writeObject(files.list());
                        toClient.flush();
                        break;

                    case StatusCode.OPEN_FILE_ON_SERVER:
                        File fileToOpen = new File(data);
                        Desktop.getDesktop().open(fileToOpen);
                        toClient.writeUTF("File opened with default program");
                        toClient.flush();
                        break;

                    case StatusCode.DELETE_FILE :
                        File fileToDel = new File(data);
                        fileToDel.delete();
                        toClient.writeUTF("File deleted !");
                        toClient.flush();
                        break;


                    case StatusCode.RESTART_PC:
                        toClient.writeUTF("Attempting to restart remote pc ...");
                        toClient.flush();
                        Runtime.getRuntime().exec("shutdown /r");
                        break;

                    case StatusCode.TERMINATE_PC:
                        toClient.writeUTF("Attempting to restart remote pc ...");
                        toClient.flush();
                        Runtime.getRuntime().exec("shutdown /s");
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("Problem in starting server, connecting with client or transferring data.");
            e.printStackTrace();
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e1) {
                    System.err.println("Cannot close socket !");
                }
            }
            Server.main(new String[]{});
        }
    }
}
