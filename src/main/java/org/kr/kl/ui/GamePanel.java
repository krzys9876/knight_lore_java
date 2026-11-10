package org.kr.kl.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

    public long screenHash() {
        long h = 0xcbf29ce484222325L;            // FNV offset basis
        for (int v : pixelData) {
            h ^= (v & 0xFFFFFF);                  // same alpha mask on both sides!
            h *= 0x100000001b3L;                  // FNV prime
        }
        return h;
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

    public void saveImage(String path, String name) throws IOException {
        ImageIO.write(image, "png", new File(Path.of(path, name).toUri()));
        try(var fw = Files.newBufferedWriter(Path.of(path, "hashes.txt"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            fw.write(String.format("%s %d\n", name, screenHash()));
        }
    }

    public void setRepaintAction(Runnable r) {
        repaintAction = r;
    }

}
