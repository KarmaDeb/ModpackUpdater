package ml.karmaconfigs.ModPackUpdater.VersionChecker;

import ml.karmaconfigs.ModPackUpdater.MainFrame;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class Checker {

    private final int version_now_int;
    private final int version_latest_int;
    private final String version_now_str;
    private final String version_latest_str;
    private final String updateURL;

    /**
     * Initialize the version checker
     *
     * @param realVersion the running version
     */
    public Checker(String realVersion) throws Throwable {
        if (realVersion == null || realVersion.isEmpty()) realVersion = "1.0.0";
        version_now_str = realVersion;
        version_now_int = Integer.parseInt(version_now_str.replaceAll("[^a-zA-Z0-9]", "").replaceAll("[aA-zZ]", ""));

        URL url = new URL("https://karmaconfigs.github.io/updates/ModpackUpdater/latest.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String word;
        List<String> lines = new ArrayList<>();
        while ((word = reader.readLine()) != null)
            if (!lines.contains(word)) {
                lines.add(word);
            }
        reader.close();
        version_latest_int = Integer.parseInt(lines.get(0).replaceAll("[^a-zA-Z0-9]", "").replaceAll("[aA-zZ]", ""));
        version_latest_str = lines.get(0);
        updateURL = lines.get(1);
    }

    /**
     * Check if the jar is outdated
     *
     * @return a boolean
     */
    private boolean isOutdated() {
        if (version_now_int != version_latest_int) {
            return version_latest_int > version_now_int;
        } else {
            return false;
        }
    }

    /**
     * Show the version dialog
     */
    public final void showVersion() {
        JFrame update = new JFrame();
        update.setPreferredSize(new Dimension(500, 90));

        try {
            update.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        JPanel labelPanel = new JPanel();
        JLabel label = new JLabel();
        if (isOutdated()) {
            label.setText("ModpackUpdater is outdated ( " + version_now_str + " ) latest is: " + version_latest_str);
        } else {
            label.setText("ModpackUpdater is up to date ( " + version_now_str + " ) latest is: " + version_latest_str);
        }

        labelPanel.add(label);

        JPanel downloadPanel = new JPanel();
        if (isOutdated()) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                JButton openButton = new JButton("Download latest version");
                downloadPanel.add(openButton);

                openButton.addActionListener(e -> {
                    try {
                        Desktop.getDesktop().browse(URI.create(updateURL));
                    } catch (Throwable ex) {
                        new Utils().log(ex);
                    }
                });
            } else {
                JTextArea url = new JTextArea();
                downloadPanel.add(url);

                url.setText(updateURL);
                url.setEditable(false);
            }
        } else {
            JLabel updatedInfo = new JLabel();
            updatedInfo.setText("You can close this window");
            downloadPanel.add(updatedInfo);
        }

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, labelPanel, downloadPanel);
        splitter.setEnabled(false);

        update.setTitle("Modpack version checker");
        update.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        update.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        update.setLocation(dim.width / 2 - update.getSize().width / 2, dim.height / 2 - update.getSize().height / 2);
        update.setResizable(false);
        update.add(splitter);
        update.setVisible(true);
    }
}