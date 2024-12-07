package main.java.com.bblewitt.util;

import javax.swing.plaf.basic.BasicToolTipUI;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class CustomToolTipUI extends BasicToolTipUI {
    private BufferedImage backgroundImage;
    private static final Logger LOGGER = Logger.getLogger(CustomToolTipUI.class.getName());

    public CustomToolTipUI() {
        try {
            URL imageUrl = getClass().getResource("/images/tooltip_background.png");
            if (imageUrl != null) {
                backgroundImage = ImageIO.read(imageUrl);
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Tooltip background image not found.", e);
            backgroundImage = null;
        }
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        Font font = new Font("Runescape UF", Font.BOLD, 14);
        c.setFont(font);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g;

        String tipText = ((JToolTip) c).getTipText();
        if (tipText == null) return;

        JLabel dummyLabel = new JLabel(tipText);
        dummyLabel.setFont(c.getFont());
        dummyLabel.setForeground(Color.YELLOW);
        dummyLabel.setSize(dummyLabel.getPreferredSize());

        int textWidth = dummyLabel.getWidth() + 20;
        int textHeight = dummyLabel.getHeight() + 20;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, textWidth, textHeight, null);
        } else {
            g2d.setColor(new Color(60, 63, 65));
            g2d.fillRoundRect(0, 0, textWidth, textHeight, 10, 10);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(0, 0, textWidth - 1, textHeight - 1, 10, 10);

        g2d.translate(10, 10);
        dummyLabel.paint(g2d);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        String tipText = ((JToolTip) c).getTipText();
        if (tipText == null) return new Dimension(0, 0);

        JLabel dummyLabel = new JLabel(tipText);
        dummyLabel.setFont(c.getFont());
        dummyLabel.setSize(dummyLabel.getPreferredSize());

        int textWidth = dummyLabel.getWidth() + 20;
        int textHeight = dummyLabel.getHeight() + 20;

        return new Dimension(textWidth, textHeight);
    }
}
