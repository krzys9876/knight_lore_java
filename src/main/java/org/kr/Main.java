package org.kr;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main {
    static void main() {
        IO.println("START init");
        FlatLightLaf.setup();

        ScreenPanel panel1 = new ScreenPanel();
        ScreenPanel panel2 = new ScreenPanel();
        DebugPanel panel3 = new DebugPanel();
        DebugPanel panel4 = new DebugPanel();
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

        /*try {
            panel1.saveImage("p1.png");
            panel1.saveImage("p2.png");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Game game = new Game(panel1, panel2, panel3, panel4);

        IO.println("END init");
    }
}

class Game {
    ScreenPanel mainPanel;
    ScreenPanel shadowPanel;
    DebugPanel debugPanel1;
    DebugPanel debugPanel2;
    VideoMemory mainMemory;
    VideoMemory shadowMemory;

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
}

