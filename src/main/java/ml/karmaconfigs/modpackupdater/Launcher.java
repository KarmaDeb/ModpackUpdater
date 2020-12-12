package ml.karmaconfigs.modpackupdater;

import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.utils.*;
import ml.karmaconfigs.modpackupdater.utils.Color;
import org.jetbrains.annotations.Nullable;
import tagapi_3.API_Interface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public final class Launcher {

    private final static JFrame launcher_frame = new JFrame("Modpack launcher");

    private final static Cache cache = new Cache();

    private final static Dimension size = new Dimension(355, 190);

    private final static JComboBox<String> modpacks = new JComboBox<>();

    private final static JButton launch = new JButton("Launch");
    private final static JButton launch_vanilla = new JButton("Launch as vanilla");

    private final static JLabel modpack_label = new JLabel("Modpack to launch");
    private final static JLabel name_label = new JLabel("Client name");
    private final static JLabel min_label = new JLabel("Minecraft min memory");
    private final static JLabel max_label = new JLabel("Minecraft max memory");

    private final static JTextArea name = new JTextArea();
    private final static JTextArea min = new JTextArea();
    private final static JTextArea max = new JTextArea();
    private final static JTextArea java_location = new JTextArea();

    private final static JScrollPane loc_scroll = new JScrollPane(java_location);

    private final static HashMap<String, MPUExt> name_modpack = new HashMap<>();

    private final static JSplitPane full_modpack = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, modpack_label, modpacks);
    private final static JSplitPane full_name = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, name_label, name);
    private final static JSplitPane full_min = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, min_label, min);
    private final static JSplitPane full_max = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, max_label, max);

    private final static JSplitPane memory = new JSplitPane(JSplitPane.VERTICAL_SPLIT, full_min, full_max);
    private final static JSplitPane name_mem = new JSplitPane(JSplitPane.VERTICAL_SPLIT, full_name, memory);
    private final static JSplitPane modpacks_name = new JSplitPane(JSplitPane.VERTICAL_SPLIT, full_modpack, name_mem);
    private final static JSplitPane buttons = new JSplitPane(JSplitPane.VERTICAL_SPLIT, launch, launch_vanilla);
    private final static JSplitPane panel_buttons = new JSplitPane(JSplitPane.VERTICAL_SPLIT, modpacks_name, buttons);

    private static boolean initialized = false;

    public Launcher() {}

    /**
     * Initialize the launcher with the specified
     * modpack as selected
     *
     * @param openWith the modpack
     */
    public Launcher(final MPUExt openWith) {
        Utils.l_memory.saveModpack(openWith);
    }

    public final void initialize() {
        Utils.downloadJava();
        modpacks.removeAllItems();
        name_modpack.clear();
        for (MPUExt modpack : Utils.getModpacks()) {
            modpacks.addItem(modpack.getName());
            name_modpack.put(modpack.getName(), modpack);
        }

        if (!initialized) {
            launcher_frame.setIconImage(cache.getIco());

            full_modpack.setDividerLocation(145);
            full_name.setDividerLocation(145);
            full_min.setDividerLocation(145);
            full_max.setDividerLocation(145);
            full_modpack.setEnabled(false);
            full_name.setEnabled(false);
            full_min.setEnabled(false);
            full_max.setEnabled(false);

            memory.setEnabled(false);
            name_mem.setEnabled(false);
            modpacks_name.setEnabled(false);
            buttons.setEnabled(false);
            panel_buttons.setEnabled(false);

            java_location.setEditable(false);
            loc_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            loc_scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            launcher_frame.setMinimumSize(size);
            launcher_frame.setMaximumSize(size);
            launcher_frame.setPreferredSize(size);
            launcher_frame.setSize(size);
            launcher_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            launcher_frame.setResizable(false);

            launcher_frame.add(panel_buttons);

            launch.addActionListener(e -> {
                if (cache.isLaunching() || cache.isDownloadingJava()) {
                    if (cache.isDownloadingJava()) {
                        Debug.util.add(Text.util.create("Please wait until the tool downloads and unzips java for minecraft", Color.INDIANRED, 12), true);
                    }
                    return;
                }

                MPUExt modpack = getSelectedModpack();
                if (modpack != null) {
                    Cache cache = new Cache();
                    cache.saveModpackMc(modpack);
                    new AsyncScheduler(() -> {
                        API_Interface API = new API_Interface();
                        API.setMemory(Utils.l_memory.getMaxMemory());
                        API.setMinMemory(Utils.l_memory.getMinMemory());
                        API.downloadVersionManifest();
                        API.downloadMinecraft(modpack.getMcVersion(), false);
                        API.downloadProfile(Utils.l_memory.getName());
                        API.setJavaPath(Utils.javaDir.getAbsolutePath().replaceAll("\\\\", "/") + "/bin/javaw.exe");
                        API.runMinecraft(Utils.l_memory.getName(), modpack.getMcVersion(), true, false);
                    }).run();
                }
            });

            launch_vanilla.addActionListener(e -> {
                if (cache.isLaunching() || cache.isDownloadingJava()) {
                    if (cache.isDownloadingJava()) {
                        Debug.util.add(Text.util.create("Please wait until the tool downloads and unzips java for minecraft", Color.INDIANRED, 12), true);
                    }
                    return;
                }

                MPUExt modpack = getSelectedModpack();
                if (modpack != null) {
                    Cache cache = new Cache();
                    cache.saveModpackMc(modpack);
                    new AsyncScheduler(() -> {
                        API_Interface API = new API_Interface();
                        API.setMemory(Utils.l_memory.getMaxMemory());
                        API.setMinMemory(Utils.l_memory.getMinMemory());
                        API.downloadVersionManifest();
                        API.downloadMinecraft(modpack.getRealVersion(), false);
                        API.downloadProfile(Utils.l_memory.getName());
                        API.setJavaPath(Utils.javaDir.getAbsolutePath().replaceAll("\\\\", "/") + "/bin/javaw.exe");
                        API.runMinecraft(Utils.l_memory.getName(), modpack.getRealVersion(), true, false);
                    }).run();
                }
            });

            modpacks.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    MPUExt last_selection = Utils.l_memory.loadModpack();
                    MPUExt current = getSelectedModpack();
                    if (last_selection != null) {
                        if (current != null) {
                            if (!last_selection.getName().equals(current.getName()))
                                Utils.l_memory.saveModpack(current);
                        }
                    } else {
                        if (current != null)
                            Utils.l_memory.saveModpack(current);
                    }
                }
            });

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        boolean allValid = !name.getText().replaceAll("\\s", "").isEmpty() && !min.getText().replaceAll("\\s", "").isEmpty() && !max.getText().replaceAll("\\s", "").isEmpty();

                        String name_text = name.getText();
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < name_text.length(); i++) {
                            try {
                                char letter = name_text.charAt(i);
                                if (Character.isLetterOrDigit(letter)
                                        || String.valueOf(letter).equals("_")) {
                                    builder.append(letter);
                                }
                            } catch (Throwable ignored) {
                                break;
                            }
                        }
                        name_text = builder.toString();
                        name.setText(name_text);
                        if (name_text.length() > 16) {
                            name.setText(name_text.substring(0, 16));
                        }
                        if (name.getText().replaceAll("\\s", "").isEmpty()) {
                            name.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 2));
                            name.setToolTipText("Please write a valid name, a valid name is\n" +
                                    "any name that contains aA-zZ, 0-9 or _, with 16\n" +
                                    "characters max.");
                        } else {
                            name.setBorder(BorderFactory.createLineBorder(launcher_frame.getBackground(), 0));
                            name.setToolTipText("This will be used by the tool when launching minecraft\n" +
                                    "to set your client name...");
                        }
                        builder = new StringBuilder();

                        String min_text = min.getText();
                        for (int i = 0; i < Math.max(4, min_text.length()); i++) {
                            try {
                                char letter = min_text.charAt(i);
                                if (Character.isDigit(letter)) {
                                    builder.append(letter);
                                }
                            } catch (Throwable ignored) {
                                break;
                            }
                        }
                        min_text = builder.toString();
                        min.setText(min_text);
                        if (min_text.length() > 4) {
                            min.setText(min_text.substring(0, 4));
                        }
                        if (min.getText().replaceAll("\\s", "").isEmpty()) {
                            min.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 2));
                            min.setToolTipText("Please write a valid ram amount in MB\n" +
                                    "( 4 digits max )");
                        } else {
                            min.setBorder(BorderFactory.createLineBorder(launcher_frame.getBackground(), 0));
                            min.setToolTipText("The minimum amount of ram that minecraft must use");
                        }
                        builder = new StringBuilder();

                        String max_text = max.getText();
                        for (int i = 0; i < Math.max(4, max_text.length()); i++) {
                            try {
                                char letter = max_text.charAt(i);
                                if (Character.isDigit(letter)) {
                                    builder.append(letter);
                                }
                            } catch (Throwable ignored) {
                                break;
                            }
                        }
                        max_text = builder.toString();
                        max.setText(max_text);
                        if (max_text.length() > 4) {
                            max.setText(max_text.substring(0, 4));
                        }
                        if (max.getText().replaceAll("\\s", "").isEmpty()) {
                            max.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 2));
                            max.setToolTipText("Please write a valid ram amount in MB\n" +
                                    "( 4 digits max )");
                        } else {
                            max.setBorder(BorderFactory.createLineBorder(launcher_frame.getBackground(), 0));
                            max.setToolTipText("The maximum amount of ram that minecraft can use");
                        }

                        Utils.l_memory.saveName(name.getText());
                        Utils.l_memory.saveMinMemory(Integer.parseInt(min.getText()));
                        Utils.l_memory.saveMaxMemory(Integer.parseInt(max.getText()));

                        launch.setEnabled(allValid);
                        launch_vanilla.setEnabled(allValid);
                    });

                    launch.setEnabled(getSelectedModpack() != null);
                    launch_vanilla.setEnabled(getSelectedModpack() != null);
                }
            }, 0, 1);

            name.setToolTipText("This will be used by the tool when launching minecraft\n" +
                    "to set your client name...");
            min.setToolTipText("The minimum amount of ram that minecraft must use");
            max.setToolTipText("The maximum amount of ram that minecraft can use");
            launch.setToolTipText("Launch the selected modpack using its fabric/forge version");
            launch_vanilla.setToolTipText("Launch the selected modpack as vanilla\n" +
                    "( no mod loaders )");

            launcher_frame.pack();
            launcher_frame.setVisible(true);

            Utils.toCenter(launcher_frame);

            initialized = true;
        } else {
            launcher_frame.setVisible(true);
            launcher_frame.toFront();
        }

        MPUExt last_modpack = Utils.l_memory.loadModpack();
        if (last_modpack != null) {
            try {
                modpacks.setSelectedItem(last_modpack.getName());
            } catch (Throwable ex) {
                //The modpack doesn't exist anymore, so it won't be found resulting in a null exception...
            }
        }

        name.setText(Utils.l_memory.getName());
        StringBuilder min_val = new StringBuilder(String.valueOf(Utils.l_memory.getMinMemory()));
        StringBuilder max_val = new StringBuilder(String.valueOf(Utils.l_memory.getMaxMemory()));
        if (min_val.length() < 4) {
            while (min_val.length() < 4)
                min_val.append("0");
        }
        if (max_val.length() < 4) {
            while (max_val.length() < 4)
                max_val.append("0");
        }
        min.setText(min_val.toString());
        max.setText(max_val.toString());
    }

    /**
     * Get the current selected modpack
     *
     * @return the current selected modpack
     */
    @Nullable
    private MPUExt getSelectedModpack() {
        try {
            String selected = modpacks.getItemAt(modpacks.getSelectedIndex());
            return name_modpack.getOrDefault(selected, null);
        } catch (Throwable ex) {
            return null;
        }
    }
}
