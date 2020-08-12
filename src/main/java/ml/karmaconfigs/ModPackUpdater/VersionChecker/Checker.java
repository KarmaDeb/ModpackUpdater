package ml.karmaconfigs.ModPackUpdater.VersionChecker;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.MainFrame;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
    private final ArrayList<String> changelog = new ArrayList<>();

    /**
     * Initialize the version checker
     *
     * @param realVersion the running version
     */
    @SneakyThrows
    public Checker(String realVersion) {
        if (realVersion == null || realVersion.isEmpty()) realVersion = "0";
        version_now_str = realVersion;
        version_now_int = Integer.parseInt(version_now_str.replaceAll("[^a-zA-Z0-9]", "").replaceAll("[aA-zZ]", ""));

        URL url = new URL("https://karmaconfigs.github.io/updates/ModpackUpdater/latest.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String word;
        List<String> lines = new ArrayList<>();
        while ((word = reader.readLine()) != null) {
            if (!lines.contains(word)) {
                lines.add(word);
            }
        }

        reader.close();

        version_latest_int = Integer.parseInt(lines.get(0).replaceAll("[^a-zA-Z0-9]", "").replaceAll("[aA-zZ]", ""));
        version_latest_str = lines.get(0);
        updateURL = lines.get(1);

        for (int i = 0; i < lines.size(); i++) {
            String current = lines.get(i);
            String next = "";
            if (i + 1 < lines.size()) {
                next = lines.get(i + 1);
            }
            if (!current.equals(updateURL) && !current.equals(version_latest_str)) {
                if (next.startsWith("-")) {
                    changelog.add(current + "<br>");
                } else {
                    changelog.add(current);
                }
            }
        }
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
        update.setPreferredSize(new Dimension(500, 250));

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
                JButton openButton = new JButton("Download latest version and open launcher");
                JButton ignoreButton = new JButton("No thanks I'm fine, let me in");

                JSplitPane buttonSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, openButton, ignoreButton);

                downloadPanel.add(buttonSplitter);

                openButton.addActionListener(e -> {
                    try {
                        Desktop.getDesktop().browse(URI.create(updateURL));
                    } catch (Throwable ex) {
                        new Utils().log(ex);
                    }
                    update.dispatchEvent(new WindowEvent(update, WindowEvent.WINDOW_CLOSING));
                });

                ignoreButton.addActionListener(e -> update.dispatchEvent(new WindowEvent(update, WindowEvent.WINDOW_CLOSING)));
            } else {
                JTextArea url = new JTextArea();
                downloadPanel.add(url);

                url.setText(updateURL);
                url.setEditable(false);
            }
        } else {
            JLabel updatedInfo = new JLabel();
            updatedInfo.setText("Close this windows to open the updater");
            downloadPanel.add(updatedInfo);
        }

        JLabel changeLog = new JLabel();
        JScrollPane changelogInfo = new JScrollPane(changeLog);
        changelogInfo.setEnabled(false);

        StringBuilder info = new StringBuilder();
        for (int i = 0; i < changelog.size(); i++) {
            if (i != changelog.size() - 1) {
                info.append(changelog.get(i)).append("<br>");
            } else {
                info.append(changelog.get(i));
            }
        }
        changeLog.setText("<html><div>" + info.toString() + "</div></html>");

        JSplitPane splitterOne = new JSplitPane(JSplitPane.VERTICAL_SPLIT, labelPanel, downloadPanel);
        splitterOne.setEnabled(false);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitterOne, changelogInfo);

        update.setTitle("Modpack version checker");
        update.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        update.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        update.setLocation(dim.width / 2 - update.getSize().width / 2, dim.height / 2 - update.getSize().height / 2);
        update.setResizable(false);
        update.add(splitter);
        update.setVisible(true);

        update.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                new MainFrame().initFrame();
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
    }
}
