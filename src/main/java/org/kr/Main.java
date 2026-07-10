package org.kr;

import com.formdev.flatlaf.FlatLightLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class Main {
    static void main() {
        IO.println("START init");
        FlatLightLaf.setup();

        KLPanel panel1 = new KLPanel();
        KLPanel panel2 = new KLPanel();
        KLPanel panel3 = new KLPanel();
        KLPanel panel4 = new KLPanel();
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

        try {
            panel1.saveImage("p1.png");
            panel1.saveImage("p2.png");
            panel1.saveImage("p3.png");
            panel1.saveImage("p4.png");
            KLPanel panel5 = new KLPanel();
            panel5.saveImage("p5.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        prepareImage();
    }

    private void prepareImage() {
        WritableRaster raster = image.getRaster();
        raster.setDataElements(0, 0, BASE_WIDTH, BASE_HEIGHT, pixelData);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        prepareImage();
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);

    }

    public void saveImage(String path) throws IOException {
        ImageIO.write(image, "png", new File(path));
    }
}
