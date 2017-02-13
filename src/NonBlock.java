/**
 * Created by DevM on 2/13/2017.
 */
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.*;

public class NonBlock {
    private char ch;
    public  JFrame frame;
    private JPanel panel = new JPanel();
    private JLabel field;
    private ObjectOutputStream toSer = null;
    NonBlock(ObjectOutputStream str){
        this.toSer = str;
        frame = new JFrame();
        field = new JLabel();
        ch =' ';
        panel.setLayout(new FlowLayout());
        panel.add(field);
        frame.add(panel);
        //frame.setUndecorated(true);
        frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        frame.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
                int ch = e.getKeyCode();
                if(ch == 35){
                    Client.stopStrokes = true;
                    frame.dispose();
                }
                try {
                    str.writeUTF(StatusCode.SEND_KEYSTROKES);
                    str.flush();
                    str.reset();
                    str.write(ch);
                    str.flush();
                    str.reset();
                } catch (IOException e1) {
                    System.err.println("Problem sending keystrokes");
                    System.err.println();
                    e1.printStackTrace();
                }
                field.setText("Pressed " + ch);
                field.repaint();
                frame.repaint();

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }
        });
        frame.setVisible(true);
    }

}


