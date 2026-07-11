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
        TextPanel panel3 = new TextPanel();
        panel3.setText("Text 1");
        TextPanel panel4 = new TextPanel();
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

class TextPanel extends JPanel {
    private final JTextArea text = new JTextArea();

    public TextPanel() {
        setLayout(new BorderLayout());
        add(new JScrollPane(text), BorderLayout.CENTER);
        text.setEditable(false);
    }

    public void setText(String text) {
        this.text.setText(text);
    }
}

