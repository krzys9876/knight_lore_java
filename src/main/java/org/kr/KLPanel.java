package org.kr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class KLPanel extends JPanel {
    final int BASE_WIDTH = 256;
    final int BASE_HEIGHT = 192;
    private int[] pixelData = new int[BASE_WIDTH * BASE_HEIGHT];
    BufferedImage image = new BufferedImage(BASE_WIDTH, BASE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    public KLPanel() {
        super();
        setSize(BASE_WIDTH, BASE_HEIGHT);
        for(int i=0; i<pixelData.length; i++){
            pixelData[i] = (int)(Math.random()*256*256*256);
        }
        prepareImage();
    }

    public void setPixelData(int[] pixelData) {
        this.pixelData = pixelData;
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
