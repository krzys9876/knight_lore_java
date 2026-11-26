package org.kr.kl.ui;

import org.kr.kl.data.DataBlock;
import org.kr.kl.objects.Location;
import org.kr.kl.objects.Locations;

import java.util.Arrays;

public class MapPanel extends GamePanel {
    private static final int ROW_COL = 0x10;
    private static final int CELL_WIDTH = 35;
    private static final int CELL_HEIGHT = 30;
    private static final int MARGIN = 15;
    private final GamePanel previewPanel;
    private Locations locations;
    private DataBlock font;
    private int selectedId = -1;
    private int currentId = -1;

    public MapPanel(GamePanel previewPanel) {
        super(ROW_COL * CELL_WIDTH + 2 * MARGIN, ROW_COL * CELL_HEIGHT + 2 * MARGIN);
        this.previewPanel = previewPanel;
    }

    public void setLocations(Locations locations) { this.locations = locations; }
    public void setFont(DataBlock font) { this.font = font; }

    public void drawMap(Locations locations, int currentLocId) {
        clear();
        printLegend();
        currentId = currentLocId;
        if(locations == null) return;

        for(Location loc : locations.locations) drawOne(loc.x, loc.y, loc.getAttr(), loc.isVisited());
        if(selectedId == -1) updatePreview(currentLocId);
        markSelected();
        markCurrent();
        repaint();
    }

    private void printLegend() {
        int screenColor = Color.getScreenColor(Color.WHITE, true);
        for(int i=0; i<ROW_COL; i++) {
            int x = MARGIN + i * CELL_WIDTH + CELL_WIDTH/2-4;
            int x1 = MARGIN/2-4;
            int x2 = ROW_COL * CELL_WIDTH + MARGIN + MARGIN/2-4;
            for(int b = 0; b<8; b++) {
                int y1 = (b + MARGIN/2-4) * BASE_WIDTH;
                int y2 = (b + MARGIN/2-4) * BASE_WIDTH + (ROW_COL * CELL_HEIGHT + MARGIN) * BASE_WIDTH;
                int y = (b + MARGIN + (ROW_COL - 1 - i) * CELL_HEIGHT + CELL_HEIGHT/2-4) * BASE_WIDTH;
                int fontByte = font.getU(font.start + i*8 + b);
                for(int bit = 0; bit<8; bit++) {
                    pixelData[x + y1 + (7-bit)] = (fontByte & (1 << bit))>0 ? screenColor : 0;
                    pixelData[x + y2 + (7-bit)] = (fontByte & (1 << bit))>0 ? screenColor : 0;
                    pixelData[x1 + y + (7-bit)] = (fontByte & (1 << bit))>0 ? screenColor : 0;
                    pixelData[x2 + y + (7-bit)] = (fontByte & (1 << bit))>0 ? screenColor : 0;
                }
            }
        }
    }

    public void updatePreview(int locId) {
        selectedId = locId;
        if(locations == null || !locations.exists(locId)) return;
        Location loc = locations.get(locId);
        int attr = loc.getAttr();
        VideoMemory memory = loc.getPreview();
        for(int a = memory.attrStart; a < memory.endExcl; a ++) memory.setByteAt(a, attr);
        int[] pixels = memory.toPixels(false);

        previewPanel.setPixelData(pixels);
    }

    private void repaint() {
        preparePixels();
        repaintAction.run();
    }

    public void clear() {
        Arrays.fill(pixelData, 0);
        repaint();
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
        repaint();
    }

    private void markCurrent() {
        if(currentId == -1) return;
        mark(currentId, Color.getScreenColor(Color.WHITE, true));
    }

    private void markSelected() {
        if(selectedId == -1 || !locations.exists(selectedId)) return;
        mark(selectedId, Color.getScreenColor(Color.RED, true));
    }

    private void unmarkSelected() {
        if(selectedId == -1) return;
        mark(selectedId, Color.getScreenColor(Color.BLACK, false));
    }

    private void mark(int id, int color) {
        int cellX = Location.xFromId(id);
        int cellY = Location.yFromId(id);
        int pixelX = cellX * CELL_WIDTH + MARGIN;
        int pixelY = cellY * CELL_HEIGHT + MARGIN;
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
        if(x < MARGIN || x >= MARGIN + CELL_WIDTH * ROW_COL || y < MARGIN || y >= MARGIN + CELL_HEIGHT * ROW_COL) return;
        int gridX = (x - MARGIN) / CELL_WIDTH;
        int gridY = ROW_COL - 1 - (y - MARGIN) / CELL_HEIGHT;
        updateAfterClick(Location.xyToId(gridX, gridY));
    }

    public void updateAfterClick(int locId) {
        if(locations.exists(locId)) {
            unmarkSelected();
            updatePreview(locId);
            markSelected();
            markCurrent();
            repaint();
        }
    }
}
