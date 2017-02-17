import javafx.stage.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Created by DevM on 2/10/2017.
 */
public class Client {
    private static ObjectOutputStream toSer;
    public static boolean stopStrokes;
    private static String ip = null;

    public static void main(String[] args) {
        boolean stop = false;
        Socket socket = null;
        try {
            Scanner sc = new Scanner(System.in);
            if (args.length > 0) {
                ip = args[0];
            } else {
                System.out.println("Enter IP to connect to");
                ip = sc.nextLine();
            }
            socket = new Socket(ip, 9999);
            toSer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream fromSer = new ObjectInputStream(socket.getInputStream());


            while (!stop) {
                System.out.println("Enter command, input 'help' for details.");
                String command = sc.nextLine().toUpperCase();
                switch (command) {
                    case StatusCode.HELP:
                        Arrays.sort(StatusCode.statusCodes);
                        for (int i = 0; i < StatusCode.statusCodes.length; i++) {
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
                        for (int i = 0; i < processes.size(); i++) {
                            System.out.println(processes.get(i));
                        }
                        System.out.println("\nEnd of list");
                        break;

                    case StatusCode.KILL_A_PROCESS:
                        System.out.println("Enter the process to kill ...");
                        String prcsToKill = sc.nextLine().trim();
                        System.out.println("Sending to server : " + prcsToKill);
                        toSer.writeUTF(StatusCode.KILL_A_PROCESS + " " + prcsToKill);
                        toSer.flush();
                        System.out.println(fromSer.readUTF());
                        break;

                    case StatusCode.SEND_KEYSTROKES:
                        System.out.println("Start typing ... press 'end' to stop");

                        new NonBlock(toSer);

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
                    case StatusCode.SEND_FILE:
                        final File file = new ChooseFile().getFile();
                        if(file == null){
                            break;
                        }
                        String[] toGetName = file.getPath().split("\\\\");
                        final String name = toGetName[toGetName.length - 1];
                        toSer.writeUTF(StatusCode.SEND_FILE + " " + name);
                        toSer.flush();
                        final int filePort = Integer.valueOf(fromSer.readUTF());
                        System.out.println(fromSer.readUTF());
                        long start = System.currentTimeMillis();
                        Thread t1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Socket fileSocket = new Socket(ip, filePort);
                                    BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(file));
                                    BufferedOutputStream toFileSer = new BufferedOutputStream(fileSocket.getOutputStream());
                                    byte[] bytes = new byte[8192];
                                    int data;
                                    while ((data = fileStream.read(bytes)) != -1) {
                                        toFileSer.write(bytes, 0, data);
                                        toFileSer.flush();
                                    }
                                    fileStream.close();
                                    toFileSer.close();
                                    fileSocket.close();
                                } catch (IOException e) {
                                    System.err.println("Problem connecting to file server !");
                                    e.printStackTrace();
                                }
                            }
                        });
                        t1.start();
                        try {
                            t1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        long end = System.currentTimeMillis() - start;
                        System.out.println("File with size " + file.length() / 1000 + " kb's uploaded successfully for : " + end / 1000 + "secs !");
                        break;

                    case StatusCode.GET_FILES_TO_OPEN:
                        toSer.writeUTF(StatusCode.GET_FILES_TO_OPEN);
                        toSer.flush();
                        String[] files = (String[]) fromSer.readObject();
                        System.out.println(Arrays.toString(files));
                        break;

                    case StatusCode.OPEN_FILE_ON_SERVER:
                        System.out.println("Enter file to open ...");
                        String fileToOpen = sc.nextLine();
                        toSer.writeUTF(StatusCode.OPEN_FILE_ON_SERVER + " " + fileToOpen);
                        toSer.flush();
                        System.out.println(fromSer.readUTF());
                        break;

                    case StatusCode.DELETE_FILE:
                        System.out.println("Enter file to delete ...");
                        String fileToDel = sc.nextLine();
                        toSer.writeUTF(StatusCode.DELETE_FILE + " " +fileToDel);
                        toSer.flush();
                        System.out.println(fromSer.readUTF());
                        break;

                    case StatusCode.RESTART_PC:
                        toSer.writeUTF(StatusCode.RESTART_PC);
                        toSer.flush();
                        System.out.println(fromSer.readUTF() + " check connection !");
                        break;

                    case StatusCode.TERMINATE_PC:
                        toSer.writeUTF(StatusCode.TERMINATE_PC);
                        toSer.flush();
                        System.out.println(fromSer.readUTF() + " check connection !");
                        break;

                    case StatusCode.CHECK_CONNECTION:
                        toSer.writeUTF(StatusCode.CHECK_CONNECTION);
                        try {
                            toSer.flush();
                            System.out.println(fromSer.readUTF());
                        } catch (IOException ex) {
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

                    case StatusCode.RESET_CONNECTION:
                        File thisProgram = new File("Client.jar");
                        Desktop.getDesktop().open(thisProgram);
                        System.exit(5);
                    default:
                        System.out.println("Please enter valid command");
                }
            }
            System.out.println("Exiting");
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Problem connecting to server or server shut down");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class ChooseFile {
        private JFrame frame;
        public ChooseFile() {
            frame = new JFrame();

            frame.setVisible(true);
            BringToFront();
        }
        public File getFile() {
            JFileChooser fc = new JFileChooser();
            if(JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null)){
                frame.setVisible(false);
                return fc.getSelectedFile();
            }else {
                System.out.println("Next time select a file.");
                //System.exit(1);
            }
            return null;
        }

        private void BringToFront() {
            frame.setExtendedState(JFrame.ICONIFIED);
            frame.setExtendedState(JFrame.NORMAL);

        }

    }

}
