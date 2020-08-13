package ml.karmaconfigs.ModPackUpdater;

import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.Launcher.Launch;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class LaunchFrame {

    public static JFrame launcherFrame;

    private static final JTextField name = new JTextField("Player");
    private static final JTextField minMemory = new JTextField("1024");

    private static final JButton launch = new JButton("Launch");

    private final Modpack modpack;

    public LaunchFrame(Modpack modpack) {
        this.modpack = modpack;
        if (launcherFrame == null) {
            launcherFrame = new JFrame();

            launcherFrame.setTitle("Launch modpack " + modpack.getName());
            launcherFrame.setPreferredSize(new Dimension(500, 300));
            try {
                launcherFrame.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
            } catch (Throwable e) {
                e.printStackTrace();
            }

            JSplitPane launchParameters = new JSplitPane(JSplitPane.VERTICAL_SPLIT, name, minMemory);
            launchParameters.setEnabled(false);
            JPanel panel = new JPanel();
            panel.add(launch);
            JSplitPane launchSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, launchParameters, launch);
            launchSplitter.setEnabled(false);
            launchSplitter.setDividerSize(190);

            launcherFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            launcherFrame.pack();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            launcherFrame.setLocation(dim.width / 2 - launcherFrame.getSize().width / 2, dim.height / 2 - launcherFrame.getSize().height / 2);
            launcherFrame.setResizable(false);
            launcherFrame.add(launchSplitter);
        }
        minMemory.setText(FilesUtilities.getConfig.getClientMemory());
        name.setText(FilesUtilities.getConfig.getClientName());
        launcherFrame.setVisible(true);
    }

    public final void display() {
        if (modpack.exists()) {
            minMemory.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                        if (!Character.isDigit(e.getKeyChar())) {
                            e.consume();
                        }
                }
            });

            name.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (name.getText().length() >= 14) {
                        e.consume();
                    }
                    if (!e.isConsumed()) {
                        if (!Character.isLetterOrDigit(e.getKeyChar()) && !String.valueOf(e.getKeyChar()).equals("_")) {
                            e.consume();
                        }
                    }
                }
            });

            minMemory.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    String correct = onlyNumbers(minMemory.getText());
                    FilesUtilities.getConfig.saveClientMem(correct);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    String correct = onlyNumbers(minMemory.getText());
                    FilesUtilities.getConfig.saveClientMem(correct);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    String correct = onlyNumbers(minMemory.getText());
                    FilesUtilities.getConfig.saveClientMem(correct);
                }
            });

            name.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    String correct = noSpecial(name.getText());
                    FilesUtilities.getConfig.saveClientName(correct);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    String correct = noSpecial(name.getText());
                    FilesUtilities.getConfig.saveClientName(correct);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    String correct = noSpecial(name.getText());
                    FilesUtilities.getConfig.saveClientName(correct);
                }
            });

            launch.addActionListener(e -> {
                new Launch();
                launcherFrame.setVisible(false);
            });
        } else {
            JFrame errorFrame = new JFrame();
            errorFrame.setPreferredSize(new Dimension(500, 100));

            try {
                errorFrame.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
            } catch (Throwable e) {
                e.printStackTrace();
            }

            JPanel infoPanel = new JPanel();
            JLabel error = new JLabel();
            error.setText("<html>The current modpack seems<br>to be null, make sure it's downloaded<br>and installed and try again</html>");

            infoPanel.add(error);

            errorFrame.setTitle("Something went wrong...");
            errorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            errorFrame.pack();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            errorFrame.setLocation(dim.width / 2 - errorFrame.getSize().width / 2, dim.height / 2 - errorFrame.getSize().height / 2);
            errorFrame.setResizable(false);
            errorFrame.add(infoPanel);
            errorFrame.setVisible(true);
        }
    }

    private String onlyNumbers(String original) {
        if (!original.isEmpty()) {
            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < original.length(); i++) {
                String letter = String.valueOf(original.charAt(i));
                if (letter.matches(".*[0-9].*")) {
                    formatted.append(letter);
                }
            }

            return formatted.toString();
        } else {
            return "1024";
        }
    }

    private String noSpecial(String name) {
        if (!name.isEmpty()) {
            StringBuilder formatted = new StringBuilder();

            int max = 14;
            if (name.length() < max) {
                max = name.length();
            }

            for (int i = 0; i < max; i++) {
                String letter = String.valueOf(name.charAt(i));
                if (letter.matches(".*[aA-zZ].*") || letter.matches(".*[0-9].*") || letter.matches("_")) {
                    formatted.append(letter);
                }
            }

            return formatted.toString();
        } else {
            return "Player";
        }
    }
}
