package org.kr;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Main {
    static void main() {
        IO.println("START init");
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

        IO.println("END init");
    }
}

class KLPanel extends JPanel {
    final int BASE_WIDTH = 256+2;
    final int BASE_HEIGHT = 192+2;
    int[] pixelData = new int[BASE_WIDTH * BASE_HEIGHT];
    BufferedImage image = new BufferedImage(BASE_WIDTH, BASE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    public KLPanel() {
        super();
        setSize(BASE_WIDTH, BASE_HEIGHT);
        for(int i=0; i<pixelData.length; i++){
            pixelData[i] = (int)(Math.random()*256*256*256);
        }
        for(int i=0; i<BASE_WIDTH; i++){
            pixelData[i] = 0x00888888;
            pixelData[(BASE_HEIGHT-1)*BASE_WIDTH+i] = 0x00888888;
        }
        for(int i=0; i<BASE_HEIGHT; i++){
            pixelData[BASE_WIDTH * i] = 0x00888888;
            pixelData[BASE_WIDTH * (i+1)-1] = 0x00888888;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        WritableRaster raster = image.getRaster();
        raster.setDataElements(0, 0, BASE_WIDTH, BASE_HEIGHT, pixelData);
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);

    }
}
