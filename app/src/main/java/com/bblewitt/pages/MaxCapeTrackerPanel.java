package com.bblewitt.pages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.bblewitt.targets.MaxCapeTrackerTargetLevels;
import com.bblewitt.util.CheckBoxTreeCellEditor;
import com.bblewitt.util.CustomToolTip;
import com.bblewitt.util.CustomTreeCellRenderer;
import com.bblewitt.util.XpTable;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MaxCapeTrackerPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(MaxCapeTrackerPanel.class.getName());
    private static final String HISCORE_DATA_DIR = System.getProperty("user.home") + "/RS3Trackers/hiscores/";
    private static final String JSON_DATA_DIR = System.getProperty("user.home") + "/RS3Trackers/json_files/";

    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private Timer saveTimer;
    private final JPanel skillsProgressPanel;
    private final JLabel skillsProgressLabel;
    private final JLabel taskProgressLabel;
    private int completedTasks = 0;
    private int totalTasks = 0;
    final List<JCheckBox> taskCheckBoxes = new ArrayList<>();
    private boolean isSkillsSoundPlaying = false;

    private String generateMessageCode() {
        return UUID.randomUUID().toString();
    }

    private void showMessage(String message) {
        String code = generateMessageCode();
        String fullMessage = "Error Code: " + code + "\n" + message;
        JOptionPane.showMessageDialog(this, fullMessage, "Error", JOptionPane.ERROR_MESSAGE);
        LOGGER.log(Level.SEVERE, "Message Code: " + code + " - " + message);
    }

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

    public MaxCapeTrackerPanel(ActionListener backActionListener) {
        setPreferredSize(new Dimension(640, 720));
        setBackground(new Color(11, 31, 41));
        setLayout(new BorderLayout());

        ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/max_cape_tracker.png")));
        JLabel imageLabel = new JLabel(imageIcon, SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(imageLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

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

        skillsProgressPanel = new JPanel(new BorderLayout());
        skillsProgressPanel.setBackground(new Color(11, 31, 41));
        skillsProgressLabel = new JLabel("Skills - 0/0", SwingConstants.CENTER);
        skillsProgressLabel.setForeground(Color.WHITE);
        skillsProgressLabel.setFont(new Font("Runescape UF", Font.BOLD, 16));
        skillsProgressPanel.add(skillsProgressLabel, BorderLayout.CENTER);
        skillsProgressPanel.setOpaque(false);

        leftPanel = new JPanel() {
            @Override
            public void doLayout() {
                super.doLayout();
                if (getComponentCount() > 0 && getComponent(0) instanceof JPanel) {
                    getComponent(0).setBounds(0, 0, getWidth(), getHeight() / 10);
                }
            }
        };
        leftPanel.add(new JPanel() {{
            setOpaque(false);
        }});
        leftPanel.add(skillsProgressPanel);
        leftPanel.add(new JPanel() {{
            setOpaque(false);
        }});
        leftPanel.setLayout(new GridLayout(11, 3, 5, 5));
        leftPanel.setBackground(new Color(11, 31, 41));

        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBackground(new Color(11, 31, 41));
        rightPanel.setOpaque(false);

        taskProgressLabel = new JLabel("Task List - 0/0", SwingConstants.CENTER);
        taskProgressLabel.setForeground(Color.WHITE);
        taskProgressLabel.setFont(new Font("Runescape UF", Font.BOLD, 16));
        taskProgressLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(taskProgressLabel, BorderLayout.NORTH);

        usernameDropdown.addActionListener(e -> {
            String selectedUsername = (String) usernameDropdown.getSelectedItem();
            loadSkillsData(selectedUsername);
            SwingUtilities.invokeLater(() -> populateMaxCapeChecklist(rightPanel, usernameDropdown));
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
                leftPanel.add(skillsProgressPanel);
                leftPanel.add(new JPanel() {{
                    setOpaque(false);
                }});
                leftPanel.add(new JPanel() {{
                    setOpaque(false);
                }});

                int totalSkills = 0;
                int targetsReached = 0;
                int completedSkills = 0;

                for (String[] row : SKILL_ORDER) {
                    for (String skillName : row) {
                        totalSkills++;
                        JsonObject skillData = jsonObject.getAsJsonObject(skillName);

                        if (skillData != null && skillData.has("level") && skillData.has("rank") && skillData.has("xp")) {
                            int currentLevel = skillData.get("level").getAsInt();
                            int targetLevel;

                            try {
                                targetLevel = MaxCapeTrackerTargetLevels.valueOf(skillName.toUpperCase()).getTargetLevel();
                            } catch (IllegalArgumentException e) {
                                showMessage("No target level found for skill: " + skillName);
                                targetLevel = 99;
                            }

                            if (currentLevel >= targetLevel) {
                                targetsReached++;
                                completedSkills++;
                            }

                            int targetXp = XpTable.getTargetXp(targetLevel, "Invention".equalsIgnoreCase(skillName));
                            ImageIcon skillIcon = loadSkillIcon(skillName.toLowerCase());
                            if (skillIcon != null) {
                                leftPanel.add(createSkillPanel(currentLevel, targetLevel, skillData.get("rank").getAsInt(),
                                        skillData.get("xp").getAsInt(), targetXp, skillIcon));
                            } else {
                                showMessage("Icon for " + skillName + " is null.");
                            }
                        } else {
                            showMessage("Incomplete data for skill: " + skillName);
                        }
                    }
                }

                skillsProgressLabel.setText(String.format("Skills - %d/%d", targetsReached, totalSkills));
                leftPanel.revalidate();
                leftPanel.repaint();

                if (completedSkills == totalSkills && !isSkillsSoundPlaying) {
                    playSkillsCompletionSound();
                }

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
        JPanel panel = new JPanel() {
            @Override
            public JToolTip createToolTip() {
                return new CustomToolTip();
            }
        };

        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(11, 31, 41));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        JLabel skillIconLabel = new JLabel(skillIcon);
        panel.add(skillIconLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        JLabel skillLevelLabel = new JLabel(currentLevel + " / " + targetLevel);
        skillLevelLabel.setForeground(Color.WHITE);
        skillLevelLabel.setFont(new Font("Runescape UF", Font.PLAIN, 14));
        panel.add(skillLevelLabel, gbc);

        int xpRemaining = (currentLevel >= targetLevel) ? 0 : targetXp - currentXp;
        String xpRemainingText = (xpRemaining == 0) ? "Done" : String.format("%,d", xpRemaining);
        String skillName = skillIcon.getDescription() != null ? skillIcon.getDescription() : "Unknown Skill";

        String tooltip = String.format(
                "<html>%s<br>Rank: %,d<br>Current XP: %,d<br>Target XP: %,d<br>XP Remaining: %s</html>",
                skillName, rank, currentXp, targetXp, xpRemainingText
        );
        panel.setToolTipText(tooltip);

        if (currentLevel >= targetLevel) {
            panel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
        } else {
            panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        return panel;
    }

    private void populateMaxCapeChecklist(JPanel rightPanel, JComboBox<String> usernameDropdown) {
        Gson gson = new Gson();
        JsonObject baseTaskLists;
        try (InputStream inputStream = getClass().getResourceAsStream("/json_files/max_cape.json")) {
            assert inputStream != null;
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                baseTaskLists = gson.fromJson(reader, JsonObject.class);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load base max cape file", e);
            return;
        }

        String username = (String) usernameDropdown.getSelectedItem();
        JsonObject userTaskLists = loadUserMaxCapeProgress(username);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Max Cape");

        totalTasks = 0;
        completedTasks = 0;

        baseTaskLists.entrySet().forEach(entry -> {
            String task = entry.getKey();
            JsonArray taskListArray = entry.getValue().getAsJsonArray();

            DefaultMutableTreeNode taskNode = new DefaultMutableTreeNode(task);

            taskListArray.forEach(taskListElement -> {
                if (taskListElement.isJsonPrimitive()) {
                    String taskList = taskListElement.getAsString();
                    JCheckBox taskListCheckBox = new JCheckBox(taskList);
                    taskCheckBoxes.add(taskListCheckBox);
                    taskListCheckBox.setForeground(Color.WHITE);
                    taskListCheckBox.setBackground(new Color(11, 31, 41));
                    boolean completed = userTaskLists.has(taskList) && userTaskLists.get(taskList).getAsBoolean();
                    taskListCheckBox.setSelected(completed);

                    totalTasks++;
                    if (completed) {
                        completedTasks++;
                        taskListCheckBox.setForeground(Color.GREEN);
                    } else {
                        taskListCheckBox.setForeground(Color.RED);
                    }

                    taskListCheckBox.addActionListener(e -> {
                        userTaskLists.addProperty(taskList, taskListCheckBox.isSelected());
                        debouncedSaveUserMaxCapeProgress(username, userTaskLists);

                        if (taskListCheckBox.isSelected()) {
                            completedTasks++;
                            taskListCheckBox.setForeground(Color.GREEN);
                        } else {
                            completedTasks--;
                            taskListCheckBox.setForeground(Color.RED);
                        }

                        taskProgressLabel.setText(String.format("Task List - %d/%d", completedTasks, totalTasks));

                        if (completedTasks == totalTasks) {
                            playTaskListCompletionSound();
                        }
                    });

                    taskNode.add(new DefaultMutableTreeNode(taskListCheckBox));
                }
            });

            root.add(taskNode);
        });

        taskProgressLabel.setText(String.format("Task List - %d/%d", completedTasks, totalTasks));

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
        rightPanel.add(taskProgressLabel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private JsonObject loadUserMaxCapeProgress(String username) {
        File userMaxCapeFile = new File(JSON_DATA_DIR + username + "_max_cape.json");

        if (!userMaxCapeFile.exists()) {
            return new JsonObject();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(userMaxCapeFile))) {
            Gson gson = new Gson();
            return gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load max cape progress for user: " + username + ". Returning default data.", e);
            return new JsonObject();
        }
    }

    private void saveUserMaxCapeProgress(String username, String taskList, boolean completed) {
        File userMaxCapeFile = new File(JSON_DATA_DIR + username + "_max_cape.json");
        JsonObject userTaskLists = loadUserMaxCapeProgress(username);

        userTaskLists.addProperty(taskList, completed);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userMaxCapeFile))) {
            Gson gson = new Gson();
            gson.toJson(userTaskLists, writer);
        } catch (IOException e) {
            showMessage("Failed to save max cape progress for user: " + username);
        }
    }

    private void debouncedSaveUserMaxCapeProgress(String username, JsonObject userTaskLists) {
        if (saveTimer != null) {
            saveTimer.stop();
        }

        saveTimer = new Timer(1000, e -> saveAllUserMaxCapeProgress(username, userTaskLists));
        saveTimer.setRepeats(false);
        saveTimer.start();
    }

    private void saveAllUserMaxCapeProgress(String username, JsonObject userTaskLists) {
        for (String taskList : userTaskLists.keySet()) {
            boolean completed = userTaskLists.get(taskList).getAsBoolean();
            saveUserMaxCapeProgress(username, taskList, completed);
        }
    }

    private void playSkillsCompletionSound() {
        if (isSkillsSoundPlaying) {
            return;
        }
        isSkillsSoundPlaying = true;

        try {
            String soundPath = "/sounds/skill_list.wav";
            InputStream soundStream = getClass().getResourceAsStream(soundPath);

            if (soundStream != null) {
                BufferedInputStream bufferedStream = new BufferedInputStream(soundStream);
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(bufferedStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        isSkillsSoundPlaying = false;
                        clip.close();
                    }
                });

                clip.start();
            } else {
                LOGGER.log(Level.WARNING, "Sound file not found: " + soundPath);
                isSkillsSoundPlaying = false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error playing skills completion sound", e);
            isSkillsSoundPlaying = false;
        }
    }

    private void playTaskListCompletionSound() {
        if (isSkillsSoundPlaying) {
            return;
        }
        try {
            String soundPath = "/sounds/task_list.wav";
            InputStream soundStream = getClass().getResourceAsStream(soundPath);

            if (soundStream != null) {
                BufferedInputStream bufferedStream = new BufferedInputStream(soundStream);
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(bufferedStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                clip.start();
            } else {
                LOGGER.log(Level.WARNING, "Sound file not found: " + soundPath);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error playing task list sound", e);
        }
    }
}