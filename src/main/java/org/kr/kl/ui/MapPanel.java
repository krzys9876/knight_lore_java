package org.kr.kl.ui;

import org.kr.kl.objects.Location;
import org.kr.kl.objects.Locations;

import java.util.Arrays;

public class MapPanel extends GamePanel {
    private final int CELL_WIDTH = BASE_WIDTH / 0x10;
    private final int CELL_HEIGHT = BASE_HEIGHT / 0x10;
    private static final int MARGIN = 5;
    private final GamePanel previewPanel;
    private Locations locations;

    public MapPanel(GamePanel previewPanel) {
        super(256 + 2 * MARGIN, 256 + 2 * MARGIN);
        this.previewPanel = previewPanel;
    }

    public void setLocations(Locations locations) { this.locations = locations; }

    public void drawMap(Locations locations, int currentLocId) {
        clear();
        if(locations == null) return;

        for(Location loc : locations.locations) {
            drawOne(loc.id & 0x0F, (loc.id & 0xF0) >> 4, loc.getAttr(), loc.isVisited());
            if(loc.id == currentLocId) {
                markCurrent(currentLocId & 0x0F, (currentLocId & 0xF0) >> 4);
                //updatePreview(loc);
            }
        }
    }

    public void updatePreview(int locId) {
        if(locations == null || !locations.exists(locId)) return;
        Location loc = locations.get(locId);
        int attr = loc.getAttr();
        VideoMemory memory = loc.getPreview();
        for(int a = memory.attrStart; a < memory.endExcl; a ++) memory.setByteAt(a, attr);
        int[] pixels = loc.getPreview().toPixels(false);
        previewPanel.setPixelData(pixels);
    }

    public void clear() {
        Arrays.fill(pixelData, 0);
        preparePixels();
        repaintAction.run();
    }

    private void drawOne(int cellX, int cellY, int color, boolean isVisited) {
        int pixelX = cellX * CELL_WIDTH + MARGIN;
        int pixelY = cellY * CELL_HEIGHT + MARGIN;
        int screenColor = Color.getScreenColor(color, !isVisited);
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

    public void doClick(int x, int y) {
        IO.println(String.format("Click: %d,%d", x, y));
        if(x < MARGIN || x >= MARGIN + CELL_WIDTH * 0x10 || y < MARGIN || y >= MARGIN + CELL_HEIGHT * 0x10) return;
        int gridX = (x - MARGIN) / CELL_WIDTH;
        int gridY = 0x10 - 1 - (y - MARGIN) / CELL_HEIGHT;
        IO.println(String.format("Grid:  %d,%d", gridX, gridY));
        int locId = gridX + gridY * 0x10;
        updatePreview(locId);
    }
}
