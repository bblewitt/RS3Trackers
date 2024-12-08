package com.bblewitt.util;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class CheckBoxTreeCellEditor extends DefaultTreeCellEditor {

    public CheckBoxTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
        if (value instanceof DefaultMutableTreeNode node) {
            Object userObject = node.getUserObject();

            if (userObject instanceof JCheckBox checkBox) {
                checkBox.setBackground(selected ? new Color(30, 50, 60) : new Color(11, 31, 41));
                return checkBox;
            }
        }

        return super.getTreeCellEditorComponent(tree, value, selected, expanded, leaf, row);
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent mouseEvent) {
            TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

            if (path != null) {
                Object lastPathComponent = path.getLastPathComponent();
                if (lastPathComponent instanceof DefaultMutableTreeNode node) {
                    return node.getUserObject() instanceof JCheckBox;
                }
            }
        }

        return false;
    }
}
