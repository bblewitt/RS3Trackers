package com.bblewitt.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
    private final Icon arrowUpIcon;
    private final Icon arrowDownIcon;
    private static final Logger LOGGER = Logger.getLogger(CustomTreeCellRenderer.class.getName());

    private Icon loadScaledIcon(String path) {
        try {
            BufferedImage image = ImageIO.read(Objects.requireNonNull(getClass().getResource(path)));
            Image scaledImage = image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to scale the icon", e);
            return null;
        }
    }

    public CustomTreeCellRenderer() {
        arrowUpIcon = loadScaledIcon("/images/gold_arrow_down.png");
        arrowDownIcon = loadScaledIcon("/images/gold_arrow_up.png");
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        Component renderer = super.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);

        if (renderer instanceof JLabel label) {
            label.setBackground(selected ? new Color(30, 50, 60) : new Color(11, 31, 41));
            label.setForeground(Color.ORANGE);
            label.setOpaque(true);
        }

        if (value instanceof DefaultMutableTreeNode node) {
            if (node.getUserObject() instanceof JCheckBox checkBox) {
                checkBox.setBackground(new Color(11, 31, 41));
                return checkBox;
            }
        }

        if (!leaf) {
            if (expanded) {
                setOpenIcon(arrowUpIcon);
            } else {
                setClosedIcon(arrowDownIcon);
            }
        }

        return renderer;
    }
}
