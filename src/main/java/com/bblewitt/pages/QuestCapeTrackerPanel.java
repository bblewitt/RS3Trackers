package main.java.com.bblewitt.pages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

public class QuestCapeTrackerPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(QuestCapeTrackerPanel.class.getName());

    private String generateMessageCode() {
        return UUID.randomUUID().toString();
    }

    private void showMessage(String message) {
        String code = generateMessageCode();
        String fullMessage = "Error Code: " + code + "\n" + message;
        JOptionPane.showMessageDialog(this, fullMessage, "Error", JOptionPane.ERROR_MESSAGE);
        LOGGER.log(Level.SEVERE, "Message Code: " + code + " - " + message);
    }

    private static final String HISCORE_DATA_DIR = System.getProperty("user.home") + "/RS3Trackers/hiscores/";
    private static final String JSON_DATA_DIR = System.getProperty("user.home") + "/RS3Trackers/json_files/";

    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private Timer saveTimer;

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

        JPanel bottomCenterPanel = new JPanel();
        bottomCenterPanel.setLayout(new GridLayout(1, 2, 10, 10));
        bottomCenterPanel.setPreferredSize(new Dimension(640, 400));
        bottomCenterPanel.setBackground(new Color(11, 31, 41));

        leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(10, 3, 5, 5));
        leftPanel.setBackground(new Color(11, 31, 41));

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(11, 31, 41));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usernameDropdown.addActionListener(e -> {
            String selectedUsername = (String) usernameDropdown.getSelectedItem();
            loadSkillsData(selectedUsername);
            SwingUtilities.invokeLater(() -> populateQuestChecklist(rightPanel, usernameDropdown));
        });

        JScrollPane scrollPane = new JScrollPane(rightPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        bottomCenterPanel.add(leftPanel);
        bottomCenterPanel.add(scrollPane);
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
                                showMessage("No target level found for skill: " + skillName);
                            }

                            BufferedImage skillIcon = loadSkillIcon(skillName.toLowerCase());
                            if (skillIcon != null) {
                                ImageIcon icon = new ImageIcon(skillIcon.getScaledInstance(15, 15, Image.SCALE_SMOOTH));

                                leftPanel.add(createSkillPanel(currentLevel, targetLevel, icon));
                            } else {
                                showMessage("Icon for " + skillName + " is null.");
                            }
                        } else {
                            showMessage("No level data for skill: " + skillName);
                        }
                    }
                }

                leftPanel.revalidate();
                leftPanel.repaint();

            } catch (IOException e) {
                showMessage("An error occurred while loading the skills data.");
            }
        } else {
            showMessage("User file not found: " + userFile.getAbsolutePath());
        }
    }

    private BufferedImage loadSkillIcon(String skillName) {
        try {
            String iconPath = "/images/" + skillName + ".png";

            InputStream inputStream = getClass().getResourceAsStream(iconPath);

            if (inputStream != null) {
                return ImageIO.read(inputStream);
            } else {
                showMessage("Icon not found: " + iconPath);
                return null;
            }
        } catch (IOException e) {
            showMessage("An error occurred while loading the skill icon.");
            return null;
        }
    }

    private JPanel createSkillPanel(int currentLevel, int targetLevel, ImageIcon icon) {
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

    private void populateQuestChecklist(JPanel rightPanel, JComboBox<String> usernameDropdown) {
        rightPanel.removeAll();

        Gson gson = new Gson();
        JsonObject baseQuests;
        try (InputStream inputStream = getClass().getResourceAsStream("/json_files/quests.json")) {
            assert inputStream != null;
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                baseQuests = gson.fromJson(reader, JsonObject.class);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load base quests file", e);
            return;
        }

        String username = (String) usernameDropdown.getSelectedItem();
        JsonObject userQuests = loadUserQuestProgress(username);

        baseQuests.entrySet().forEach(entry -> {
            String year = entry.getKey();
            JsonArray questsArray = entry.getValue().getAsJsonArray();

            JLabel yearLabel = new JLabel(year);
            yearLabel.setForeground(Color.WHITE);
            rightPanel.add(yearLabel);

            questsArray.forEach(questElement -> {
                if (questElement.isJsonPrimitive()) {
                    String quest = questElement.getAsString();
                    JCheckBox questCheckBox = new JCheckBox(quest);
                    questCheckBox.setForeground(Color.WHITE);
                    questCheckBox.setBackground(new Color(11, 31, 41));
                    boolean completed = userQuests.has(quest) && userQuests.get(quest).getAsBoolean();
                    questCheckBox.setSelected(completed);

                    if (completed) {
                        questCheckBox.setForeground(Color.GREEN);
                    } else {
                        questCheckBox.setForeground(Color.RED);
                    }

                    questCheckBox.addActionListener(e -> {
                        userQuests.addProperty(quest, questCheckBox.isSelected());
                        debouncedSaveUserQuestProgress(username, userQuests);

                        if (questCheckBox.isSelected()) {
                            questCheckBox.setForeground(Color.GREEN);
                        } else {
                            questCheckBox.setForeground(Color.RED);
                        }
                    });

                    rightPanel.add(questCheckBox);
                }
            });
        });

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private JsonObject loadUserQuestProgress(String username) {
        File userQuestFile = new File(JSON_DATA_DIR + username + "_quests.json");

        if (!userQuestFile.exists()) {
            return new JsonObject();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(userQuestFile))) {
            Gson gson = new Gson();
            return gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load quest progress for user: " + username + ". Returning default data.", e);
            return new JsonObject();
        }
    }

    private void saveUserQuestProgress(String username, String quest, boolean completed) {
        File jsonDir = new File(JSON_DATA_DIR);
        if (!jsonDir.exists()) {
            boolean directoriesCreated = jsonDir.mkdirs();
            if (directoriesCreated) {
                System.out.println("Directories created successfully.");
            } else {
                showMessage("Failed to create directories. Please check permissions or path.");
            }
        } else {
            System.out.println("Directories already exist.");
        }

        File userQuestFile = new File(JSON_DATA_DIR + username + "_quests.json");
        JsonObject userQuests = loadUserQuestProgress(username);

        userQuests.addProperty(quest, completed);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userQuestFile))) {
            Gson gson = new Gson();
            gson.toJson(userQuests, writer);
        } catch (IOException e) {
            showMessage("Failed to save quest progress for user: " + username);
        }
    }

    private void debouncedSaveUserQuestProgress(String username, JsonObject userQuests) {
        if (saveTimer != null) {
            saveTimer.stop();
        }

        saveTimer = new Timer(1000, e -> saveAllUserQuestProgress(username, userQuests));
        saveTimer.setRepeats(false);
        saveTimer.start();
    }

    private void saveAllUserQuestProgress(String username, JsonObject userQuests) {
        for (String quest : userQuests.keySet()) {
            boolean completed = userQuests.get(quest).getAsBoolean();
            saveUserQuestProgress(username, quest, completed);
        }
    }
}
