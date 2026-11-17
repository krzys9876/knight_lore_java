package org.kr.kl.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ToolWindow {
    public static void addToggleWindow(JFrame owner, JMenu menu, String title, JComponent component, Dimension size,
                                       KeyStroke shortcut) {
        JDialog dialog = new JDialog(owner, title, false);
        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        dialog.setContentPane(component);
        dialog.setSize(size);
        dialog.setResizable(true);
        dialog.setLocationByPlatform(true);

        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(title);
        if (shortcut != null) menuItem.setAccelerator(shortcut);
        menuItem.addActionListener(e -> dialog.setVisible(menuItem.isSelected()));

        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                menuItem.setSelected(true);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                menuItem.setSelected(false);
            }
        });

        menu.add(menuItem);
    }
}

