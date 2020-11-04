package ml.karmaconfigs.modpackupdater;

import ml.karmaconfigs.modpackupdater.utils.Utils;
import ml.karmaconfigs.modpackupdater.utils.files.FilesUtilities;
import ml.karmaconfigs.modpackupdater.utils.modpack.Downloader;
import ml.karmaconfigs.modpackupdater.utils.modpack.Modpack;

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
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;
import java.util.Set;

public class SimpleFrame {

    public static JComboBox<String> modpacks = new JComboBox<>(Modpack.listing.modpacks());
    public static JFrame frame;
    public static File mcFolder = MainFrame.mcFolder;
    public static JProgressBar bar = new JProgressBar();
    public static JLabel barLabel = new JLabel();
    public static JScrollPane jsp = new JScrollPane(Utils.bPane);
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

    /**
     * Initialize the launcher GUI
     */
    public void initFrame() throws InvocationTargetException, InterruptedException {
        active = false;

        Utils utils = new Utils();
        mcFolder = FilesUtilities.getConfig.getMinecraftDir();
        installFolder = FilesUtilities.getConfig.getDownloadDir();
        frame = new JFrame();

        bar.setPreferredSize(new Dimension(frame.getWidth(), 1));

        barLabel.setHorizontalAlignment(JLabel.CENTER);
        barLabel.setVerticalAlignment(JLabel.CENTER);
        barLabel.setText("<html><div><h3>Download status bar</h3></div></html>");

        bar.setValue(0);

        try {
            frame.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        JComboBox<String> theme = new JComboBox<>(MainFrame.getThemes());
        theme.setSelectedItem(FilesUtilities.getConfig.getTheme());
        for (int i = 0; i < theme.getItemCount(); i++) {
            String name = theme.getItemAt(i);

            if (Objects.equals(theme.getSelectedItem(), name)) {
                theme.setSelectedIndex(i);
                break;
            }
        }

        //Text areas
        dlURL = new JTextArea("Modpack download.txt url");
        dlURL.setText(FilesUtilities.getConfig.getDownloadURL());

        jsp.getVerticalScrollBar().setUnitIncrement(15);

        //Buttons
        JButton install = new JButton("Install modpack");
        JButton creator = new JButton("Creator panel");
        JButton version_selector = new JButton("Change version");
        JButton choose_folder = new JButton("Minecraft dir");

        JSplitPane high_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, barLabel, bar);
        JSplitPane url_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dlURL, high_split);
        JSplitPane install_create = new JSplitPane(JSplitPane.VERTICAL_SPLIT, install, creator);
        JSplitPane header_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, url_split, install_create);
        JSplitPane version_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, header_split, version_selector);
        JSplitPane cs_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, version_split, choose_folder);
        JSplitPane chooser_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cs_split, jsp);

        disable(high_split, url_split, install_create, header_split, version_split, cs_split, chooser_split);

        frame.setPreferredSize(new Dimension(1280, 720));
        frame.setTitle("ModPack updater by KarmaDev " + MainFrame.version);
        frame.add(chooser_split);
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

        version_selector.addActionListener(e -> {
            frame.setVisible(false);
            new SimplePopup().initialize();
        });

        creator.addActionListener(e -> SwingUtilities.invokeLater(() -> {
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

        theme.addActionListener(e -> {
            String oldTheme = FilesUtilities.getConfig.getTheme();
            try {
                if (theme.getSelectedItem() != null) {
                    String themeName = theme.getSelectedItem().toString();

                    if (!oldTheme.equals(themeName) && !themeName.equals("Themes by FlatLaf themes")) {
                        UIManager.setLookAndFeel(MainFrame.themeData.get(themeName));
                        FilesUtilities.getConfig.saveTheme(themeName);
                    } else {
                        if (!themeName.equals("Themes by FlatLaf themes")) {
                            utils.setDebug(utils.rgbColor("Selected theme is the same as actual, nothing changed", 220, 100, 100), true);
                        } else {
                            if (!oldTheme.equals("Themes by FlatLaf themes")) {
                                theme.setSelectedItem(oldTheme);
                            } else {
                                UIManager.setLookAndFeel(MainFrame.themeData.get("Light"));
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
                boolean outdated = utils.isOutdated(urlDir);
                if (!outdated) {
                    outdated = !new Modpack(utils.getModpackName(urlDir)).exists();
                }

                if (!outdated) {
                    utils.setDebug(utils.rgbColor("ModPack is already installed and updated!", 155, 240, 175), false);
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

        choose_folder.addActionListener(ee -> {
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

        SwingUtilities.invokeAndWait(utils::reloadTool);
    }

    private void disable(JSplitPane... panes) {
        for (JSplitPane pane : panes) {
            pane.setEnabled(false);
        }
    }
}