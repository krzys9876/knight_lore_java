package org.kr;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main {
    static void main() {
        IO.println("START init");
        FlatLightLaf.setup();

        KLPanel panel1 = new KLPanel();
        KLPanel panel2 = new KLPanel();
        KLPanel panel3 = new KLPanel();
        KLPanel panel4 = new KLPanel();
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
            panel1.saveImage("p3.png");
            panel1.saveImage("p4.png");
            KLPanel panel5 = new KLPanel();
            panel5.saveImage("p5.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        IO.println("END init");
    }
}

