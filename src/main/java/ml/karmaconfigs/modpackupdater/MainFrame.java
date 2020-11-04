package ml.karmaconfigs.modpackupdater;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import ml.karmaconfigs.modpackupdater.utils.Utils;
import ml.karmaconfigs.modpackupdater.utils.files.Config;
import ml.karmaconfigs.modpackupdater.utils.files.FilesUtilities;
import ml.karmaconfigs.modpackupdater.utils.modpack.Downloader;
import ml.karmaconfigs.modpackupdater.utils.modpack.Installer;
import ml.karmaconfigs.modpackupdater.utils.modpack.ListMods;
import ml.karmaconfigs.modpackupdater.utils.modpack.Modpack;
import ml.karmaconfigs.modpackupdater.versionchecker.Changelog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Timer;
import java.util.*;

public class MainFrame {

    static final HashMap<String, String> themeData = new HashMap<>();
    public static String version = "${version}";
    public static JComboBox<String> modpacks = new JComboBox<>(Modpack.listing.modpacks());
    public static JFrame frame;
    public static JPanel line = new Line();
    public static JProgressBar internal_bar;
    public static JLabel internal_barLabel;
    public static JScrollPane jsp;
    public static JLabel internal_label = new JLabel();
    static {
        internal_label.setOpaque(true);
        internal_label.setBackground(Color.DARK_GRAY);
    }
    public static JSplitPane modpackLabel;

    public static JButton refreshChangelog;
    public static JButton chooseFolder;
    public static File mcFolder;
    public static boolean active = false;
    private static boolean shift = false;
    private static JFrame cFrame;
    private static JTextArea dlURL;
    private File installFolder;

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

    public static void main(String[] args) {
        detectThemes();
        try {
            InputStream props = (MainFrame.class).getResourceAsStream("/data.properties");
            Properties properties = new Properties();
            properties.load(props);
            version = properties.getProperty("version");
        } catch (Throwable e) {
            version = "0";
        }

        try {
            String themeName = new Config().getTheme();

            UIManager.setLookAndFeel(themeData.getOrDefault(themeName
                    .replace("Dark 2", "Dark purple")
                    .replace("Themes by FlatLaf themes", "Light"), "Dark"));

            new MainFrame().initFrame();
            new SimpleFrame().initFrame();
        } catch (Throwable e) {
            e.printStackTrace();
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
                    if (SimpleFrame.frame != null) {
                        SimpleFrame.frame.invalidate();
                        SimpleFrame.frame.repaint();
                        SimpleFrame.frame.revalidate();
                    }
                    if (CreateFrame.creatorFrame != null) {
                        CreateFrame.creatorFrame.invalidate();
                        CreateFrame.creatorFrame.repaint();
                        CreateFrame.creatorFrame.revalidate();
                    }
                });
            }
        }, 0, 1);

        Config config = new Config();
        if (config.skipSelector()) {
            if (config.openSimple()) {
                SimpleFrame.frame.setVisible(true);
            } else {
                MainFrame.frame.setVisible(true);
            }
        } else {
            new SimplePopup().initialize();
        }
    }

    private static void detectThemes() {
        themeData.put("Light", FlatLightLaf.class.getCanonicalName());
        FlatLightLaf.install();
        themeData.put("Dark", FlatDarkLaf.class.getCanonicalName());
        FlatDarkLaf.install();
        themeData.put("Darcula", FlatDarculaLaf.class.getCanonicalName());
        FlatDarculaLaf.install();

        for (UIManager.LookAndFeelInfo info : FlatAllIJThemes.INFOS) {
            UIManager.installLookAndFeel(info);
            themeData.put(info.getName(), info.getClassName());
        }

        themeData.put("Themes by FlatLaf themes", FlatLightLaf.class.getCanonicalName());
    }

    static String[] getThemes() {
        String[] namesArray = new String[themeData.keySet().size()];
        int index = 0;
        for (String str : themeData.keySet()) {
            namesArray[index] = str;
            index++;
        }

        return namesArray;
    }

    /**
     * Initialize the launcher GUI
     */
    public void initFrame() throws InvocationTargetException, InterruptedException {
        active = false;

        Utils utils = new Utils();
        mcFolder = FilesUtilities.getConfig.getMinecraftDir();
        installFolder = FilesUtilities.getConfig.getDownloadDir();
        frame = new JFrame();

        internal_bar = new JProgressBar();
        internal_bar.setPreferredSize(new Dimension(frame.getWidth(), 1));
        internal_barLabel = new JLabel();

        internal_barLabel.setHorizontalAlignment(JLabel.CENTER);
        internal_barLabel.setVerticalAlignment(JLabel.CENTER);
        internal_barLabel.setText("<html><div><h3>Download status bar</h3></div></html>");

        internal_bar.setValue(0);

        try {
            frame.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        JComboBox<String> theme = new JComboBox<>(getThemes());
        theme.setSelectedItem(FilesUtilities.getConfig.getTheme());
        for (int i = 0; i < theme.getItemCount(); i++) {
            String name = theme.getItemAt(i);

            if (Objects.equals(theme.getSelectedItem(), name)) {
                theme.setSelectedIndex(i);
                break;
            }
        }

        //Check boxes
        JCheckBox hardInstall = new JCheckBox("Force install/update");
        JCheckBox checkUpdates = new JCheckBox("Version checker");

        //Text areas
        dlURL = new JTextArea("Modpack download.txt url");
        dlURL.setText(FilesUtilities.getConfig.getDownloadURL());

        //Buttons
        JButton download = new JButton("Update modpack");
        JButton install = new JButton("Install modpack");
        JButton list = new JButton("List mods");
        JButton createPanel = new JButton("Creator panel");
        refreshChangelog = new JButton("Refresh version info");
        chooseFolder = new JButton("MC download folder");
        JButton exportDebug = new JButton("Export debug");
        JButton version_selector = new JButton("Version selector");

        //Panels
        JPanel installPanel = new JPanel();
        JPanel optionsPanel = new JPanel();
        JPanel managerPanel = new JPanel();
        JPanel themePane = new JPanel();
        JPanel modpackPane = new JPanel();
        JPanel launcherPanel = new JPanel();
        JPanel chooserPanel = new JPanel();

        //Labels
        JLabel cPane = new JLabel();
        JLabel themeText = new JLabel("Theme");
        JLabel packText = new JLabel("Modpack");

        //Scroll panes
        jsp = new JScrollPane(internal_label);
        JScrollPane csp = new JScrollPane(cPane);

        //Split panes
        JSplitPane installSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, install, hardInstall);
        JSplitPane options = new JSplitPane(JSplitPane.VERTICAL_SPLIT, download, installPanel);
        JSplitPane manager = new JSplitPane(JSplitPane.VERTICAL_SPLIT, list, createPanel);
        JSplitPane version_button = new JSplitPane(JSplitPane.VERTICAL_SPLIT, manager, version_selector);

        JSplitPane leftTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionsPanel, managerPanel);

        JSplitPane themeLabel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, themeText, theme);
        modpackLabel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, packText, modpacks);

        JSplitPane managerSplitterOne = new JSplitPane(JSplitPane.VERTICAL_SPLIT, themePane, modpackPane);
        JSplitPane managerSplitterTwo = new JSplitPane(JSplitPane.VERTICAL_SPLIT, launcherPanel, chooserPanel);
        JSplitPane managerSplitterThree = new JSplitPane(JSplitPane.VERTICAL_SPLIT, managerSplitterTwo, managerSplitterOne);
        JSplitPane managerSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, managerSplitterThree, new JPanel());
        JSplitPane optionOneSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftTop, managerSplitter);
        JSplitPane optionTwoSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionOneSplitter, checkUpdates);

        JSplitPane exportUrl = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dlURL, exportDebug);
        exportUrl.setDividerLocation(700);

        JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionTwoSplitter, csp);
        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, exportUrl, jsp);

        JSplitPane barSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        JSplitPane barStatusSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, internal_bar, internal_barLabel);
        JSplitPane lineSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, barStatusSplitter, line);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lineSplit, barSplitter);

        //Disable the split panes resizing...
        disable(installSplit, options, manager, version_button, leftTop, themeLabel, modpackLabel, managerSplitterOne, managerSplitterTwo, managerSplitterThree, managerSplitter, optionOneSplitter, optionTwoSplitter, left, right, barSplitter, barStatusSplitter, lineSplit, splitter);

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
                managerSplitterTwo.setDividerSize(-5);
                barStatusSplitter.setDividerSize(-4);

                //Grouping left-bottom options
                managerSplitter.setDividerSize(-5);

                //Dividing progress bar from the rest
                //of the panel
                splitter.setDividerSize(25);
            }

            //JCheckBoxes options sync with config
            {
                checkUpdates.setSelected(FilesUtilities.getConfig.checkVersions());
            }

            //Sync the boxes size
            {
                syncSize(installSplit, download, install, list, createPanel, theme, modpacks, refreshChangelog, chooseFolder);
            }

            { //Panels options
                installPanel.add(installSplit);
                optionsPanel.add(options);
                managerPanel.add(version_button);
                themePane.add(themeLabel);
                modpackPane.add(modpackLabel);
                launcherPanel.add(refreshChangelog);
                chooserPanel.add(chooseFolder);
                try {
                    Changelog changelog = new Changelog();
                    cPane.setText(changelog.toString());
                } catch (Throwable e) {
                    utils.log(e);
                }
            }
        }

        int themeAmount = theme.getItemCount() - 1;
        utils.setDebug(utils.rgbColor("Loaded " + themeAmount + " themes", 125, 255, 195), false);

        frame.setPreferredSize(new Dimension(1280, 720));
        frame.setTitle("ModPack updater by KarmaDev " + version);
        frame.add(splitter);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
        frame.setMinimumSize(new Dimension(800, 800));

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                for (Thread thread : threadSet) {
                    if (thread.getState() == Thread.State.RUNNABLE)
                        thread.interrupt();
                }
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

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            synchronized (MainFrame.class) {
                switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT || ke.getKeyCode() == KeyEvent.VK_CONTROL) {
                            shift = true;
                            exportDebug.setText("Open debug");
                            packText.setText("<html>Modpack<br>info</html>");
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT || ke.getKeyCode() == KeyEvent.VK_CONTROL) {
                            shift = false;
                            exportDebug.setText("Export debug");
                            packText.setText("Modpack");
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

                    if (!oldTheme.equals(themeName) && !themeName.equals("Themes by FlatLaf themes")) {
                        UIManager.setLookAndFeel(themeData.get(themeName));
                        FilesUtilities.getConfig.saveTheme(themeName);
                    } else {
                        if (!themeName.equals("Themes by FlatLaf themes")) {
                            utils.setDebug(utils.rgbColor("Selected theme is the same as actual, nothing changed", 220, 100, 100), true);
                        } else {
                            if (!oldTheme.equals("Themes by FlatLaf themes")) {
                                theme.setSelectedItem(oldTheme);
                            } else {
                                UIManager.setLookAndFeel(themeData.get("Light"));
                                theme.setSelectedItem("Light");
                            }
                            utils.setDebug(utils.rgbColor("https://github.com/JFormDesigner/FlatLaf", 120, 100, 100), true);
                            utils.setDebug(utils.rgbColor("https://www.formdev.com/flatlaf/", 120, 100, 100), true);
                        }
                    }
                }

                utils.reloadTool();
            } catch (Throwable ex) {
                utils.log(ex);
                utils.setDebug(utils.rgbColor("Failed to change theme", 220, 100, 100), true);
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

                if (((DefaultComboBoxModel<String>) modpacks.getModel()).getIndexOf(modpack.getName()) == -1) {
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
            if (!shift) {
                if (CreateFrame.creatorFrame == null) {
                    CreateFrame newFrame = new CreateFrame();
                    newFrame.display();
                } else {
                    CreateFrame.versioning.checkVersions();

                    if (!CreateFrame.creatorFrame.isVisible()) {
                        CreateFrame.creatorFrame.setVisible(true);
                    }
                    CreateFrame.creatorFrame.toFront();
                }
            }
        }));

        version_selector.addActionListener(e -> {
            frame.setVisible(false);
            new SimplePopup().initialize();
        });

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
                        case "DOWNLOAD_NEED":
                            utils.setDebug(utils.rgbColor("The modpack " + modpack.getName() + " needs to be downloaded", 220, 100, 100), true);
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

        refreshChangelog.addActionListener(e -> {
            try {
                Changelog changelog = new Changelog();
                cPane.setText(changelog.toString());
            } catch (Throwable ex) {
                utils.log(ex);
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

                try {
                    cFrame.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
                } catch (Throwable e) {
                    e.printStackTrace();
                }

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
                if (et.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                    if (!chooser.getSelectedFile().equals(installFolder)) {
                        installFolder = chooser.getSelectedFile();
                        utils.setDebug(utils.rgbColor("Changed modpack download directory to: " + FilesUtilities.getPath(installFolder), 120, 200, 155), true);
                        FilesUtilities.getConfig.saveMcDownloadDir(installFolder);
                        cFrame.setVisible(false);
                    } else {
                        utils.setDebug(utils.rgbColor("Same modpack download directory selected, nothing changed", 110, 150, 150), false);
                    }

                    chooser.setCurrentDirectory(installFolder);
                } else {
                    if (et.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
                        cFrame.setVisible(false);
                        SwingUtilities.updateComponentTreeUI(MainFrame.frame);
                    }
                }
            });
        });

        exportDebug.addActionListener(e -> {
            File debug = new File(FilesUtilities.getUpdaterDir(), "debug.html");
            if (!shift) {
                try {
                    if (!debug.exists() && debug.createNewFile()) {
                        System.out.println("Executed");
                    }
                    String color = "DarkGrey";
                    String rgb = "( R:" + internal_label.getBackground().getRed() + ", B:" + internal_label.getBackground().getBlue() + ", G:" + internal_label.getBackground().getGreen() + " )";

                    String totalRGB = internal_label.getBackground().getRed() + ", " + internal_label.getBackground().getBlue() + ", " + internal_label.getBackground().getGreen();

                    FileWriter writer = new FileWriter(debug);
                    writer.write("<!-- THEME: " + FilesUtilities.getConfig.getTheme() + ", Base-Color: " + color.replace("DarkGrey", "DARK_GRAY") + "; [AS RGB]: " + rgb + " -->");
                    for (String str : internal_label.getText().split("<br>")) {
                        writer.write(str.replace("<html>", "<html style=\"background-color: rgb(" + totalRGB + ");\">") + "<br>" + "\n");
                    }
                    writer.flush();
                    writer.close();

                    utils.setDebug(utils.rgbColor("Exported debug to debug.html", 155, 240, 175), true);
                } catch (Throwable ex) {
                    utils.log(ex);
                }
            } else {
                try {
                    Desktop.getDesktop().open(debug);
                } catch (Throwable ex) {
                    utils.log(ex);
                }
            }
        });

        SwingUtilities.invokeAndWait(utils::reloadTool);
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
}

class Line extends JPanel {

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Line2D.Float(25, 8, 1255, 8));
    }
}