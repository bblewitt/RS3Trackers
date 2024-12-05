package main.java.com.bblewitt.pages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.java.com.bblewitt.targets.MasterQuestCapeTrackerTargetLevels;
import main.java.com.bblewitt.util.CheckBoxTreeCellEditor;
import main.java.com.bblewitt.util.CustomTreeCellRenderer;
import main.java.com.bblewitt.util.XpTable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MasterQuestCapeTrackerPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(MasterQuestCapeTrackerPanel.class.getName());

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

    public MasterQuestCapeTrackerPanel(ActionListener backActionListener) {
        setPreferredSize(new Dimension(640, 720));
        setBackground(new Color(11, 31, 41));
        setLayout(new BorderLayout());

        JLabel masterQuestLabel = new JLabel("Master Quest Cape Tracker", SwingConstants.CENTER);
        masterQuestLabel.setFont(new Font("Arial", Font.BOLD, 30));
        masterQuestLabel.setForeground(Color.WHITE);
        masterQuestLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(masterQuestLabel, BorderLayout.NORTH);

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
            SwingUtilities.invokeLater(() -> populateMasterQuestChecklist(rightPanel, usernameDropdown));
        });

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

                        if (skillData != null && skillData.has("level") && skillData.has("rank") && skillData.has("xp")) {
                            int currentLevel = skillData.get("level").getAsInt();
                            int rank = skillData.get("rank").getAsInt();
                            int currentXp = skillData.get("xp").getAsInt();
                            int targetLevel;

                            try {
                                targetLevel = MasterQuestCapeTrackerTargetLevels.valueOf(skillName.toUpperCase()).getTargetLevel();
                            } catch (IllegalArgumentException e) {
                                showMessage("No target level found for skill: " + skillName);
                                targetLevel = 99;
                            }

                            int targetXp = XpTable.getTargetXp(targetLevel, "Invention".equalsIgnoreCase(skillName));

                            ImageIcon skillIcon = loadSkillIcon(skillName.toLowerCase());
                            if (skillIcon != null) {
                                leftPanel.add(createSkillPanel(currentLevel, targetLevel, rank, currentXp, targetXp, skillIcon));
                            } else {
                                showMessage("Icon for " + skillName + " is null.");
                            }
                        } else {
                            showMessage("Incomplete data for skill: " + skillName);
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

    private ImageIcon loadSkillIcon(String skillName) {
        try {
            String iconPath = "/images/" + skillName + ".png";
            InputStream inputStream = getClass().getResourceAsStream(iconPath);

            if (inputStream != null) {
                BufferedImage originalImage = ImageIO.read(inputStream);
                Image scaledImage = originalImage.getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                icon.setDescription(capitalizeSkillName(skillName));
                return icon;
            } else {
                showMessage("Icon not found: " + iconPath);
                return null;
            }
        } catch (IOException e) {
            showMessage("An error occurred while loading the skill icon.");
            return null;
        }
    }

    private String capitalizeSkillName(String skillName) {
        if (skillName == null || skillName.isEmpty()) {
            return skillName;
        }
        return skillName.substring(0, 1).toUpperCase() + skillName.substring(1);
    }

    private JPanel createSkillPanel(int currentLevel, int targetLevel, int rank, int currentXp, int targetXp, ImageIcon skillIcon) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(11, 31, 41));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel skillIconLabel = new JLabel(skillIcon);
        panel.add(skillIconLabel, gbc);

        gbc.gridy = 1;
        JLabel skillLevelLabel = new JLabel(currentLevel + "/" + targetLevel);
        skillLevelLabel.setForeground(Color.WHITE);
        panel.add(skillLevelLabel, gbc);

        int xpRemaining = (currentLevel >= targetLevel) ? 0 : targetXp - currentXp;
        String skillName = skillIcon.getDescription() != null ? skillIcon.getDescription() : "Unknown Skill";

        String tooltip = String.format(
                "<html>%s<br>Rank: %,d<br>Current XP: %,d<br>Target XP: %,d<br>XP Remaining: %,d</html>",
                skillName, rank, currentXp, targetXp, xpRemaining
        );
        panel.setToolTipText(tooltip);

        if (currentLevel >= targetLevel) {
            panel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
        } else {
            panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        return panel;
    }

    private void populateMasterQuestChecklist(JPanel rightPanel, JComboBox<String> usernameDropdown) {
        rightPanel.removeAll();

        Gson gson = new Gson();
        JsonObject baseMasterQuest;
        try (InputStream inputStream = getClass().getResourceAsStream("/json_files/master_quest.json")) {
            assert inputStream != null;
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                baseMasterQuest = gson.fromJson(reader, JsonObject.class);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load base master quest file", e);
            return;
        }

        String username = (String) usernameDropdown.getSelectedItem();
        JsonObject userMasterQuest = loadUserMasterQuestProgress(username);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Master Quest");

        baseMasterQuest.entrySet().forEach(entry -> {
            String year = entry.getKey();
            JsonArray masterQuestArray = entry.getValue().getAsJsonArray();

            DefaultMutableTreeNode yearNode = new DefaultMutableTreeNode(year);

            masterQuestArray.forEach(masterQuestElement -> {
                if (masterQuestElement.isJsonPrimitive()) {
                    String masterQuest = masterQuestElement.getAsString();
                    JCheckBox masterQuestCheckBox = new JCheckBox(masterQuest);
                    masterQuestCheckBox.setForeground(Color.WHITE);
                    masterQuestCheckBox.setBackground(new Color(11, 31, 41));
                    boolean completed = userMasterQuest.has(masterQuest) && userMasterQuest.get(masterQuest).getAsBoolean();
                    masterQuestCheckBox.setSelected(completed);

                    if (completed) {
                        masterQuestCheckBox.setForeground(Color.GREEN);
                    } else {
                        masterQuestCheckBox.setForeground(Color.RED);
                    }

                    masterQuestCheckBox.addActionListener(e -> {
                        userMasterQuest.addProperty(masterQuest, masterQuestCheckBox.isSelected());
                        debouncedSaveUserMasterQuestProgress(username, userMasterQuest);

                        if (masterQuestCheckBox.isSelected()) {
                            masterQuestCheckBox.setForeground(Color.GREEN);
                        } else {
                            masterQuestCheckBox.setForeground(Color.RED);
                        }
                    });

                    yearNode.add(new DefaultMutableTreeNode(masterQuestCheckBox));
                }
            });

            root.add(yearNode);
        });

        JTree tree = new JTree(root);
        tree.setBackground(new Color(11, 31, 41));
        tree.setCellRenderer(new CustomTreeCellRenderer());
        tree.setEditable(true);
        tree.setCellEditor(new CheckBoxTreeCellEditor(tree, new DefaultTreeCellRenderer()));
        tree.setShowsRootHandles(false);
        tree.setRootVisible(false);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private JsonObject loadUserMasterQuestProgress(String username) {
        File userMasterQuestFile = new File(JSON_DATA_DIR + username + "_master_quest.json");

        if (!userMasterQuestFile.exists()) {
            return new JsonObject();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(userMasterQuestFile))) {
            Gson gson = new Gson();
            return gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load master quest cape progress for user: " + username + ". Returning default data.", e);
            return new JsonObject();
        }
    }

    private void saveUserMasterQuestProgress(String username, String masterQuest, boolean completed) {
        File userMasterQuestFile = new File(JSON_DATA_DIR + username + "_master_quest.json");
        JsonObject userMasterQuest = loadUserMasterQuestProgress(username);

        userMasterQuest.addProperty(masterQuest, completed);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userMasterQuestFile))) {
            Gson gson = new Gson();
            gson.toJson(userMasterQuest, writer);
        } catch (IOException e) {
            showMessage("Failed to save master quest cape progress for user: " + username);
        }
    }

    private void debouncedSaveUserMasterQuestProgress(String username, JsonObject userMasterQuest) {
        if (saveTimer != null) {
            saveTimer.stop();
        }

        saveTimer = new Timer(1000, e -> saveAllUserMasterQuestProgress(username, userMasterQuest));
        saveTimer.setRepeats(false);
        saveTimer.start();
    }

    private void saveAllUserMasterQuestProgress(String username, JsonObject userMasterQuest) {
        for (String masterQuest : userMasterQuest.keySet()) {
            boolean completed = userMasterQuest.get(masterQuest).getAsBoolean();
            saveUserMasterQuestProgress(username, masterQuest, completed);
        }
    }
}
