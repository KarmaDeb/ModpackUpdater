package ml.karmaconfigs.modpackupdater.manager;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.project.CurseProject;
import ml.karmaconfigs.modpackupdater.utils.*;
import ml.karmaconfigs.modpackupdater.utils.Color;
import org.apache.logging.log4j.core.util.FileUtils;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefRequest;
import org.jetbrains.annotations.Nullable;
import org.panda_lang.pandomium.Pandomium;
import org.panda_lang.pandomium.settings.PandomiumSettings;
import org.panda_lang.pandomium.wrapper.PandomiumBrowser;
import org.panda_lang.pandomium.wrapper.PandomiumClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.Timer;

public final class CurseDownloader {

    private final static JFrame main_frame = new JFrame("CurseForge downloader");

    private final static Pandomium pandomium = new Pandomium(PandomiumSettings.getDefaultSettings());

    static {
        pandomium.initialize();
    }

    private final static JButton back = new JButton("Back");
    private final static JButton forward = new JButton("Forward");
    private final static JButton open_in_browser = new JButton("Browser");
    private final static JButton download = new JButton("Download");
    private final static JButton manage = new JButton("Manage mods");
    private final static JTextArea url_input = new JTextArea("https://www.curseforge.com/minecraft/mc-mods");

    private final static PandomiumClient client = pandomium.createClient();
    private final static PandomiumBrowser browser = client.loadURL("https://www.curseforge.com/minecraft/mc-mods");

    private final static Dimension dimension = new Dimension(1200, 800);

    private static boolean initialized = false;

    public final void initialize() {
        if (!initialized) {
            Cache cache = new Cache();
            main_frame.setIconImage(cache.getIco());

            url_input.setEditable(false);

            main_frame.setMinimumSize(dimension);
            main_frame.setMaximumSize(dimension);
            main_frame.setPreferredSize(dimension);
            main_frame.setSize(dimension);

            main_frame.setResizable(false);

            JSplitPane buttons = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, back, forward);
            JSplitPane but_url = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttons, url_input);
            JSplitPane buttons_two = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, open_in_browser, download);
            JSplitPane buttons_three = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttons_two, manage);
            JSplitPane but3_url = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, but_url, buttons_three);
            JSplitPane final_browser = new JSplitPane(JSplitPane.VERTICAL_SPLIT, but3_url, browser.toAWTComponent());

            buttons.setEnabled(false);
            but_url.setEnabled(false);
            buttons_two.setEnabled(false);
            but3_url.setEnabled(false);
            final_browser.setEnabled(false);

            but3_url.setDividerLocation(907);

            main_frame.add(final_browser);
            main_frame.pack();
            main_frame.setVisible(true);

            main_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            Utils.toCenter(main_frame);

            back.setToolTipText("Go to the previous page");
            forward.setToolTipText("Return to the previous page");
            open_in_browser.setToolTipText("Open the current page in your browser");
            download.setToolTipText("Download the current mod");
            manage.setToolTipText("Open the mod manager tool window");
            url_input.setToolTipText("The current page url");

            client.getCefClient().addDisplayHandler(new CefDisplayHandlerAdapter() {
                @Override
                public boolean onTooltip(CefBrowser cefBrowser, String s) {
                    return true;
                }

                @Override
                public boolean onConsoleMessage(CefBrowser cefBrowser, CefSettings.LogSeverity logSeverity, String s, String s1, int i) {
                    return true;
                }
            });

            client.getCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
                @Override
                public void onLoadStart(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest.TransitionType transitionType) {
                    super.onLoadStart(cefBrowser, cefFrame, transitionType);
                    if (cefFrame.getURL().startsWith("https://www.curseforge.com/")) {
                        if (!cefBrowser.getURL().startsWith("https://www.curseforge.com/minecraft/mc-mods") || cefBrowser.getURL().endsWith("/files") || cefBrowser.getURL().contains("/relations")) {
                            cefBrowser.goBack();
                        } else {
                            if (cefBrowser.getURL().startsWith("https://www.curseforge.com/minecraft/mc-mods")) {
                                url_input.setText(cefBrowser.getURL());
                            }
                        }
                        if (cefBrowser.getURL().contains("/download")) {
                            cefBrowser.goBack();
                            try {
                                CurseProject mod = getMod();
                                if (mod != null) {
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            DownloadVersionDialog dialog = new DownloadVersionDialog();
                                            dialog.show(mod);
                                        } catch (Throwable ex) {
                                            Text text = new Text(ex);
                                            text.format(Color.INDIANRED, 14);

                                            Debug.util.add(text, true);
                                        }
                                    });
                                }
                            } catch (Throwable ex) {
                                Text text = new Text(ex);
                                text.format(Color.INDIANRED, 14);

                                Debug.util.add(text, true);
                            }
                        }
                    } else {
                        if (!cefBrowser.getURL().startsWith("https://www.curseforge.com/minecraft/mc-mods"))
                            cefBrowser.goBack();
                    }
                }
            });

            manage.addActionListener(e -> {
                ModManager manager = new ModManager();
                manager.initialize();
            });

            back.addActionListener(e -> {
                if (browser.getCefBrowser().canGoBack()) {
                    browser.getCefBrowser().goBack();
                } else{
                    main_frame.setVisible(false);
                }
            });

            forward.addActionListener(e -> {
                if (browser.getCefBrowser().canGoForward())
                    browser.getCefBrowser().goForward();
            });

            open_in_browser.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(new URL(browser.getCefBrowser().getURL()).toURI());
                } catch (Throwable ex) {
                    Debug.util.add(Text.util.create("Your system is not compatible with java browser", Color.INDIANRED, 12), true);
                }
            });

            download.addActionListener(e -> {
                try {
                    CurseProject mod = getMod();
                    if (mod != null) {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                DownloadVersionDialog dialog = new DownloadVersionDialog();
                                dialog.show(mod);
                            } catch (Throwable ex) {
                                Text text = new Text(ex);
                                text.format(Color.INDIANRED, 14);

                                Debug.util.add(text, true);
                            }
                        });
                    }
                } catch (Throwable ex) {
                    Text text = new Text(ex);
                    text.format(Color.INDIANRED, 14);

                    Debug.util.add(text, true);
                }
            });



            initialized = true;
        } else {
            main_frame.setVisible(true);
            main_frame.toFront();
        }
    }

    /**
     * Tries to get the mod directly from
     * specified mod name or mod name
     * initials
     *
     * @return a manager project
     */
    @Nullable
    private CurseProject getMod() throws Throwable {
        String url_path = browser.getCefBrowser().getURL().replace("https://www.curseforge.com/minecraft/", "").replace("/download", "");
        System.out.println(url_path);
        Optional<CurseProject> direct_project = CurseAPI.project("minecraft/" + url_path);

        return direct_project.orElse(null);
    }
}

final class ModManager {

    private final static JFrame manager_frame = new JFrame("Mod manager");

    private final static Cache cache = new Cache();

    private final static Dimension size = new Dimension(400, 600);

    private final static JPanel mod_list = new JPanel();
    private final static JScrollPane mod_list_scroll = new JScrollPane(mod_list);

    private final static HashSet<File> selected_mods = new HashSet<>();
    private final static HashSet<File> disabled_mods = new HashSet<>();

    private final static HashMap<File, Long> last_modified = new HashMap<>();
    private final static HashMap<JCheckBox, File> box_file = new HashMap<>();

    private static boolean initialized = false;

    public final void initialize() {
        if (!initialized) {
            manager_frame.setIconImage(cache.getIco());

            mod_list.setLayout(new BoxLayout(mod_list, BoxLayout.Y_AXIS));
            mod_list_scroll.getVerticalScrollBar().setUnitIncrement(15);
            mod_list_scroll.getHorizontalScrollBar().setUnitIncrement(15);

            JPopupMenu menu = new JPopupMenu("Mods manager");
            JMenuItem disable_selected = new JMenuItem("Disable selected");
            JMenuItem enable_selected = new JMenuItem("Enable selected");
            JMenuItem remove_selected = new JMenuItem("Delete selected");

            mod_list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        menu.show(manager_frame, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
                        menu.setLocation(MouseInfo.getPointerInfo().getLocation());
                    }
                }
            });

            disable_selected.addActionListener(e -> {
                try {
                    for (File selected : selected_mods) {
                        File file = new File(selected.getAbsolutePath());
                        if (!disabled_mods.contains(file)) {
                            File dest = new File(file.getAbsolutePath().replace(file.getName(), file.getName().replace(".jar", "")));
                            Files.move(file.toPath(), dest.toPath());
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });

            enable_selected.addActionListener(e -> {
                try {
                    for (File selected : selected_mods) {
                        File file = new File(selected.getAbsolutePath());
                        if (disabled_mods.contains(file)) {
                            File dest = new File(file.getAbsolutePath().replace(file.getName(), file.getName() + ".jar"));
                            Files.move(file.toPath(), dest.toPath());
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });

            remove_selected.addActionListener(e -> {
                try {
                    for (File selected : selected_mods) {
                        File file = new File(selected.getAbsolutePath());
                        Files.delete(file.toPath());
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });

            menu.add(disable_selected);
            menu.add(enable_selected);
            menu.add(remove_selected);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    File mods_folder = new File(cache.getMcFolder(), "mods");

                    disable_selected.setToolTipText("Disable all the selected mods ( " + selected_mods.size() + " )");
                    enable_selected.setToolTipText("Enable all the selected mods ( " + selected_mods.size() + " )");
                    remove_selected.setToolTipText("Remove all the selected mods ( " + selected_mods.size() + " )");

                    if (hasModifications(getSubdirectories(mods_folder))) {
                        selected_mods.clear();
                        box_file.clear();
                        fetchModifications(mods_folder);
                        disabled_mods.clear();
                        mod_list.removeAll();
                        listMods(mods_folder);
                        addMods("Mods", mods_folder);
                    }
                }
            }, 0, 1);

            manager_frame.setMinimumSize(size);
            manager_frame.setMaximumSize(size);
            manager_frame.setPreferredSize(size);
            manager_frame.setSize(size);
            manager_frame.setResizable(false);

            manager_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            manager_frame.add(mod_list_scroll);

            manager_frame.pack();
            initialized = true;

            Utils.toCenter(manager_frame);
        }

        manager_frame.setVisible(true);
    }

    /**
     * List all the mods of the mods folder
     *
     * @param folder the mods folders
     */
    private void listMods(final File folder) {
        File[] mods = folder.listFiles();
        for (File mod : mods) {
            if (mod.isDirectory()) {
                listMods(mod);
            } else {
                String extension = FileUtils.getFileExtension(mod);
                if (!extension.equals("jar")) {
                    disabled_mods.add(mod);
                }
            }
        }
    }

    private void addMods(final String name, final File folder) {
        Text divider = Text.util.create("<br>-------------------<br><br>", Color.ORANGERED, 12);
        JLabel space = new JLabel("<html>" + divider.getText(true) + "</html>");
        space.setName(name + "_space");

        if (name.equals("Mods")) {
            Text text = Text.util.create("Instructions:<br>" +
                    "Right click folder name to manage folder<br>" +
                    "Right click mod to manage mod<br>" +
                    "Right click blank space to manage selected<br>" +
                    divider.getText(true), Color.CORNFLOWERBLUE, 12);
            JLabel title = new JLabel("<html>" + text.getText(true) + "</html>");
            mod_list.add(title);
            mod_list.add(space);
        }

        File[] mods = folder.listFiles();
        JLabel label = new JLabel("<html>Folder:<br>" + name + "<br><br></html>");
        label.setName(name);

        Dimension size = new Dimension(40, 40);

        label.setMinimumSize(size);
        label.setMaximumSize(size);
        label.setPreferredSize(size);
        label.setSize(size);

        mod_list.add(label);

        ArrayList<File> folders = new ArrayList<>();
        HashSet<JCheckBox> boxes = new HashSet<>();

        if (mods.length > 0) {
            for (File mod : mods) {
                if (mod.isDirectory()) {
                    folders.add(mod);
                } else {
                    if (mod.getName().endsWith(".jar")) {
                        JCheckBox mod_item = new JCheckBox(mod.getName().replace(".jar", ""));
                        JPopupMenu mod_manager = new JPopupMenu("Mod " + mod_item.getText() + " manager");

                        box_file.put(mod_item, mod);

                        JMenuItem disable = new JMenuItem("Disable");
                        JMenuItem delete = new JMenuItem("Delete");

                        disable.setToolTipText("Disable the mod " + mod.getName().replace(".jar", ""));
                        delete.setToolTipText("Remove the mod " + mod.getName().replace(".jar", ""));

                        disable.addActionListener(ev -> {
                            try {
                                File dest_file = new File(mod.getAbsolutePath().replace(mod.getName(), mod.getName().replace(".jar", "")));
                                Files.move(mod.toPath(), dest_file.toPath());
                            } catch (Throwable ignored) {
                            }

                            mod_manager.setVisible(false);
                        });

                        delete.addActionListener(ev -> {
                            try {
                                Files.delete(mod.toPath());
                            } catch (Throwable ignored) {
                            }

                            mod_manager.setVisible(false);
                        });

                        mod_manager.add(disable);
                        mod_manager.add(delete);

                        mod_item.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                displayMenu(e);
                            }

                            private void displayMenu(MouseEvent e) {
                                if (SwingUtilities.isRightMouseButton(e)) {
                                    SwingUtilities.invokeLater(() -> {
                                        mod_item.setBackground(java.awt.Color.gray);
                                        mod_item.setText("<html>" + Text.util.create(mod_item.getText(), ml.karmaconfigs.modpackupdater.utils.Color.WHITE, 12).getText(true) + "</html>");
                                    });
                                    mod_manager.show(manager_frame, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
                                    mod_manager.setLocation(MouseInfo.getPointerInfo().getLocation());
                                }
                            }
                        });

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (!mod_manager.isVisible()) {
                                    SwingUtilities.invokeLater(() -> {
                                        mod_item.setText(mod.getName().replace(".jar", ""));
                                        mod_item.setBackground(manager_frame.getBackground());
                                    });
                                }
                            }
                        }, 0, 1);

                        mod_item.addActionListener(e -> {
                            if (mod_item.isSelected()) {
                                selected_mods.add(mod);
                            } else {
                                selected_mods.remove(mod);
                            }
                        });

                        mod_list.add(mod_item);
                        boxes.add(mod_item);
                    }
                }
            }
        }

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu folder_manager = new JPopupMenu("Folder manager");

                    JMenuItem select_all = new JMenuItem("Select all");
                    JMenuItem unselect_all = new JMenuItem("Unselect all");
                    JMenuItem invert_selection = new JMenuItem("Invert selection");

                    select_all.setToolTipText("Select all the mods located at " + folder.getName());
                    unselect_all.setToolTipText("Unselect all the located at " + folder.getName());
                    invert_selection.setToolTipText("Invert the mod selections located at " + folder.getName());

                    select_all.addActionListener(ev -> {
                        for (JCheckBox box : boxes) {
                            box.setSelected(true);
                            selected_mods.add(box_file.get(box));
                        }
                    });

                    unselect_all.addActionListener(ev -> {
                        for (JCheckBox box : boxes) {
                            box.setSelected(false);
                            selected_mods.remove(box_file.get(box));
                        }
                    });

                    invert_selection.addActionListener(ev -> {
                        for (JCheckBox box : boxes) {
                            if (box.isSelected()) {
                                selected_mods.remove(box_file.get(box));
                            } else {
                                selected_mods.add(box_file.get(box));
                            }
                            box.setSelected(!box.isSelected());
                        }
                    });

                    folder_manager.add(select_all);
                    folder_manager.add(unselect_all);
                    folder_manager.add(invert_selection);

                    folder_manager.show(manager_frame, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
                    folder_manager.setLocation(MouseInfo.getPointerInfo().getLocation());
                }
            }
        });

        mod_list.add(space);

        if (!folders.isEmpty()) {
            for (File _folder : folders) {
                addMods(_folder.getName(), _folder);
            }
        }

        if (name.equals("Mods")) {
            JLabel disabled = new JLabel("<html>Disabled mods<br><br></html>");
            disabled.setName("disabled");

            Dimension disabled_size = new Dimension(45, 40);

            disabled.setMinimumSize(disabled_size);
            disabled.setMaximumSize(disabled_size);
            disabled.setPreferredSize(disabled_size);
            disabled.setSize(disabled_size);

            mod_list.add(disabled);

            HashSet<JCheckBox> disabled_boxes = new HashSet<>();

            if (!disabled_mods.isEmpty()) {
                for (File mod : disabled_mods) {
                    JCheckBox mod_item = new JCheckBox(mod.getName());

                    box_file.put(mod_item, mod);

                    JPopupMenu mod_manager = new JPopupMenu("Mod " + mod_item.getText() + " manager");

                    JMenuItem enable = new JMenuItem("Enable");
                    JMenuItem delete = new JMenuItem("Delete");

                    enable.setToolTipText("Enable the mod " + mod.getName());
                    delete.setToolTipText("Remove the mod " + mod.getName());

                    enable.addActionListener(ev -> {
                        try {
                            File dest_file = new File(mod.getAbsolutePath().replace(mod.getName(), mod.getName() + ".jar"));
                            Files.move(mod.toPath(), dest_file.toPath());
                        } catch (Throwable ignored) {
                        }

                        mod_manager.setVisible(false);
                    });

                    delete.addActionListener(ev -> {
                        try {
                            Files.delete(mod.toPath());
                        } catch (Throwable ignored) {
                        }

                        mod_manager.setVisible(false);
                    });

                    mod_manager.add(enable);
                    mod_manager.add(delete);

                    mod_item.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            displayMenu(e);
                        }

                        private void displayMenu(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                SwingUtilities.invokeLater(() -> {
                                    mod_item.setBackground(java.awt.Color.gray);
                                    mod_item.setText("<html>" + Text.util.create(mod_item.getText(), ml.karmaconfigs.modpackupdater.utils.Color.WHITE, 12).getText(true) + "</html>");
                                });
                                mod_manager.show(manager_frame, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
                                mod_manager.setLocation(MouseInfo.getPointerInfo().getLocation());
                            }
                        }
                    });

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (!mod_manager.isVisible()) {
                                SwingUtilities.invokeLater(() -> {
                                    mod_item.setText(mod.getName().replace(".jar", ""));
                                    mod_item.setBackground(manager_frame.getBackground());
                                });
                            }
                        }
                    }, 0, 1);

                    mod_item.addActionListener(e -> {
                        if (mod_item.isSelected()) {
                            selected_mods.add(mod);
                        } else {
                            selected_mods.remove(mod);
                        }
                    });

                    disabled_boxes.add(mod_item);
                    mod_list.add(mod_item);
                }
            }

            disabled.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu folder_manager = new JPopupMenu("Disabled manager");

                        JMenuItem select_all = new JMenuItem("Select all");
                        JMenuItem unselect_all = new JMenuItem("Unselect all");
                        JMenuItem invert_selection = new JMenuItem("Invert selection");

                        select_all.setToolTipText("Select all the disabled mods");
                        unselect_all.setToolTipText("Unselect all the disabled mods");
                        invert_selection.setToolTipText("Invert the selection of disabled mods");

                        select_all.addActionListener(ev -> {
                            for (JCheckBox box : disabled_boxes) {
                                box.setSelected(true);
                                selected_mods.add(box_file.get(box));
                            }
                        });

                        unselect_all.addActionListener(ev -> {
                            for (JCheckBox box : disabled_boxes) {
                                box.setSelected(false);
                                selected_mods.remove(box_file.get(box));
                            }
                        });

                        invert_selection.addActionListener(ev -> {
                            for (JCheckBox box : disabled_boxes) {
                                if (box.isSelected()) {
                                    selected_mods.remove(box_file.get(box));
                                } else {
                                    selected_mods.add(box_file.get(box));
                                }
                                box.setSelected(!box.isSelected());
                            }
                        });

                        folder_manager.add(select_all);
                        folder_manager.add(unselect_all);
                        folder_manager.add(invert_selection);

                        folder_manager.show(manager_frame, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
                        folder_manager.setLocation(MouseInfo.getPointerInfo().getLocation());
                    }
                }
            });
        }

        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(mod_list);
            for (Component component : mod_list.getComponents()) {
                if (component != null) {
                    SwingUtilities.updateComponentTreeUI(component);
                }
            }
        });
    }

    /**
     * Fetch modifications date for each
     * folder
     *
     * @param main_dir the main directory
     */
    private void fetchModifications(final File main_dir) {
        HashSet<File> directories = getSubdirectories(main_dir);

        for (File dir : directories) {
            last_modified.put(dir, dir.lastModified());
        }
    }

    /**
     * Get the sub-directories of the specified directory
     *
     * @param main_dir the starting directory
     * @return a list of sub-folders
     */
    private HashSet<File> getSubdirectories(final File main_dir) {
        HashSet<File> directories = new HashSet<>();
        directories.add(main_dir);
        for (File file : main_dir.listFiles()) {
            if (file.isDirectory()) {
                directories.addAll(getSubdirectories(file));
            }
        }

        return directories;
    }

    /**
     * Check if the folders has modifications
     *
     * @param folders the folders to check
     * @return if the folder and sub-folders have
     * modifications
     */
    private boolean hasModifications(final HashSet<File> folders) {
        for (File folder : folders) {
            if (folder.isDirectory()) {
                boolean modified = last_modified.getOrDefault(folder, 0L) != folder.lastModified();
                if (modified) {
                    return true;
                }
            }
        }

        return false;
    }
}