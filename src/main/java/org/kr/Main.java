package org.kr;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentSkipListSet;

public class Main {
    static void main() {
        FlatLightLaf.setup();

        ScreenPanel panel1 = new ScreenPanel();
        ScreenPanel panel2 = new ScreenPanel();
        DebugPanel panel3 = new DebugPanel();
        DebugPanel panel4 = new DebugPanel();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1800, 900);
        frame.setTitle("Knight Lore recreated");
        //frame.setLayout(new GridLayout(1, 3));
        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridy = 0;

        c.gridx = 0; c.weightx = 4.0;
        frame.add(panel1, c);

        c.gridx = 1; c.weightx = 1.0;
        frame.add(panel3, c);

        c.gridx = 2; c.weightx = 1.0;
        frame.add(panel4, c);

        //frame.add(panel2);
        frame.setVisible(true);

        panel1.requestFocus();
        ConcurrentSkipListSet<Integer> keyQueue = new ConcurrentSkipListSet<>();
        Game game = new Game(panel1, panel2, panel3, panel4, keyQueue);
        Thread gameThread = new Thread(game);
        gameThread.start();

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() == KeyEvent.KEY_PRESSED) keyQueue.add(e.getKeyCode());   // e.g. KeyEvent.VK_LEFT
                    else if (e.getID() == KeyEvent.KEY_RELEASED)keyQueue.remove(e.getKeyCode());
                    return false; // false = let the event continue to normal processing
                });
    }
}

