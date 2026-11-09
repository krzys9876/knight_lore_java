package org.kr.kl.ui;

import javax.swing.*;
import java.awt.*;

public class ScreenPanel extends JPanel {
    private final GamePanel gamePanel;

    public ScreenPanel(GamePanel gamePanel) {
        super();
        this.gamePanel = gamePanel;
        gamePanel.setRepaintAction(this::repaint);
        setSize(gamePanel.BASE_WIDTH, gamePanel.BASE_HEIGHT);
        setFocusable(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        gamePanel.preparePixels();
        g.drawImage(gamePanel.image, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}