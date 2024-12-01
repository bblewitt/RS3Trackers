package main.java.com.bblewitt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.java.com.bblewitt.pages.QuestCapeTrackerPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class RS3Trackers {
    private static CardLayout cardLayout;
    private static JPanel mainPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RS3Trackers app = new RS3Trackers();
            app.run();
        });
    }

    public void run() {
        JFrame frame = new JFrame("RS3 Trackers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 360);
        frame.setLocationRelativeTo(null);

        ImageIcon windowIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/icon.png")));
        frame.setIconImage(windowIcon.getImage());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        BackgroundPanel panel = new BackgroundPanel("/images/rs3.png");
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("RS3 Trackers", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        JLabel usernameLabel = new JLabel("Enter Runescape Username:");
        usernameLabel.setForeground(Color.WHITE);
        JTextField usernameField = new JTextField(15);
        JButton fetchButton = new JButton("Update Hiscores");

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(fetchButton);
        centerPanel.add(inputPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));

        int[][] buttonSizes = {
                {40, 85},
                {28, 28},
                {40, 85},
                {40, 85},
                {40, 85},
                {48, 85}
        };

        for (int i = 1; i <= 6; i++) {
            JButton navButton = new JButton();

            ImageIcon originalIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/button" + i + ".png")));
            Image originalImage = originalIcon.getImage();

            Image scaledImage = originalImage.getScaledInstance(buttonSizes[i - 1][0], buttonSizes[i - 1][1], Image.SCALE_SMOOTH);

            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            navButton.setIcon(scaledIcon);

            navButton.setText("");
            navButton.setContentAreaFilled(false);
            navButton.setBorder(null);
            navButton.setFocusPainted(false);
            navButton.setPreferredSize(new Dimension(buttonSizes[i - 1][0], buttonSizes[i - 1][1]));

            navButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    navButton.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    navButton.setBorder(null);
                }
            });

            if (i == 1) {
                navButton.addActionListener(e -> {
                    frame.setSize(640, 720);
                    cardLayout.show(mainPanel, "questCapeTracker");
                });
            } else {
                int finalI = i;
                navButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Page " + finalI + " not implemented yet."));
            }
            buttonPanel.add(navButton);
        }

        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(buttonPanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        fetchButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                fetchHiscoresData(username);
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a username.");
            }
        });

        JPanel questCapeTrackerPanel = new QuestCapeTrackerPanel(e -> {
            frame.setSize(640, 360);
            cardLayout.show(mainPanel, "mainMenu");
        });

        mainPanel.add(panel, "mainMenu");
        mainPanel.add(questCapeTrackerPanel, "questCapeTracker");

        cardLayout.show(mainPanel, "mainMenu");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                this.backgroundImage = new ImageIcon(Objects.requireNonNull(getClass().getResource(imagePath))).getImage();
            } catch (Exception e) {
                System.err.println("Could not load background image: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private static void fetchHiscoresData(String username) {
        try {
            String url = "https://secure.runescape.com/m=hiscore/index_lite.ws?player=" + username;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine).append("\n");
            }
            in.close();
            connection.disconnect();

            final String outputFileName = saveHiscoresData(username, content);

            System.out.println("Data saved successfully to " + outputFileName);
        } catch (Exception e) {
            System.err.println("Error fetching or saving data: " + e.getMessage());
        }
    }

    private static String saveHiscoresData(String username, StringBuilder content) throws IOException {
        String userHome = System.getProperty("user.home");
        String outputDir = userHome + "/RS3Trackers/hiscores/";
        File hiscoresDir = new File(outputDir);

        if (!hiscoresDir.exists()) {
            if (!hiscoresDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + outputDir);
            }
        }

        String[] lines = content.toString().split("\n");
        JsonObject hiscoreData = new JsonObject();
        String[] skills = {
                "Overall", "Attack", "Defence", "Strength", "Constitution", "Ranged", "Prayer",
                "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking",
                "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer",
                "Farming", "Runecrafting", "Hunter", "Construction", "Summoning", "Dungeoneering",
                "Divination", "Invention", "Archaeology", "Necromancy"
        };
        for (int i = 0; i < lines.length && i < skills.length; i++) {
            String[] values = lines[i].split(",");
            JsonObject skillData = new JsonObject();
            skillData.addProperty("rank", values[0]);
            skillData.addProperty("level", values[1]);
            skillData.addProperty("xp", values[2]);
            hiscoreData.add(skills[i], skillData);
        }

        String outputFileName = outputDir + username + ".json";
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(outputFileName)) {
            gson.toJson(hiscoreData, writer);
        }
        return outputFileName;
    }
}
