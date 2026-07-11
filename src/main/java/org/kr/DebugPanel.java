package org.kr;

import javax.swing.*;
import java.awt.*;

public class DebugPanel extends JPanel {
    private final JTextArea text = new JTextArea();

    public DebugPanel() {
        setLayout(new BorderLayout());
        add(new JScrollPane(text), BorderLayout.CENTER);
        text.setEditable(false);
    }

    public void setText(String t) {
        this.text.setText(t);
    }

    public void append(String t) {
        text.append(t+"\n");
    }
}
