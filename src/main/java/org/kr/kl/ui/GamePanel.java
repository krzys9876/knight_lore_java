package org.kr.kl.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class GamePanel {
    final int BASE_WIDTH = 256;
    final int BASE_HEIGHT = 192;
    protected int[] pixelData = new int[BASE_WIDTH * BASE_HEIGHT];
    BufferedImage image = new BufferedImage(BASE_WIDTH, BASE_HEIGHT, BufferedImage.TYPE_INT_RGB);
    private Runnable repaintAction = () -> {};

    public GamePanel() {
        for(int i=0; i<pixelData.length; i++){
            pixelData[i] = (int)(Math.random()*256*256*256);
        }
        preparePixels();
    }

    public BufferedImage getImage() {
        return image;
    }

    public int[] getPixelData() {
        return pixelData;
    }

    public void setPixelData(int[] pixelData) {
        this.pixelData = pixelData;
        preparePixels();
        repaintAction.run();
    }

    protected void preparePixels() {
        WritableRaster raster = image.getRaster();
        raster.setDataElements(0, 0, BASE_WIDTH, BASE_HEIGHT, pixelData);
    }

    public void saveImage(String path) throws IOException {
        ImageIO.write(image, "png", new File(path));
    }

    public void setRepaintAction(Runnable r) {
        repaintAction = r;
    }

}
