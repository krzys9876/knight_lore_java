package org.kr.kl.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

    public void addMouseListener(GamePanel gamePanel) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                int imgX = e.getX() * gamePanel.BASE_WIDTH  / getWidth();
                int imgY = e.getY() * gamePanel.BASE_HEIGHT / getHeight();
                gamePanel.doClick(imgX, imgY);
            }
        });
    }
}