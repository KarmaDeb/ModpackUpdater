package ml.karmaconfigs.modpackupdater;

import com.formdev.flatlaf.FlatDarkLaf;

import ml.karmaconfigs.modpackupdater.manager.CurseDownloader;
import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.files.data.DataReader;
import ml.karmaconfigs.modpackupdater.files.memory.ClientMemory;
import ml.karmaconfigs.modpackupdater.utils.*;
import ml.karmaconfigs.modpackupdater.utils.Color;
import ml.karmaconfigs.modpackupdater.utils.datatype.Resource;
import ml.karmaconfigs.modpackupdater.utils.downloader.ResourceDownloader;
import org.apache.commons.validator.routines.UrlValidator;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public final class Updater implements Utils {

    private static boolean ctr = false;

    private final static JFrame main_frame = new JFrame();
    private final static Dimension size = new Dimension(900, 600);

    private static String modpack_url;
    private final static Debug debug = new Debug();

    private final static JTextArea url_input = new JTextArea();

    private final static JButton download = new JButton("Download");
    private final static JButton create = new JButton("Create");
    private final static JButton open = new JButton("Open");
    private final static JButton choose = new JButton("<html>Minecraft<br>directory</html>");
    private final static JButton curse = new JButton("<html>Mods<br>manager</html>");
    private final static JButton launch = new JButton("Launcher");
    private final static JButton export = new JButton("Export");

    private final static JCheckBox auto_scroll = new JCheckBox("Auto scroll");
    private final static JCheckBox check_updates = new JCheckBox("Check updates");

    private final static JPanel left_panel = new JPanel();

    private final static JSplitPane debug_export = new JSplitPane(JSplitPane.VERTICAL_SPLIT, export, debug.getDebugScrollable());
    private final static JSplitPane data_debug = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left_panel, debug_export);
    private final static JSplitPane download_create = new JSplitPane(JSplitPane.VERTICAL_SPLIT, download, create);
    private final static JSplitPane dc_open = new JSplitPane(JSplitPane.VERTICAL_SPLIT, download_create, open);
    private final static JSplitPane dco_choose = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dc_open, choose);
    private final static JSplitPane dcoc_curse = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dco_choose, curse);
    private final static JSplitPane dcocc_launch = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dcoc_curse, launch);
    private final static JSplitPane scroll_check = new JSplitPane(JSplitPane.VERTICAL_SPLIT, auto_scroll, check_updates);
    private final static JSplitPane dcoc_sc = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dcocc_launch, scroll_check);
    private final static JSplitPane text_url = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JLabel("        Download url"), url_input);
    private final static JSplitPane url_tool = new JSplitPane(JSplitPane.VERTICAL_SPLIT, text_url, data_debug);

    private static DataReader reader = new DataReader(null);
    private final static ChangeLog changelog = new ChangeLog();

    private static boolean downloading = false;
    private static boolean shown = false;
    private static boolean available = false;
    private static boolean advised = false;
    private static int checks = 0;

    /**
     * Initialize the frame to show it
     * to the user
     */
    private static void initialize() {
        //This won't work when testing, but it should show the version
        //on built jar instances
        final String app_ver = Updater.class.getPackage().getSpecificationVersion();
        final String app_build = Updater.class.getPackage().getImplementationVersion();

        //Some frame utils
        main_frame.setIconImage(app_ico);
        main_frame.setTitle("ModpackUpdater [ %s ]".replace("%s", (app_ver != null ? app_ver : "?") + " " + (app_build != null ? app_build : "build ?")));

        //Set the main frame size
        main_frame.setMinimumSize(size);
        main_frame.setMaximumSize(size);
        main_frame.setPreferredSize(size);
        main_frame.setSize(size);

        //What will do the main frame on close
        main_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Center the frame
        Utils.toCenter(main_frame);

        //Setting up the app GUI
        {
            if (modpack_url.isEmpty())
                modpack_url = "https://example.org/modpack.mpu";

            //Debug area
            debug.getEditor().addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Throwable ex) {
                        Debug.util.add(Text.util.create("Your system is not compatible with java browser", Color.INDIANRED, 12), true);
                    }
                }
            });

            //Text areas
            url_input.setText(modpack_url);
            url_input.setToolTipText("Put here the url to an .mpu directory" +
                    "\n(https://example.org/modpack.mpu)" +
                    "\nso when you click \"Download\" it will be" +
                    "\ndownloaded");

            //Buttons
            download.setToolTipText("Download the modpack specified in the" +
                    "\nmodpack download url");
            create.setToolTipText("If you are a modpack creator you can" +
                    "\ncreate modpacks by clicking here");
            open.setToolTipText("If you have a .zip file with the .mpu" +
                    "\nfile, you can just install it by opening" +
                    "\nit using this tool");
            choose.setToolTipText("Select where your minecraft" +
                    "\ndirectory is located, this" +
                    "\nwill be used when installing" +
                    "\nand creating modpacks");
            export.setToolTipText("Export the debug as a .md (markdown) file" +
                    "\nIt will include your system info and" +
                    "\ndebug timestamp, you can use it to" +
                    "\nreport errors easily and help the developer" +
                    "\nfixing bugs");
            auto_scroll.setToolTipText("Scroll automatically when there's new" +
                    "\noutput in the debug panel");
            check_updates.setToolTipText("Enable version-checks periodically\nCTRL + Click to show changelog");
            curse.setToolTipText("Open a built-in nav browser in curseforge to\ndownload mods or manage installed mods");
            launch.setToolTipText("Open the built-in minecraft launcher to\nlaunch any modpack you have installed");

            //Splitters
            debug_export.setEnabled(false);
            data_debug.setEnabled(false);
            download_create.setEnabled(false);
            text_url.setEnabled(false);
            url_tool.setEnabled(false);
            dco_choose.setEnabled(false);
            dcoc_curse.setEnabled(false);
            dcocc_launch.setEnabled(false);

            data_debug.setDividerLocation(120);

            download_create.setDividerLocation(22);
            download_create.setDividerSize(5);

            dc_open.setDividerLocation(48);
            dc_open.setDividerSize(5);

            dco_choose.setDividerSize(20);
            dco_choose.setDividerLocation(72);

            dcocc_launch.setDividerSize(20);

            SwingUtilities.invokeLater(() -> {
                try {
                    SwingUtilities.updateComponentTreeUI(main_frame);
                    for (Component component : main_frame.getComponents()) {
                        if (component != null) {
                            SwingUtilities.updateComponentTreeUI(component);
                        }
                    }
                } catch (Throwable ignored) {}
            });

            text_url.setDividerLocation(120);

            //Panels
            left_panel.setOpaque(false);
            left_panel.add(dcoc_sc);
            debug.getDebugScrollable().getVerticalScrollBar().setUnitIncrement(15);

            //Check boxes
            auto_scroll.setSelected(c_memory.autoScroll());
            check_updates.setSelected(c_memory.updateChecks());

            auto_scroll.addActionListener(e -> c_memory.saveAutoScroll(auto_scroll.isSelected()));
            check_updates.addActionListener(e -> {
                if (!ctr) {
                    c_memory.saveUpdateCheck(check_updates.isSelected());
                } else {
                    check_updates.setSelected(!check_updates.isSelected());
                    try {
                        showChangelogDialog();
                    } catch (Throwable ex) {
                        Text text = new Text(ex);
                        text.format(Color.INDIANRED, 14);

                        Debug.util.add(text, true);
                    }
                }
            });
        }

        //Just show the frame
        main_frame.pack();
        main_frame.setVisible(true);

        main_frame.add(url_tool);

        Debug.util.add(Text.util.create("This is the debug pane", Color.WHITE, 12), false);
        Debug.util.add(Text.util.create("And here, you will see", Color.WHITE, 12), false);
        Debug.util.add(Text.util.create("a lot of util info", Color.WHITE, 12), false);
        Debug.util.add(Text.util.create("Click \"Export\" to export", Color.WHITE, 12), true);
        Debug.util.add(Text.util.create("the debug as markdown (.md)", Color.WHITE, 12), false);
        Debug.util.add(Text.util.create("formatted file", Color.WHITE, 12), false);

        main_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Cache cache = new Cache();
                new ClientMemory().saveMc(cache.getMcFolder());
            }
        });

        export.addActionListener(e -> {
            try {
                Debug.util.export();
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        });

        download.addActionListener(e -> new AsyncScheduler(() -> {
            if (!getUpdaterDir.exists() && getUpdaterDir.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(getUpdaterDir), Color.LIGHTGREEN, 12), false);

            Cache cache = new Cache();
            File temp_file = new File(getUpdaterDir, url_input.getText().split("/")[url_input.getText().split("/").length - 1]);

            if (cache.getMpuFile() != null)
                temp_file = cache.getMpuFile();

            try {
                if (cache.getMpuFile() == null) {
                    URL url = new URL(url_input.getText());
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        Debug.util.add(Text.util.create("Downloading modpack data from " + url_input.getText() + "<br>to " + Utils.findPath(temp_file), Color.LIGHTGREEN, 12), false);
                        InputStream input = connection.getInputStream();
                        OutputStream out = new FileOutputStream(temp_file);
                        while (input.available() != 0) {
                            out.write(input.read());
                        }
                        input.close();
                        out.close();

                        MPUExt modpack = null;
                        try {
                            MPUExt temp_modpack = new MPUExt(temp_file);
                            String name = temp_modpack.getName();

                            File modpack_folder = new File(getPacksDir, name);
                            if (!modpack_folder.exists() && modpack_folder.mkdirs())
                                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(modpack_folder), Color.LIGHTGREEN, 12), false);

                            File final_file = new File(modpack_folder, name + ".mpu");

                            if (final_file.length() != temp_file.length()) {
                                if (temp_file.renameTo(final_file)) {
                                    modpack = new MPUExt(final_file);
                                    Debug.util.add(Text.util.create("Downloaded modpack " + modpack.getName(), Color.LIGHTGREEN, 12), false);
                                }
                            } else {
                                Files.delete(temp_file.toPath());
                                modpack = new MPUExt(final_file);
                                Debug.util.add(Text.util.create("Downloaded modpack " + modpack.getName(), Color.LIGHTGREEN, 12), false);
                            }
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        } finally {
                            if (modpack != null) {
                                MPUExt finalModpack = modpack;
                                if (!downloading) {
                                    new AsyncScheduler(() -> {
                                        try {
                                            showInstallDialog(finalModpack, false);
                                        } catch (Throwable ex) {
                                            Text text = new Text(ex);
                                            text.format(Color.INDIANRED, 14);

                                            Debug.util.add(text, true);
                                        }
                                    }).run();
                                }
                            } else {
                                Debug.util.add(Text.util.create("Couldn't download modpack ", Color.INDIANRED, 12), false);
                            }
                        }
                    } else {
                        Debug.util.add(Text.util.create("Skipped modpack download | Bad connection request", Color.LIGHTGREEN, 12), false);
                    }
                } else {
                    MPUExt modpack = null;
                    try {
                        MPUExt temp_modpack = new MPUExt(temp_file);
                        String name = temp_modpack.getName();

                        File modpack_folder = new File(getPacksDir, name);
                        if (!modpack_folder.exists() && modpack_folder.mkdirs())
                            Debug.util.add(Text.util.create("Created directory " + Utils.findPath(modpack_folder), Color.LIGHTGREEN, 12), false);

                        File final_file = new File(modpack_folder, name + ".mpu");

                        if (final_file.length() != temp_file.length()) {
                            if (temp_file.renameTo(final_file)) {
                                modpack = new MPUExt(final_file);
                                Debug.util.add(Text.util.create("Downloaded modpack " + modpack.getName(), Color.LIGHTGREEN, 12), false);
                            }
                        } else {
                            modpack = new MPUExt(final_file);
                            Debug.util.add(Text.util.create("Downloaded modpack " + modpack.getName(), Color.LIGHTGREEN, 12), false);
                        }
                    } catch (Throwable ex) {
                        Text text = new Text(ex);
                        text.format(Color.INDIANRED, 14);

                        Debug.util.add(text, true);
                    } finally {
                        if (modpack != null) {
                            MPUExt finalModpack = modpack;
                            if (!downloading) {
                                new AsyncScheduler(() -> {
                                    try {
                                        showInstallDialog(finalModpack, true);
                                    } catch (Throwable ex) {
                                        Text text = new Text(ex);
                                        text.format(Color.INDIANRED, 14);

                                        Debug.util.add(text, true);
                                    }
                                }).run();
                            }
                        } else {
                            Debug.util.add(Text.util.create("Couldn't download modpack ", Color.INDIANRED, 12), false);
                        }
                    }
                }
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }).run());

        create.addActionListener(e -> {
            Creator creator = new Creator();
            creator.initialize();
        });

        open.addActionListener(e -> {
            ModpackOpener opener = new ModpackOpener();
            opener.initialize();
        });

        curse.addActionListener(e -> {
            if (!advised)
                Debug.util.add(Text.util.create("If this is the first time you use CurseForge mod downloader, it will take some minutes to open the window ( to download web browser ), please wait...", Color.LIGHTCORAL, 21), true);

            new AsyncScheduler(() -> {
                CurseDownloader downloader = new CurseDownloader();
                downloader.initialize();
                advised = true;
            }).run();
        });

        launch.addActionListener(e -> {
            Launcher launcher = new Launcher();
            launcher.initialize();
        });

        choose.addActionListener(e -> {
            MinecraftChooser chooser = new MinecraftChooser();
            chooser.initialize();
        });

        url_input.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                boolean isPasting = ctr && e.getKeyCode() == KeyEvent.VK_V;
                boolean isCopying = ctr && e.getKeyCode() == KeyEvent.VK_C;

                String letter = String.valueOf(e.getKeyChar());
                if (!Character.isLetterOrDigit(e.getKeyChar())
                        && e.getKeyCode() != KeyEvent.VK_BACK_SPACE
                        && e.getKeyCode() != KeyEvent.VK_RIGHT
                        && e.getKeyCode() != KeyEvent.VK_LEFT
                        && !letter.equals(".")
                        && !letter.equals("/")
                        && !letter.equals(":")
                        && !isPasting
                        && !isCopying) {
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                boolean isPasting = ctr && e.getKeyCode() == KeyEvent.VK_V;

                SwingUtilities.invokeLater(() -> {
                    try {
                        if (isPasting) {
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            String data = (String) clipboard.getData(DataFlavor.stringFlavor);

                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < data.length(); i++) {
                                char character = data.charAt(i);

                                String letter = String.valueOf(character);
                                if (Character.isLetterOrDigit(character) || letter.equals(".") || letter.equals("/") || letter.equals(":")) {
                                    builder.append(character);
                                }
                            }

                            url_input.setText(builder.toString());
                        }

                        UrlValidator validator = new UrlValidator();

                        if (url_input.getText().isEmpty())
                            url_input.setText("https://example.org/modpack.mpu");

                        boolean valid = validator.isValid(url_input.getText());
                        boolean isMPU = url_input.getText().endsWith(".mpu");

                        if (!isMPU || !valid) {
                            url_input.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 2));
                            download.setEnabled(false);

                            if (isMPU || valid) {
                                if (!valid) {
                                    url_input.setToolTipText("Please enter a valid url" +
                                            "\nExample: https://example.org/modpack.mpu");
                                } else {
                                    url_input.setToolTipText("The specified URL does not" +
                                            "\npoints to an .mpu file" +
                                            "\nExample: https://example.org/modpack.mpu");
                                }
                            } else {
                                url_input.setToolTipText("Fix these problems:" +
                                        "\nEnter a valid URL" +
                                        "\n(https://...) || (http://...)" +
                                        "\n" +
                                        "\nEnter an URL that points to" +
                                        "\nan .mpu file" +
                                        "\nhttps://example.org/modpack.mpu");
                            }

                            if (isPasting) {
                                Utils.displayToolTip(url_input);
                            }
                        } else {
                            download.setEnabled(true);
                            url_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
                            url_input.setToolTipText("Put here the url to an .mpu directory" +
                                    "\n(https://example.org/modpack.mpu)" +
                                    "\nso when you click \"Download\" it will be" +
                                    "\ndownloaded");

                            c_memory.saveURL(url_input.getText());
                        }
                    } catch (Throwable ignored) {
                    }
                });
            }
        });

        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(main_frame);
            for (Component component : main_frame.getComponents()) {
                if (component != null) {
                    SwingUtilities.updateComponentTreeUI(component);
                }
            }
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (check_updates.isSelected()) {
                    if (checks >= 3 && available)
                        shown = false;
                    try {
                        changelog.requestChangelog();

                        String current = app_ver != null ? app_ver : "0";
                        String latest = changelog.getVersion();

                        int c_int = Integer.parseInt(current.replaceAll("\\s", "").replaceAll("[aA-zZ]", "").replace(".", ""));
                        int l_int = Integer.parseInt(latest.replaceAll("\\s", "").replaceAll("[aA-zZ]", "").replace(".", ""));

                        if (c_int != l_int) {
                            if (c_int < l_int) {
                                available = true;
                                if (!shown) {
                                    showChangelogDialog();
                                    shown = true;
                                }
                            }
                        }
                    } catch (Throwable ex) {
                        Text text = new Text(ex);
                        text.format(Color.INDIANRED, 14);

                        Debug.util.add(text, true);
                    }
                    if (available)
                        checks++;
                }
            }
        }, 0, TimeUnit.MINUTES.toMillis(1));
    }

    /**
     * When the app is launched
     *
     * @param args the launch arguments
     */
    public static void main(String[] args) {
        final String url = Utils.findArgument(args, "-UpdaterURL=", "");

        if (url != null && !url.isEmpty())
            modpack_url = url;

        if (modpack_url == null || modpack_url.isEmpty())
            modpack_url = c_memory.loadURL();

        try {
            ToolTipManager.sharedInstance().setDismissDelay((int) TimeUnit.SECONDS.toMillis(61));

            FlatDarkLaf.install();
            UIManager.setLookAndFeel(FlatDarkLaf.class.getName());

            initialize();

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
                synchronized (Updater.class) {
                    switch (ke.getID()) {
                        case KeyEvent.KEY_PRESSED:
                            if (ke.getKeyCode() == KeyEvent.VK_CONTROL) {
                                ctr = true;
                            }
                            break;

                        case KeyEvent.KEY_RELEASED:
                            if (ke.getKeyCode() == KeyEvent.VK_CONTROL) {
                                ctr = false;
                            }
                            break;
                    }
                    return false;
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            //Exit with 1 since 1 is "Something wrong happened"
            System.exit(1);
        }
    }

    public interface external {

        boolean isDownloading = downloading;

        static void checkFiles(final MPUExt modpack) {
            reader = new DataReader(modpack);
            reader.downloadData();
            downloadModpack(modpack, false, true);
        }
    }

    /**
     * Download the modpack mods
     *
     * @param modpack the modpack
     * @param force force the files download
     * @param isCheck is a modpack files check
     */
    private static void downloadModpack(final MPUExt modpack, final boolean force, final boolean isCheck) {
        downloading = true;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!reader.isDownloading()) {
                    cancel();
                    new AsyncScheduler(() -> {
                        try {
                            HashSet<Resource> resources = reader.getMods();
                            if (!resources.isEmpty()) {
                                ResourceDownloader downloader = new ResourceDownloader(modpack, resources, force);
                                downloader.download();

                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (downloader.isDownloaded()) {
                                            cancel();
                                            try {
                                                downloadVersions(modpack, force, isCheck);
                                            } catch (Throwable ex) {
                                                Text text = new Text(ex);
                                                text.format(Color.INDIANRED, 14);

                                                Debug.util.add(text, true);
                                            }
                                        }
                                    }
                                }, 0, 1);
                            } else {
                                try {
                                    downloadVersions(modpack, force, isCheck);
                                } catch (Throwable ex) {
                                    Text text = new Text(ex);
                                    text.format(Color.INDIANRED, 14);

                                    Debug.util.add(text, true);
                                }
                            }
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }).run();
                }
            }
        }, 0, TimeUnit.SECONDS.toMillis(1));
    }

    /**
     * Download the modpack versions
     *
     * @param modpack the modpack
     * @param force force the files download
     * @param isCheck is a modpack files check
     */
    private static void downloadVersions(final MPUExt modpack, final boolean force, final boolean isCheck) throws Throwable {
        HashSet<Resource> resources = reader.getVersions();
        if (!resources.isEmpty()) {
            ResourceDownloader downloader = new ResourceDownloader(modpack, resources, force);
            downloader.download();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (downloader.isDownloaded()) {
                        cancel();
                        try {
                            downloadResources(modpack, force, isCheck);
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }
                }
            }, 0, 1);
        } else {
            try {
                downloadResources(modpack, force, isCheck);
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }
    }

    /**
     * Download the modpack resourcepacks
     *
     * @param modpack the modpack
     * @param force force the files download
     * @param isCheck is a modpack files check
     */
    private static void downloadResources(final MPUExt modpack, final boolean force, final boolean isCheck) throws Throwable {
        HashSet<Resource> resources = reader.getResourcepacks();
        if (!resources.isEmpty()) {
            ResourceDownloader downloader = new ResourceDownloader(modpack, resources, force);
            downloader.download();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (downloader.isDownloaded()) {
                        cancel();
                        try {
                            downloadShaders(modpack, force, isCheck);
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }
                }
            }, 0, 1);
        } else {
            try {
                downloadShaders(modpack, force, isCheck);
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }
    }

    /**
     * Download the modpack shaders
     *
     * @param modpack the modpack
     * @param force force the files download
     * @param isCheck is a modpack files check
     */
    private static void downloadShaders(final MPUExt modpack, final boolean force, final boolean isCheck) throws Throwable {
        HashSet<Resource> resources = reader.getShaderpacks();
        if (!resources.isEmpty()) {
            ResourceDownloader downloader = new ResourceDownloader(modpack, resources, force);
            downloader.download();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (downloader.isDownloaded()) {
                        cancel();
                        try {
                            downloadWorlds(modpack, force, isCheck);
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }
                }
            }, 0, 1);
        } else {
            try {
                downloadWorlds(modpack, force, isCheck);
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }
    }

    /**
     * Download the modpack worlds
     *
     * @param modpack the modpack
     * @param force force the files download
     * @param isCheck is a modpack files check
     */
    private static void downloadWorlds(final MPUExt modpack, final boolean force, final boolean isCheck) throws Throwable {
        HashSet<Resource> resources = reader.getWorlds();
        if (!resources.isEmpty()) {
            ResourceDownloader downloader = new ResourceDownloader(modpack, resources, force);
            downloader.download();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (downloader.isDownloaded()) {
                        cancel();
                        Debug.util.add(Text.util.create("Modpack " + modpack.getName() + " info:", Color.WHITE, 12), true);
                        Debug.util.add(Text.util.create("Version: " + modpack.getVersion(), Color.WHITE, 12), false);
                        Debug.util.add(Text.util.create("Mc version: " + modpack.getMcVersion() + " based on " + modpack.getRealVersion(), Color.WHITE, 12), false);
                        Debug.util.add(Text.util.create("Authors: ", Color.WHITE, 12), false);
                        for (String author : modpack.getAuthors())
                            Debug.util.add(Text.util.create(author, Color.LIGHTGRAY, 12), false);
                        Debug.util.add(modpack.getDescription(), true);
                        downloading = false;

                        if (!isCheck) {
                            Launcher launcher = new Launcher(modpack);
                            launcher.initialize();
                        }
                    }
                }
            }, 0, 1);
        } else {
            Debug.util.add(Text.util.create("Modpack " + modpack.getName() + " info:", Color.WHITE, 12), true);
            Debug.util.add(Text.util.create("Version: " + modpack.getVersion(), Color.WHITE, 12), false);
            Debug.util.add(Text.util.create("Mc version: " + modpack.getMcVersion() + " based on " + modpack.getRealVersion(), Color.WHITE, 12), false);
            Debug.util.add(Text.util.create("Authors: ", Color.WHITE, 12), false);
            for (String author : modpack.getAuthors())
                Debug.util.add(Text.util.create(author, Color.LIGHTGRAY, 12), false);
            Debug.util.add(modpack.getDescription(), true);
            downloading = false;

            if (!isCheck) {
                Launcher launcher = new Launcher(modpack);
                launcher.initialize();
            }
        }

        Cache cache = new Cache();
        File copy = new File(Utils.getPackMc(modpack), modpack.getName() + ".mpu");
        File original = new File(Utils.getPackDir(modpack.getName()), modpack.getName() + ".mpu");

        if (copy.exists())
            Files.delete(copy.toPath());

        Files.copy(original.toPath(), copy.toPath());

        cache.saveMpuFile(null);
    }

    private static void showInstallDialog(final MPUExt modpack, boolean force) throws Throwable {
        Cache cache = new Cache();

        ImageIcon icon = null;
        if (cache.getIco() != null)
            icon = new ImageIcon(cache.getIco());

        int i;
        File copy = new File(Utils.getPackMc(modpack), modpack.getName() + ".mpu");

        if (copy.exists() && !force) {
            MPUExt copy_mpu = new MPUExt(copy);
            int copy_last = Integer.parseInt(copy_mpu.getVersion().replaceAll("[aA-zZ]", "").replace(".", ""));
            int orig_last = Integer.parseInt(modpack.getVersion().replaceAll("[aA-zZ]", "").replace(".", ""));

            if (copy_last < orig_last) {
                i = JOptionPane.showOptionDialog(main_frame,
                        "You are about to update\nmodpack " + modpack.getName(),
                        "Modpack " + modpack.getName() + " update tool", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, null, null);
            } else {
                i = JOptionPane.showOptionDialog(main_frame,
                        "You are about to check\nmodpack " + modpack.getName() + " files",
                        "Modpack " + modpack.getName() + " files check tool", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, null, null);
            }
        } else {
            i = JOptionPane.showOptionDialog(main_frame,
                    "You are about to install\nmodpack " + modpack.getName(),
                    "Modpack " + modpack.getName() + " installation tool", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, null, null);
        }

        if (i == JOptionPane.YES_OPTION) {
            try {
                reader = new DataReader(modpack);
                reader.downloadData();
                downloadModpack(modpack, force, false);
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }
    }

    private static void showChangelogDialog() {
        JLabel info = new JLabel(changelog.toString());

        Cache cache = new Cache();
        ImageIcon icon = null;
        if (cache.getIco() != null)
            icon = new ImageIcon(cache.getIco());

        JOptionPane.showOptionDialog(main_frame,
                info,
                "Modpack Updater changelog", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, null, null);
    }
}
