package ml.karmaconfigs.ModPackUpdater;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.Config;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.Launcher.Launch;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.*;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;
import ml.karmaconfigs.ModPackUpdater.VersionChecker.Checker;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Timer;
import java.util.TimerTask;

public class MainFrame {

    private static boolean shift = false;
    private static JFrame cFrame;

    public static String version = MainFrame.class.getPackage().getImplementationVersion();

    public static JComboBox<String> modpacks = new JComboBox<>(Modpack.listing.modpacks());

    public static JFrame frame;

    public static JLabel bPane;
    public static JLabel barLabel;

    public static JScrollPane jsp;

    public static JSplitPane modpackLabel;

    public static JProgressBar bar;

    public static JButton launchPack;
    public static JButton chooseFolder;

    private static JTextArea dlURL;
    protected static JTextArea c_name;
    protected static JTextArea c_mem;

    public static File mcFolder;
    private File installFolder;

    /**
     * Initialize the launcher GUI
     */
    public void initFrame() {
        Utils utils = new Utils();
        mcFolder = FilesUtilities.getConfig.getMinecraftDir();
        installFolder = FilesUtilities.getConfig.getDownloadDir();
        frame = new JFrame();

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

        /*
        Nobody should use "System default" since it looks pretty bad and honestly, it works really slow :)
        Should I remove it in a future version?
        */
        JComboBox<String> theme = new JComboBox<>(new String[]{"Light", "Dark", "System default"});
        theme.setSelectedItem(FilesUtilities.getConfig.getTheme());

        //Check boxes
        JCheckBox hardInstall = new JCheckBox("Force install/update");
        JCheckBox checkUpdates = new JCheckBox("Version checker");

        //Text areas
        c_name = new JTextArea("Player");
        c_name.setText(FilesUtilities.getConfig.getClientName());
        c_mem = new JTextArea("2048");
        c_mem.setText(FilesUtilities.getConfig.getClientMemory());
        dlURL = new JTextArea("Modpack download.txt url");
        dlURL.setText(FilesUtilities.getConfig.getDownloadURL());

        //Buttons
        JButton download = new JButton("Update modpack");
        JButton install = new JButton("Install modpack");
        JButton list = new JButton("List mods");
        JButton createPanel = new JButton("Creator panel");
        launchPack = new JButton("Launch modpack ( " + utils.getCurrentModpack() + " )");
        chooseFolder = new JButton("MC download folder");
        JButton exportDebug = new JButton("Export debug");

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
        modpackLabel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, packText, modpacks);

        JSplitPane managerSplitterOne = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, c_mem, c_name);
        JSplitPane managerSpacer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, managerSplitterOne, new JPanel());
        JSplitPane managerSplitterTwo = new JSplitPane(JSplitPane.VERTICAL_SPLIT, themePane, modpackPane);
        JSplitPane managerSplitterThree = new JSplitPane(JSplitPane.VERTICAL_SPLIT, launchPack, chooseFolder);
        JSplitPane managerSplitterFour = new JSplitPane(JSplitPane.VERTICAL_SPLIT, managerSplitterTwo, managerSplitterThree);
        JSplitPane managerSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, managerSpacer, managerSplitterFour);
        JSplitPane optionOneSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftTop, managerSplitter);
        JSplitPane optionTwoSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionOneSplitter, checkUpdates);

        JSplitPane exportUrl = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dlURL, exportDebug);
        exportUrl.setDividerLocation(700);

        JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionTwoSplitter, new JPanel());
        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, exportUrl, jsp);

        JSplitPane barSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bar, barSplitter);

        //Disable the split panes resizing...
        disable(installSplit, options, manager, leftTop, themeLabel, modpackLabel, managerSpacer, managerSplitterOne, managerSplitterTwo, managerSplitterThree, managerSplitterFour, managerSplitter, optionOneSplitter, optionTwoSplitter, left, right, barSplitter, splitter);

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

            //JCheckBoxes options sync with config
            {
                checkUpdates.setSelected(FilesUtilities.getConfig.checkVersions());
            }

            //Sync the boxes size
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

        frame.setPreferredSize(new Dimension(1280, 720));
        frame.setTitle("ModPack updater by KarmaDev " + version);
        frame.add(splitter);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        frame.setMinimumSize(new Dimension(800, 800));

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            synchronized (MainFrame.class) {
                switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT || ke.getKeyCode() == KeyEvent.VK_CONTROL) {
                            shift = true;
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT || ke.getKeyCode() == KeyEvent.VK_CONTROL) {
                            shift = false;
                        }
                        break;
                }
                return false;
            }
        });

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
                                if (Utils.info != null && Utils.infoScrollable != null) {
                                    Utils.infoScrollable.setBackground(Color.DARK_GRAY);
                                }
                                UIManager.setLookAndFeel(FlatLightLaf.class.getCanonicalName());
                                break;
                            case "Dark":
                                bPane.setOpaque(true);
                                bPane.setBackground(Color.DARK_GRAY);
                                UIManager.setLookAndFeel(FlatDarkLaf.class.getCanonicalName());
                                if (Utils.info != null && Utils.infoScrollable != null) {
                                    Utils.infoScrollable.setBackground(Color.DARK_GRAY);
                                }
                                break;
                            case "System default":
                                bPane.setOpaque(true);
                                bPane.setBackground(Color.GRAY);
                                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                                if (Utils.info != null && Utils.infoScrollable != null) {
                                    Utils.infoScrollable.setBackground(Color.GRAY);
                                }
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
                    if (Utils.info != null) {
                        SwingUtilities.updateComponentTreeUI(Utils.info);
                    }
                    SwingUtilities.updateComponentTreeUI(CreateFrame.chooser);
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

            if (urlDir.endsWith("/")) {
                urlDir = urlDir.substring(0, urlDir.length() - 1);
            }

            utils.setDebug(utils.rgbColor("Checking modpack from the url " + urlDir + ", please wait...", 155, 210, 50), true);

            try {
                Modpack modpack = new Modpack(utils.getModpackName(urlDir));
                if (!modpack.exists()) {
                    URL downloadURL = new URL(urlDir);
                    ReadableByteChannel rbc = Channels.newChannel(downloadURL.openStream());
                    FileOutputStream fos = new FileOutputStream(FilesUtilities.getFileFromURL(urlDir));
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }

                if(((DefaultComboBoxModel<String>)modpacks.getModel()).getIndexOf(modpack.getName()) == -1) {
                    modpacks.addItem(modpack.getName());
                    modpacks.setSelectedItem(modpack.getName());
                    dlURL.setText(urlDir);
                }
            } catch (Throwable ex) {
                utils.log(ex);
            }

            try {
                boolean outdated = true;
                if (!hardInstall.isSelected()) {
                    outdated = utils.isOutdated(urlDir);
                    if (!outdated) {
                        outdated = !new Modpack(utils.getModpackName(urlDir)).exists();
                    }
                }

                if (!outdated) {
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
            String urlDir;

            if (!dlURL.getText().contains("http://") && !dlURL.getText().contains("https://")) {
                urlDir = "http://" + dlURL.getText();
            } else {
                urlDir = dlURL.getText();
            }

            if (urlDir.endsWith("/")) {
                urlDir = urlDir.substring(0, urlDir.length() - 1);
            }

            utils.setDebug(utils.rgbColor("Listing mods from url: " + urlDir + " (<span style=\"color: rgb(155, 240, 175);\">green</span> = downloaded) (<span style=\"color: rgb(220, 100, 100);\">red</span> = needs to update)", 155, 210, 50), true);
            try {
                ListMods listMods = new ListMods(urlDir);

                Thread thread = new Thread(listMods, "Listing");
                thread.start();
            } catch (Throwable ex) {
                utils.log(ex);
                utils.setDebug(utils.rgbColor("Error while retrieving mod list...", 220, 100, 100), false);
            }
        });

        createPanel.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (CreateFrame.creatorFrame == null) {
                CreateFrame newFrame = new CreateFrame();
                newFrame.display();
            } else {
                if (!CreateFrame.creatorFrame.isVisible()) {
                    CreateFrame.creatorFrame.setVisible(true);
                }
                CreateFrame.creatorFrame.toFront();
            }
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
            Object modpackName = modpacks.getSelectedItem();

            if (modpackName != null) {
                String name = modpackName.toString();
                Modpack modpack = new Modpack(name);

                if (modpack.exists()) {
                    String urlDir;

                    if (!modpack.getDownloadURL().contains("http://") && !modpack.getDownloadURL().contains("https://")) {
                        urlDir = "http://" + modpack.getDownloadURL();
                    } else {
                        urlDir = modpack.getDownloadURL();
                    }

                    if (urlDir.endsWith("/")) {
                        urlDir = urlDir.substring(0, urlDir.length() - 1);
                    }

                    utils.setDebug(utils.rgbColor("Detected modpack " + modpack.getName() + " url: " + urlDir, 155, 240, 175), true);
                    dlURL.setText(urlDir);

                    FilesUtilities.getConfig.saveDownloadURL(urlDir);
                }
            }
        });

        modpacks.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (shift) {
                    Object modpackName = modpacks.getSelectedItem();
                    if (modpackName != null) {
                        Modpack modpack = new Modpack(modpackName.toString());
                        if (modpack.exists()) {
                            utils.displayPackInfo(modpack);
                        }
                    }
                }
            }
        });

        install.addActionListener(e -> {
            try {
                if (modpacks.getSelectedItem() != null) {
                    String urlDir;

                    if (!dlURL.getText().contains("http://") && !dlURL.getText().contains("https://")) {
                        urlDir = "http://" + dlURL.getText();
                    } else {
                        urlDir = dlURL.getText();
                    }

                    if (urlDir.endsWith("/")) {
                        urlDir = urlDir.substring(0, urlDir.length() - 1);
                    }

                    Modpack modpack = new Modpack(String.valueOf(modpacks.getSelectedItem()));

                    if (!modpack.exists()) {
                        if (!modpack.exists()) {
                            URL downloadURL = new URL(urlDir);
                            ReadableByteChannel rbc = Channels.newChannel(downloadURL.openStream());
                            FileOutputStream fos = new FileOutputStream(FilesUtilities.getFileFromURL(urlDir));
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        }
                    }

                    Installer installer = new Installer(modpack, hardInstall.isSelected());

                    String result = installer.install();

                    switch (result) {
                        case "SUCCESS":
                            utils.setDebug(utils.rgbColor("Installed modpack " + modpack.getName() + " using its latest downloaded files...", 155, 240, 175), true);
                            break;
                        case "EMPTY":
                            utils.setDebug(utils.rgbColor("Modpack " + modpack.getName() + " mod list is empty", 220, 100, 100), true);
                            break;
                        case "ALREADY":
                            utils.setDebug(utils.rgbColor("The modpack " + modpack.getName() + " is already installed, check hard install to force the install", 220, 100, 100), true);
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
            } catch (Throwable ex) {
                utils.log(ex);
            }
        });

        checkUpdates.addActionListener(e -> new Config().saveVersionOptions(checkUpdates.isSelected()));

        launchPack.addActionListener(e -> {
            try {
                Modpack modpack = new Modpack(utils.getCurrentModpack());
                if (modpack.exists()) {
                    new Launch();
                } else {
                    utils.setDebug(utils.rgbColor("Modpack not found, please make sure it's installed", 220, 100, 100), true);
                }
            } catch (Throwable ex) {
                utils.log(ex);
            }
        });

        c_name.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveClientName(c_name.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveClientName(c_name.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveClientName(c_name.getText());
            }
        });

        c_mem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveClientMem(c_mem.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveClientMem(c_mem.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                FilesUtilities.getConfig.saveClientMem(c_mem.getText());
            }
        });

        chooseFolder.addActionListener(ee -> {
            JFileChooser chooser = new JFileChooser();

            chooser.setCurrentDirectory(installFolder);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setToolTipText("");
            chooser.setApproveButtonToolTipText("");

            if (cFrame == null) {
                cFrame = new JFrame();

                cFrame.setPreferredSize(new Dimension(800, 800));
                cFrame.setTitle("Choose modpack downloads dest dir");
                cFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                cFrame.pack();
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                cFrame.setLocation(dimension.width / 2 - cFrame.getPreferredSize().width / 2, dimension.height / 2 - cFrame.getPreferredSize().height / 2);
                cFrame.add(chooser);
                cFrame.setResizable(false);
            }

            if (!cFrame.isVisible()) {
                cFrame.setVisible(true);
            } else {
                cFrame.toFront();
            }

            cFrame.setSize(new Dimension(800, 800));

            chooser.addActionListener(et -> {
                if (et.getActionCommand().contains("Approve")) {
                    if (!chooser.getSelectedFile().equals(installFolder)) {
                        installFolder = chooser.getSelectedFile();
                        utils.setDebug(utils.rgbColor("Changed modpack download directory to: " + FilesUtilities.getPath(installFolder), 120, 200, 155), true);
                        FilesUtilities.getConfig.saveMinecraftDir();
                        cFrame.setVisible(false);
                    } else {
                        utils.setDebug(utils.rgbColor("Same modpack download directory selected, nothing changed", 110, 150, 150), false);
                    }
                } else {
                    if (et.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
                        cFrame.setVisible(false);
                        SwingUtilities.updateComponentTreeUI(MainFrame.frame);
                    }
                }
            });
        });

        exportDebug.addActionListener(e -> {
            try {
                File debug = new File(FilesUtilities.getUpdaterDir(), "debug.html");

                if (!debug.exists() && debug.createNewFile()) {
                    System.out.println("Executed");
                }
                String color;
                String rgb;
                if (FilesUtilities.getConfig.getTheme().equals("System default")) {
                    color = "Grey";
                } else {
                    color = "DarkGrey";
                }
                rgb  = "( R:" + bPane.getBackground().getRed() + ", B:" + bPane.getBackground().getBlue() + ", G:" + bPane.getBackground().getGreen() + " )";

                String totalRGB = bPane.getBackground().getRed() + ", " + bPane.getBackground().getBlue() + ", " + bPane.getBackground().getGreen();

                FileWriter writer = new FileWriter(debug);
                writer.write("<!-- THEME: " + FilesUtilities.getConfig.getTheme() + ", Base-Color: " + color.replace("DarkGrey", "DARK_GRAY") + "; [AS RGB]: " + rgb + " -->");
                for (String str : bPane.getText().split("<br>")) {
                    writer.write(str.replace("<html>", "<html style=\"background-color: rgb(" + totalRGB + ");\">") + "<br>" + "\n");
                }
                writer.flush();
                writer.close();

                utils.setDebug(utils.rgbColor("Exported debug to debug.html", 155, 240, 175), true);
            } catch (Throwable ex) {
                utils.log(ex);
            }
        });
    }

    public static void restartModpacksListeners() {
        Utils utils = new Utils();
        modpacks.addActionListener(e -> {
            Object modpackName = modpacks.getSelectedItem();

            if (modpackName != null) {
                String name = modpackName.toString();
                Modpack modpack = new Modpack(name);

                if (modpack.exists()) {
                    String urlDir;

                    if (!modpack.getDownloadURL().contains("http://") && !modpack.getDownloadURL().contains("https://")) {
                        urlDir = "http://" + modpack.getDownloadURL();
                    } else {
                        urlDir = modpack.getDownloadURL();
                    }

                    if (urlDir.endsWith("/")) {
                        urlDir = urlDir.substring(0, urlDir.length() - 1);
                    }

                    utils.setDebug(utils.rgbColor("Detected modpack " + modpack.getName() + " url: " + urlDir, 155, 240, 175), true);
                    dlURL.setText(urlDir);

                    FilesUtilities.getConfig.saveDownloadURL(urlDir);
                }
            }
        });

        modpacks.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (shift) {
                    Object modpackName = modpacks.getSelectedItem();
                    if (modpackName != null) {
                        Modpack modpack = new Modpack(modpackName.toString());
                        if (modpack.exists()) {
                            utils.displayPackInfo(modpack);
                        }
                    }
                }
            }
        });
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

        if (new Config().checkVersions()) {
            new Checker(version).showVersion();
        } else {
            new MainFrame().initFrame();
        }

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

    protected boolean isValidVersion(String version) {
        int v = Integer.parseInt(version.replaceAll("[^a-zA-Z0-9]", "").replaceAll("[aA-zZ]", ""));

        if (v < 1000) {
            return v <= 112;
        } else {
            return v <= 1122;
        }
    }
}