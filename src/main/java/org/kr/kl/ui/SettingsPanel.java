package org.kr.kl.ui;

public class SettingsPanel extends GamePanel {
    public void doClick(int x, int y) {
        IO.println(String.format("(%d,%d)", x, y));
    }
}
