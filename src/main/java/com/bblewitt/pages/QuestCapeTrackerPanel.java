package main.java.com.bblewitt.pages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class QuestCapeTrackerPanel extends JPanel {

    public QuestCapeTrackerPanel(ActionListener backActionListener) {
        // Set panel size and layout
        setPreferredSize(new Dimension(640, 720));
        setBackground(new Color(11, 31, 41));
        setLayout(new BorderLayout());

        // Title Label at the top center
        JLabel questLabel = new JLabel("Quest Cape Tracker", SwingConstants.CENTER);
        questLabel.setFont(new Font("Arial", Font.BOLD, 30));
        questLabel.setForeground(Color.BLACK);  // Title color
        questLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));  // Add padding
        add(questLabel, BorderLayout.NORTH);

        // Create the center panel with two rectangles
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2, 10, 10)); // 1 row, 2 columns

        // Create two rectangles (JPanel with background colors)
        JPanel rectangle1 = new JPanel();
        rectangle1.setBackground(Color.DARK_GRAY);
        rectangle1.setPreferredSize(new Dimension(200, 300)); // Size of rectangle

        JPanel rectangle2 = new JPanel();
        rectangle2.setBackground(Color.GRAY);
        rectangle2.setPreferredSize(new Dimension(200, 300)); // Size of rectangle

        // Add the rectangles to the center panel
        centerPanel.add(rectangle1);
        centerPanel.add(rectangle2);

        // Add the centerPanel to the center of the main panel
        add(centerPanel, BorderLayout.CENTER);

        // Back Button at the bottom
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(backActionListener);
        backButton.setPreferredSize(new Dimension(200, 40));
        add(backButton, BorderLayout.SOUTH);

        // Add the back button to the bottom of the panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);  // Make sure the bottom panel doesn't obscure anything
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
