package org.kr;

import com.formdev.flatlaf.FlatLightLaf;
import org.kr.kl.Game;
import org.kr.kl.GameParams;
import org.kr.kl.ui.*;
import org.kr.kl.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ConcurrentSkipListSet;

public class Main {
    static void main() {
        FlatLightLaf.setup();

        ConcurrentSkipListSet<Integer> keyQueue = new ConcurrentSkipListSet<>();
        KeySequence keySequence = new KeySequence(KeySequenceMode.NONE); // set SAVE to enable saving or REPLAY to replay the complete gameplay
        //KeySequence keySequence = new KeySequence(KeySequenceMode.REPLAY); // set SAVE to enable saving or REPLAY to replay the complete gameplay, REPLAY_SAVE to replay and save each frame
        SeedGenerator rnd = new SeedGeneratorRandom();
        //SeedGenerator rnd = new SeedGeneratorSequential();
        //GameParams params = new GameParams(rnd, false, false, false, false, 99, 98, 14, 90, 0);
        GameParams params = new GameParams(rnd, false, false, false, false, 40, 4, 14, 90, 0);

        GamePanel gamePanel = new GamePanel();
        ScreenPanel screenGamePanel = new ScreenPanel(gamePanel);
        GamePanel shadowPanel = new GamePanel();
        ScreenPanel screenShadowPanel = new ScreenPanel(shadowPanel);
        GamePanel previewPanel = new GamePanel();
        ScreenPanel screenPreviewPanel = new ScreenPanel(previewPanel);
        MapPanel mapPanel = new MapPanel(previewPanel);
        ScreenPanel screenMapPanel = new ScreenPanel(mapPanel);
        ObjectsPanel objectsPanel = new ObjectsPanel(mapPanel);
        ScreenPanel screenObjectsPanel = new ScreenPanel(objectsPanel);
        SettingsPanel settingsPanel = new SettingsPanel(params);
        ScreenPanel screenSettingsPanel = new ScreenPanel(settingsPanel);
        ScreenDebugPanel debugPanel1 = new ScreenDebugPanel();
        ScreenDebugPanel debugPanel2 = new ScreenDebugPanel();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(gamePanel.BASE_WIDTH * 4, gamePanel.BASE_HEIGHT * 4);
        frame.setTitle("Knight Lore recreated");
        frame.add(screenGamePanel, BorderLayout.CENTER);

        screenMapPanel.addMouseListener(mapPanel);
        screenObjectsPanel.addMouseListener(objectsPanel);
        screenSettingsPanel.addMouseListener(settingsPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu windowMenu = new JMenu("Windows");
        JMenu debugMenu = new JMenu("Debug");
        menuBar.add(windowMenu);
        menuBar.add(debugMenu);
        frame.setJMenuBar(menuBar);

        ToolWindow.addToggleWindow(frame, windowMenu, "Map", screenMapPanel, new Dimension(mapPanel.BASE_WIDTH, mapPanel.BASE_HEIGHT), null);
        ToolWindow.addToggleWindow(frame, windowMenu, "Preview", screenPreviewPanel, new Dimension(previewPanel.BASE_WIDTH * 2, previewPanel.BASE_HEIGHT * 2), null);
        ToolWindow.addToggleWindow(frame, windowMenu, "Objects", screenObjectsPanel, new Dimension(objectsPanel.BASE_WIDTH * 2, objectsPanel.BASE_HEIGHT * 2), null);
        ToolWindow.addToggleWindow(frame, windowMenu, "Settings", screenSettingsPanel, new Dimension(settingsPanel.BASE_WIDTH * 2, settingsPanel.BASE_HEIGHT * 2), null);
        ToolWindow.addToggleWindow(frame, debugMenu, "Debug (1)", debugPanel1, new Dimension(250, 750), null);
        ToolWindow.addToggleWindow(frame, debugMenu, "Debug (2)", debugPanel2, new Dimension(350, 500), null);
        ToolWindow.addToggleWindow(frame, debugMenu, "Shadow memory view", screenShadowPanel, new Dimension(400, 300), null);

        frame.setVisible(true);
        screenGamePanel.requestFocus();

        Game game = new Game(gamePanel, shadowPanel, mapPanel, objectsPanel, settingsPanel, debugPanel1, debugPanel2, keyQueue, params, keySequence);
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

