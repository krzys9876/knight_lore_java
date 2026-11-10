package org.kr;

import com.formdev.flatlaf.FlatLightLaf;
import org.kr.kl.Game;
import org.kr.kl.GameParams;
import org.kr.kl.ui.GamePanel;
import org.kr.kl.ui.ScreenDebugPanel;
import org.kr.kl.ui.ScreenPanel;
import org.kr.kl.utils.KeySequence;
import org.kr.kl.utils.KeySequenceMode;
import org.kr.kl.utils.SeedGenerator;
import org.kr.kl.utils.SeedGeneratorSequential;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentSkipListSet;

public class Main {
    static void main() {
        FlatLightLaf.setup();

        GamePanel gamePanel1 = new GamePanel();
        ScreenPanel panel1 = new ScreenPanel(gamePanel1);
        GamePanel gamePanel2 = new GamePanel();
        ScreenPanel panel2 = new ScreenPanel(gamePanel2);
        ScreenDebugPanel panel3 = new ScreenDebugPanel();
        ScreenDebugPanel panel4 = new ScreenDebugPanel();
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
        KeySequence keySequence = new KeySequence(KeySequenceMode.NONE); // set SAVE to enable saving or REPLAY to replay the complete gameplay
        SeedGenerator rnd = new SeedGeneratorSequential();
        GameParams params = new GameParams(rnd, false, false, false, false, 99, 98, 14, 90, 0);
        Game game = new Game(gamePanel1, gamePanel2, panel3, panel4, keyQueue, params, keySequence);
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

