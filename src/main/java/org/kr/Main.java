package org.kr;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    static void main() {
        IO.println("START init");
        FlatLightLaf.setup();

        ScreenPanel panel1 = new ScreenPanel();
        ScreenPanel panel2 = new ScreenPanel();
        DebugPanel panel3 = new DebugPanel();
        DebugPanel panel4 = new DebugPanel();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);
        frame.setTitle("KL");
        frame.setLayout(new GridLayout(2, 2));
        frame.add(panel1);
        frame.add(panel2);
        frame.add(panel3);
        frame.add(panel4);
        frame.setVisible(true);

        /*try {
            panel1.saveImage("p1.png");
            panel1.saveImage("p2.png");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ConcurrentLinkedQueue<Integer> keyQueue = new ConcurrentLinkedQueue<>();
        Game game = new Game(panel1, panel2, panel3, panel4, keyQueue);
        Thread gameThread = new Thread(game);
        gameThread.start();

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        keyQueue.add(e.getKeyCode());   // e.g. KeyEvent.VK_LEFT
                    }
                    return false; // false = let the event continue to normal processing
                });


        IO.println("END init");
    }
}

