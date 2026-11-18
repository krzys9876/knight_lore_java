package org.kr.kl.ui;

import org.kr.Color;
import org.kr.kl.objects.Location;
import org.kr.kl.objects.Locations;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class MapPanel extends GamePanel {
    private final int CELL_WIDTH = BASE_WIDTH / 0x10;
    private final int CELL_HEIGHT = BASE_HEIGHT / 0x10;
    private static final int MARGIN = 5;

    public MapPanel() {
        super(256 + 2 * MARGIN, 256 + 2 * MARGIN);
    }

    public void drawMap(Locations locations, int currentLocId) {
        clear();
        for(Location loc : locations.locations) {
            drawOne(loc.id & 0x0F, (loc.id & 0xF0) >> 4, loc.getAttr());
        }
        markCurrent(currentLocId & 0x0F, (currentLocId & 0xF0) >> 4);
    }

    public void clear() {
        Arrays.fill(pixelData, 0);
        preparePixels();
        repaintAction.run();
    }

    private void drawOne(int cellX, int cellY, int color) {
        int pixelX = cellX * CELL_WIDTH + MARGIN;
        int pixelY = cellY * CELL_HEIGHT + MARGIN;
        int screenColor = Color.getScreenColor(color, true);
        for(int x = 1; x < CELL_WIDTH - 1; x++) {
            for(int y = 1; y < CELL_HEIGHT - 1; y++) {
                pixelData[(pixelX + x) + (BASE_HEIGHT - (pixelY + y)) * BASE_WIDTH] = screenColor;
            }
        }
        preparePixels();
        repaintAction.run();
    }

    private void markCurrent(int cellX, int cellY) {
        int pixelX = cellX * CELL_WIDTH + MARGIN;
        int pixelY = cellY * CELL_HEIGHT + MARGIN;
        int color = Color.getScreenColor(Color.WHITE, true);
        for(int i=-1; i<CELL_WIDTH+1; i++) {
            for(int j=-1; j<1; j++) {
                pixelData[(pixelX + i) + (BASE_HEIGHT - (pixelY + j)) * BASE_WIDTH] = color;
                pixelData[(pixelX + i) + (BASE_HEIGHT - (pixelY + j + CELL_HEIGHT)) * BASE_WIDTH] = color;
            }
        }
        for(int i=1; i<CELL_HEIGHT-1; i++) {
            for(int j=-1; j<1; j++) {
                pixelData[(pixelX + j) + (BASE_HEIGHT - (pixelY + i)) * BASE_WIDTH] = color;
                pixelData[(pixelX + j + CELL_WIDTH) + (BASE_HEIGHT - (pixelY + i)) * BASE_WIDTH] = color;
            }
        }
    }
}
