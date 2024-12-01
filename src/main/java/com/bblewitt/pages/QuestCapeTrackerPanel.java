package main.java.com.bblewitt.pages;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.java.com.bblewitt.targets.QuestCapeTrackerTargetLevels;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class QuestCapeTrackerPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(QuestCapeTrackerPanel.class.getName());

    private static final String HISCORE_DATA_DIR = System.getProperty("user.home") + "/RS3Trackers/hiscores/";

    private final JPanel leftPanel;

    private static final String[][] SKILL_ORDER = {
            {"Attack", "Constitution", "Mining"},
            {"Strength", "Agility", "Smithing"},
            {"Defence", "Herblore", "Fishing"},
            {"Ranged", "Thieving", "Cooking"},
            {"Prayer", "Crafting", "Firemaking"},
            {"Magic", "Fletching", "Woodcutting"},
            {"Runecrafting", "Slayer", "Farming"},
            {"Construction", "Hunter", "Summoning"},
            {"Dungeoneering", "Divination", "Invention"},
            {"Archaeology", "Necromancy"}
    };

    public QuestCapeTrackerPanel(ActionListener backActionListener) {
        setPreferredSize(new Dimension(640, 720));
        setBackground(new Color(11, 31, 41));
        setLayout(new BorderLayout());

        JLabel questLabel = new JLabel("Quest Cape Tracker", SwingConstants.CENTER);
        questLabel.setFont(new Font("Arial", Font.BOLD, 30));
        questLabel.setForeground(Color.WHITE);
        questLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(questLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(11, 31, 41));
        add(centerPanel, BorderLayout.CENTER);

        JPanel topCenterPanel = new JPanel();
        topCenterPanel.setPreferredSize(new Dimension(640, 1));
        topCenterPanel.setOpaque(false);
        List<String> usernames = getAvailableUsernames();
        JComboBox<String> usernameDropdown = new JComboBox<>(usernames.toArray(new String[0]));
        usernameDropdown.setPreferredSize(new Dimension(200, 30));
        topCenterPanel.add(new JLabel("Select Username:")).setForeground(Color.WHITE);
        topCenterPanel.add(usernameDropdown);
        centerPanel.add(topCenterPanel);

        usernameDropdown.addActionListener(e -> {
            String selectedUsername = (String) usernameDropdown.getSelectedItem();
            loadSkillsData(selectedUsername);
        });

        JPanel bottomCenterPanel = new JPanel();
        bottomCenterPanel.setLayout(new GridLayout(1, 2, 10, 10));
        bottomCenterPanel.setPreferredSize(new Dimension(640, 400));
        bottomCenterPanel.setBackground(Color.WHITE);

        leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(10, 3, 5, 5));
        leftPanel.setBackground(new Color(11, 31, 41));

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.GRAY);
        rightPanel.setPreferredSize(new Dimension(200, 300));

        bottomCenterPanel.add(leftPanel);
        bottomCenterPanel.add(rightPanel);
        centerPanel.add(bottomCenterPanel);

        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(backActionListener);
        backButton.setPreferredSize(new Dimension(200, 40));
        add(backButton, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private List<String> getAvailableUsernames() {
        List<String> usernames = new ArrayList<>();
        File hiscoresDir = new File(HISCORE_DATA_DIR);
        if (hiscoresDir.exists() && hiscoresDir.isDirectory()) {
            File[] files = hiscoresDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    String username = file.getName().replace(".json", "");
                    usernames.add(username);
                }
            }
        }
        return usernames;
    }

    private void loadSkillsData(String selectedUsername) {
        File userFile = new File(HISCORE_DATA_DIR + selectedUsername + ".json");

        if (userFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                String data = sb.toString();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(data, JsonObject.class);

                leftPanel.removeAll();

                for (String[] row : SKILL_ORDER) {
                    for (String skillName : row) {
                        JsonObject skillData = jsonObject.getAsJsonObject(skillName);

                        if (skillData != null && skillData.has("level")) {
                            int currentLevel = skillData.get("level").getAsInt();

                            int targetLevel = 99;
                            try {
                                targetLevel = QuestCapeTrackerTargetLevels.valueOf(skillName.toUpperCase()).getTargetLevel();
                            } catch (IllegalArgumentException e) {
                                System.out.println("No target level found for skill: " + skillName);
                            }

                            BufferedImage skillIcon = loadSkillIcon(skillName.toLowerCase());
                            if (skillIcon != null) {
                                ImageIcon icon = new ImageIcon(skillIcon.getScaledInstance(15, 15, Image.SCALE_SMOOTH));

                                leftPanel.add(createSkillPanel(skillName, currentLevel, targetLevel, icon));
                            } else {
                                System.out.println("Icon for " + skillName + " is null.");
                            }
                        } else {
                            System.out.println("No level data for skill: " + skillName);
                        }
                    }
                }

                leftPanel.revalidate();
                leftPanel.repaint();

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An error occurred", e);
            }
        } else {
            System.out.println("User file not found: " + userFile.getAbsolutePath());
        }
    }

    private BufferedImage loadSkillIcon(String skillName) {
        try {
            String iconPath = "/images/" + skillName + ".png";

            InputStream inputStream = getClass().getResourceAsStream(iconPath);

            if (inputStream != null) {
                return ImageIO.read(inputStream);
            } else {
                System.out.println("Icon not found: " + iconPath);
                return null;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred", e);
            return null;
        }
    }

    private JPanel createSkillPanel(String skillName, int currentLevel, int targetLevel, ImageIcon icon) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(11, 31, 41));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel skillIconLabel = new JLabel(icon);
        panel.add(skillIconLabel, gbc);

        gbc.gridy = 1;

        JLabel skillLevelLabel = new JLabel(currentLevel + "/" + targetLevel);
        skillLevelLabel.setForeground(Color.WHITE);
        panel.add(skillLevelLabel, gbc);

        if (currentLevel >= targetLevel) {
            panel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
        } else {
            panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }
        return panel;
    }
}
