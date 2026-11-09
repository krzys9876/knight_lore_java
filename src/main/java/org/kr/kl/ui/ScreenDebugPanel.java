package org.kr.kl.ui;

import javax.swing.*;
import java.awt.*;

public class ScreenDebugPanel extends JPanel implements DebugPanel {
    private final JTextArea text = new JTextArea();

    public ScreenDebugPanel() {
        setLayout(new BorderLayout());
        add(new JScrollPane(text), BorderLayout.CENTER);
        text.setEditable(false);
        text.setFocusable(false);
    }

    public void setText(String t) {
        this.text.setText(t);
    }

    public void append(String t) {
        text.append(t+"\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
}
