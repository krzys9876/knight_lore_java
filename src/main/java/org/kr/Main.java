package org.kr;

import com.formdev.flatlaf.FlatLightLaf;
import org.kr.kl.Game;
import org.kr.kl.GameParams;
import org.kr.kl.ui.*;
import org.kr.kl.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentSkipListSet;

public class Main {
    static void main() {
        FlatLightLaf.setup();

        GamePanel gamePanel = new GamePanel();
        ScreenPanel screenGamePanel = new ScreenPanel(gamePanel);
        GamePanel shadowPanel = new GamePanel();
        ScreenPanel screenShadowPanel = new ScreenPanel(shadowPanel);
        GamePanel previewPanel = new GamePanel();
        ScreenPanel screenPreviewPanel = new ScreenPanel(previewPanel);
        MapPanel mapPanel = new MapPanel(previewPanel);
        ScreenPanel screenMapPanel = new ScreenPanel(mapPanel);
        ScreenDebugPanel debugPanel1 = new ScreenDebugPanel();
        ScreenDebugPanel debugPanel2 = new ScreenDebugPanel();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(256*4, 192*4);
        frame.setTitle("Knight Lore recreated");
        //frame.setLayout(new GridLayout(1, 3));
        //frame.setLayout(new GridBagLayout());
        frame.add(screenGamePanel, BorderLayout.CENTER);
        //GridBagConstraints c = new GridBagConstraints();
        //c.fill = GridBagConstraints.BOTH;
        //c.weighty = 1.0;
        //c.gridy = 0;

        //c.gridx = 0; c.weightx = 4.0;
        //frame.add(panel1, c);

        //c.gridx = 1; c.weightx = 1.0;
        //frame.add(panel3, c);

        //c.gridx = 2; c.weightx = 1.0;
        //frame.add(panel4, c);

        //frame.add(panel2);
        JMenuBar menuBar = new JMenuBar();
        JMenu windowMenu = new JMenu("Windows");
        menuBar.add(windowMenu);
        frame.setJMenuBar(menuBar);

        ToolWindow.addToggleWindow(frame, windowMenu, "Map", screenMapPanel, new Dimension(mapPanel.BASE_WIDTH, mapPanel.BASE_HEIGHT), null);
        ToolWindow.addToggleWindow(frame, windowMenu, "Preview", screenPreviewPanel, new Dimension(previewPanel.BASE_WIDTH, previewPanel.BASE_HEIGHT), null);
        ToolWindow.addToggleWindow(frame, windowMenu, "Debug (1)", debugPanel1, new Dimension(250, 750), null);
        ToolWindow.addToggleWindow(frame, windowMenu, "Debug (2)", debugPanel2, new Dimension(350, 500), null);
        ToolWindow.addToggleWindow(frame, windowMenu, "Shadow memory view", screenShadowPanel, new Dimension(400, 300), null);

        frame.setVisible(true);
        screenGamePanel.requestFocus();

        ConcurrentSkipListSet<Integer> keyQueue = new ConcurrentSkipListSet<>();
        KeySequence keySequence = new KeySequence(KeySequenceMode.NONE); // set SAVE to enable saving or REPLAY to replay the complete gameplay
        //KeySequence keySequence = new KeySequence(KeySequenceMode.REPLAY); // set SAVE to enable saving or REPLAY to replay the complete gameplay, REPLAY_SAVE to replay and save each frame
        SeedGenerator rnd = new SeedGeneratorRandom();
        //SeedGenerator rnd = new SeedGeneratorSequential();
        //GameParams params = new GameParams(rnd, false, false, false, false, 99, 98, 14, 90, 0);
        GameParams params = new GameParams(rnd, true, false, false, false, 99, 98, 14, 90, 0);
        Game game = new Game(gamePanel, shadowPanel, mapPanel, debugPanel1, debugPanel2, keyQueue, params, keySequence);
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

