package org.kr.kl.ui;

import java.io.IOException;

public interface GamePanel {
    void setPixelData(int[] pixelData);
    void saveImage(String path) throws IOException;
    void repaint();
}
