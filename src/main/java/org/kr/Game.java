package org.kr;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Game implements Runnable {
    ScreenPanel mainPanel;
    ScreenPanel shadowPanel;
    DebugPanel debugPanel1;
    DebugPanel debugPanel2;

    // $4000-$57FF - spectrum video memory
    // $5800-$5AFF - spectrum attribute memory
    VideoMemory mainMemory;
    // $D8F3-$F0F2 - video buffer
    VideoMemory shadowMemory;
    // $5BA0-$6107 - variables
    byte[] variables = new byte[0x6107 - 0x5BA0 + 1];

    public Game(ScreenPanel mainPanel, ScreenPanel shadowPanel, DebugPanel debugPanel1, DebugPanel debugPanel2) {
        this.mainPanel = mainPanel;
        this.shadowPanel = shadowPanel;
        this.debugPanel1 = debugPanel1;
        this.debugPanel2 = debugPanel2;

        this.debugPanel1.append("Start");
        this.debugPanel2.append("Start");

        mainMemory = new VideoMemory();
        shadowMemory = new VideoMemory();

        updateMainMemory();
        updateShadowMemory();
    }

    public void updateMainMemory() { this.mainPanel.setPixelData(mainMemory.toPixels()); }
    public void updateShadowMemory() { this.shadowPanel.setPixelData(shadowMemory.toPixels()); }


    @Override
    public void run() {
        while (true) {
            debugPanel1.append(DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
