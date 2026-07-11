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
        panel3.setText("Text 1");
        DebugPanel panel4 = new DebugPanel();
        panel4.setText("Text 2");
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

        try {
            panel1.saveImage("p1.png");
            panel1.saveImage("p2.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        IO.println("END init");
    }
}

