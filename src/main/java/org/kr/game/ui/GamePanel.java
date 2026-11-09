package org.kr.game.ui;

import java.io.IOException;

public interface GamePanel {
    void setPixelData(int[] pixelData);
    void saveImage(String path) throws IOException;
    void repaint();
}
