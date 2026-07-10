package org.kr;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Main {
    static void main() {
        IO.println("START");
        FlatLightLaf.setup();

        JPanel panel1 = new KLPanel();
        JPanel panel2 = new KLPanel();
        JPanel panel3 = new KLPanel();
        JPanel panel4 = new KLPanel();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setTitle("KL");
        frame.setLayout(new GridLayout(2, 2));
        frame.add(panel1);
        frame.add(panel2);
        frame.add(panel3);
        frame.add(panel4);
        frame.setVisible(true);


        //panel.paint(g);
        //panel.repaint();

        IO.println("END");
    }
}

class KLPanel extends JPanel {
    final int WIDTH = 400;
    final int HEIGHT = 300;
    BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    public KLPanel() {
        super();
        setSize(WIDTH, HEIGHT);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics gi = image.getGraphics();
        gi.setColor(Color.RED);
        gi.fillRect(0, 0, WIDTH/2, HEIGHT/2);
        gi.setColor(Color.GREEN);
        gi.fillRect(WIDTH/2, HEIGHT/2, WIDTH/2, HEIGHT/2);

        g.drawImage(image, 0, 0, null);

    }
}
