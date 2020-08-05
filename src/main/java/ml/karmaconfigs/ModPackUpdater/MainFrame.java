package ml.karmaconfigs.ModPackUpdater;

import com.formdev.flatlaf.*;
import ml.karmaconfigs.ModPackUpdater.Utils.*;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.Config;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Downloader;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Installer;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.ListMods;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.VersionChecker.Checker;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainFrame {

    public static String version = MainFrame.class.getPackage().getImplementationVersion();

    public static JFrame frame;

    public static JLabel bPane;
    public static JLabel barLabel;

    public static JScrollPane jsp;

    public static JProgressBar bar;

    public static File mcFolder;

    /**
     * Initialize the launcher GUI
     */
    public void initFrame() {
        Utils utils = new Utils();
        mcFolder = FilesUtilities.getConfig.getMinecraftDir();
        frame = new JFrame() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1600, 900);
            }
        };

        try {
            frame.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        bar = new JProgressBar();
        bar.setPreferredSize(new Dimension(frame.getWidth(), 30));
        barLabel = new JLabel();

        barLabel.setHorizontalAlignment(JLabel.CENTER);
        barLabel.setVerticalAlignment(JLabel.CENTER);
        barLabel.setBorder(new EmptyBorder(15, 15, 15, 15));

        barLabel.setText("Download status bar");
        bar.add(barLabel);
        bar.setValue(0);

        //Combo boxes
        JComboBox<String> modpacks = new JComboBox<>(Modpack.listing.modpacks());
        /*
        Nobody should use "System default" since it looks pretty bad and honestly, it works really slow :)
        Should I remove it in a future version?
        */
        JComboBox<String> theme = new JComboBox<>(new String[]{"Light", "Dark", "System default"});
        theme.setSelectedItem(FilesUtilities.getConfig.getTheme());

        //Check boxes
        JCheckBox hardInstall = new JCheckBox("Hard install");

        //Text areas
        JTextArea dlURL = new JTextArea("Modpack download.txt url");
        dlURL.setText(FilesUtilities.getConfig.getDownloadURL());

        //Buttons
        JButton download = new JButton("[Download & Install] modpack");
        JButton install = new JButton("Install modpack");
        JButton list = new JButton("List mods");
        JButton createPanel = new JButton("Creator panel");

        //Panels
        JPanel installPanel = new JPanel();
        JPanel optionsPanel = new JPanel();
        JPanel managerPanel = new JPanel();
        JPanel themePane = new JPanel();
        JPanel modpackPane = new JPanel();

        //Labels
        bPane = new JLabel();
        JLabel themeText = new JLabel("Theme");
        JLabel packText = new JLabel("Modpack");

        //Scroll panes
        jsp = new JScrollPane(bPane);

        //Split panes
        JSplitPane installSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, install, hardInstall);
        JSplitPane options = new JSplitPane(JSplitPane.VERTICAL_SPLIT, download, installPanel);
        JSplitPane manager = new JSplitPane(JSplitPane.VERTICAL_SPLIT, list, createPanel);

        JSplitPane leftTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionsPanel, managerPanel);

        JSplitPane themeLabel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, themeText, theme);
        JSplitPane modpackLabel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, packText, modpacks);

        JSplitPane managerSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, themePane, modpackPane);
        JSplitPane optionSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftTop, managerSplitter);

        JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionSplitter, new JPanel());
        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dlURL, jsp);

        JSplitPane barSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bar, barSplitter);

        //Disable the split panes resizing...
        disable(installSplit, options, manager, leftTop, themeLabel, modpackLabel, managerSplitter, optionSplitter, left, right, barSplitter, splitter);

        //Make all the panels work and display the correct way
        //Yes, I use brackets to separate it '-'
        {
            { //This is the most important
                jsp.getVerticalScrollBar().setUnitIncrement(100);
            }
            { //Split panes options
                //Grouping left-top options
                leftTop.setDividerSize(-5);

                //This actually sucks...
                themeLabel.setDividerSize(19);

                //Grouping left-bottom options
                managerSplitter.setDividerSize(-5);
            }

            {
                syncSize(installSplit, download, install, list, createPanel, theme, modpacks);
            }

            { //Panels options
                installPanel.add(installSplit);
                optionsPanel.add(options);
                managerPanel.add(manager);
                themePane.add(themeLabel);
                modpackPane.add(modpackLabel);
            }
        }

        utils.setDebug(utils.rgbColor("Debug pane", 125, 255, 195), false);

        if (FilesUtilities.getConfig.getTheme().equals("System default")) {
            bPane.setOpaque(true);
            bPane.setBackground(Color.GRAY);
        } else {
            bPane.setOpaque(true);
            bPane.setBackground(Color.DARK_GRAY);
        }

        frame.setTitle("ModPack updater by KarmaDev " + version);
        frame.add(splitter);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);

        theme.addActionListener(e -> {
            String oldTheme = FilesUtilities.getConfig.getTheme();
            try {
                if (theme.getSelectedItem() != null) {
                    String themeName = theme.getSelectedItem().toString();

                    if (!oldTheme.equals(themeName)) {
                        switch (themeName) {
                            case "Light":
                                bPane.setOpaque(true);
                                bPane.setBackground(Color.DARK_GRAY);
                                UIManager.setLookAndFeel(FlatLightLaf.class.getCanonicalName());
                                break;
                            case "Dark":
                                bPane.setOpaque(true);
                                bPane.setBackground(Color.DARK_GRAY);
                                UIManager.setLookAndFeel(FlatDarkLaf.class.getCanonicalName());
                                break;
                            case "System default":
                                bPane.setOpaque(true);
                                bPane.setBackground(Color.GRAY);
                                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                                break;
                            default:
                                utils.setDebug(utils.rgbColor("Failed to change theme", 255, 0, 0), true);
                                break;
                        }
                        FilesUtilities.getConfig.saveTheme(themeName);
                    } else {
                        utils.setDebug(utils.rgbColor("Theme name is the same, nothing changed", 255, 0, 0), true);
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    SwingUtilities.updateComponentTreeUI(frame);
                    if (CreateFrame.creatorFrame != null) {
                        SwingUtilities.updateComponentTreeUI(CreateFrame.creatorFrame);
                    }
                });
            } catch (Throwable ex) {
                utils.log(ex);
                utils.setDebug(utils.rgbColor("Failed to change theme", 255, 0, 0), true);
                theme.setSelectedItem(oldTheme);
                FilesUtilities.getConfig.saveTheme(oldTheme);
            }
        });

        download.addActionListener(e -> {
            String urlDir;

            if (!dlURL.getText().contains("http://") && !dlURL.getText().contains("https://")) {
                urlDir = "http://" + dlURL.getText();
            } else {
                urlDir = dlURL.getText();
            }

            utils.setDebug(utils.rgbColor("Checking modpack, please wait...", 155, 210, 50), true);

            try {
                String name = utils.getModpackName(urlDir);

                if(((DefaultComboBoxModel<String>)modpacks.getModel()).getIndexOf(name) == -1) {
                    modpacks.addItem(name);
                    modpacks.setSelectedItem(name);
                    dlURL.setText(urlDir);
                }
            } catch (Throwable ex) {
                utils.log(ex);
            }


            try {
                if (!utils.isOutdated(urlDir)) {
                    utils.setDebug(utils.rgbColor("ModPack is updated!", 155, 240, 175), false);
                } else {
                    utils.setDebug(utils.rgbColor("Downloading new modpack update...", 255, 100, 100), false);

                    if (!dlURL.getText().contains("http://") && !dlURL.getText().contains("https://")) {
                        urlDir = "http://" + dlURL.getText();
                    } else {
                        urlDir = dlURL.getText();
                    }

                    Downloader downloader = new Downloader(urlDir);

                    Thread thread = new Thread(downloader, "Download");
                    thread.start();
                }
            } catch (Throwable ex) {
                utils.log(ex);
            }
        });

        list.addActionListener(e -> {
            utils.setDebug(utils.rgbColor("Listing mods: (green = downloaded) (red = needs to update)", 255, 100, 100), true);
            try {
                String urlDir;

                if (!dlURL.getText().contains("http://") && !dlURL.getText().contains("https://")) {
                    urlDir = "http://" + dlURL.getText();
                } else {
                    urlDir = dlURL.getText();
                }

                ListMods listMods = new ListMods(urlDir);
                ArrayList<File> listedMods = listMods.getMods();
                if (!listedMods.isEmpty()) {
                    int mods = 0;
                    int greenM = 0;
                    int redM = 0;

                    ArrayList<String> green = new ArrayList<>();
                    ArrayList<String> red = new ArrayList<>();
                    for (File listedMod : listedMods) {
                        mods++;
                        String path = FilesUtilities.getPath(listedMod);
                        if (utils.ModExists(listedMod)) {
                            green.add("( " + mods + " ) " + path);
                            greenM++;
                        } else {
                            red.add("( " + mods + " ) " + path);
                            redM++;
                        }
                    }

                    if (!green.isEmpty()) {
                        utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
                    }
                    if (!red.isEmpty()) {
                        utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
                    }
                    utils.setDebug(utils.rgbColor("Listed a total of " + mods + " mods <span style=\" color: green;\">" + greenM + "</span> downloaded and <span style=\" color: red;\">" + redM + "</span> needed of update", 255, 100, 100), false);
                } else {
                    utils.setDebug(utils.rgbColor("Error while retrieving mod list...", 220, 100, 100), false);
                }
            } catch (Throwable ex) {
                utils.log(ex);
                utils.setDebug(utils.rgbColor("Error while retrieving mod list...", 220, 100, 100), false);
            }
        });

        createPanel.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            CreateFrame newFrame = new CreateFrame();
            newFrame.display();
        }));

        dlURL.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveDownloadURL(dlURL.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveDownloadURL(dlURL.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveDownloadURL(dlURL.getText());
            }
        });

        modpacks.addActionListener(e -> {
            if (modpacks.getSelectedItem() != null) {
                String name = modpacks.getSelectedItem().toString();

                Modpack modpack = new Modpack(name);

                String urlDir;

                if (!dlURL.getText().contains("http://") && !dlURL.getText().contains("https://")) {
                    urlDir = "http://" + dlURL.getText();
                } else {
                    urlDir = dlURL.getText();
                }

                if (!urlDir.equals(modpack.getDownloadURL())) {
                    dlURL.setText(modpack.getDownloadURL());
                }
            }
        });

        install.addActionListener(e -> {
            if (modpacks.getSelectedItem() != null) {
                Modpack modpack = new Modpack(String.valueOf(modpacks.getSelectedItem()));

                Installer installer = new Installer(modpack, hardInstall.isSelected());

                String result = installer.install();

                switch (result) {
                    case "SUCCESS":
                        utils.setDebug(utils.rgbColor("Installed modpack " + modpack.getName() + " using its latest downloaded files...", 155, 240, 175), true);
                        break;
                    case "ALREADY_INSTALLED":
                        utils.setDebug(utils.rgbColor("Modpack " + modpack.getName() + " is already installed and updated", 220, 100, 100), true);
                        break;
                    case "EMPTY":
                        utils.setDebug(utils.rgbColor("Modpack " + modpack.getName() + " mod list is empty", 220, 100, 100), true);
                        break;
                    case "DOWNLOAD_NEED":
                        utils.setDebug(utils.rgbColor("Can't access modpack " + modpack.getName() + " cache files, please download and install it again", 220, 100, 100), true);
                        break;
                    default:
                        utils.setDebug(utils.rgbColor("Unexpected install result: " + result, 220, 100, 100), true);
                        break;
                }
            } else {
                if (modpacks.getItemCount() != 0) {
                    utils.setDebug(utils.rgbColor("Please choose a modpack from the list", 220, 100, 100), true);
                } else {
                    utils.setDebug(utils.rgbColor("Please download at least one modpack first", 220, 100, 100), true);
                }
            }
        });

        try {
            Checker checker = new Checker(version);
            checker.showVersion();
        } catch (Throwable e) {
            utils.log(e);
        }
    }

    private void disable(JSplitPane... panes) {
        for (JSplitPane pane : panes) {
            pane.setEnabled(false);
        }
    }

    private void syncSize(Component syncWith, Component... components) {
        for (Component component : components) {
            component.setPreferredSize(syncWith.getPreferredSize());
        }
    }

    public static void main(String[] args) {
        FlatLightLaf.install();
        FlatDarkLaf.install();

        try {
            String themeName = new Config().getTheme();

            String finalTheme;
            switch (themeName) {
                case "Light":
                    finalTheme = "Light";
                    UIManager.setLookAndFeel(FlatLightLaf.class.getCanonicalName());
                    break;
                case "Dark":
                    finalTheme = "Dark";
                    UIManager.setLookAndFeel(FlatDarkLaf.class.getCanonicalName());
                    break;
                default:
                    finalTheme = "System default";
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
            }

            new Config().saveTheme(finalTheme);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new MainFrame().initFrame());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (frame != null) {
                        frame.invalidate();
                        frame.repaint();
                        frame.revalidate();
                    }
                    if (CreateFrame.creatorFrame != null) {
                        CreateFrame.creatorFrame.invalidate();
                        CreateFrame.creatorFrame.repaint();
                        CreateFrame.creatorFrame.revalidate();
                    }
                });
            }
        }, 0, 1);
    }
}