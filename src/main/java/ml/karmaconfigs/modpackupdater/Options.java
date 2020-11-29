package ml.karmaconfigs.modpackupdater;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import javafx.scene.paint.Color;
import ml.karmaconfigs.modpackupdater.utils.AsyncScheduler;
import ml.karmaconfigs.modpackupdater.utils.Cache;
import ml.karmaconfigs.modpackupdater.utils.Debug;
import ml.karmaconfigs.modpackupdater.utils.Text;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public final class Options {

    private static boolean initialized = false;

    private final static JFrame main_frame = new JFrame("Creator options");

    private final static Dimension size = new Dimension(250, 393);

    private final static JComboBox<String> versions = new JComboBox<>();

    private final static JPanel selector = new JPanel();

    private final static JScrollPane selector_scroll = new JScrollPane(selector);

    private final static JCheckBox include_worlds = new JCheckBox("Include worlds");
    private final static JCheckBox include_textures = new JCheckBox("Include textures");
    private final static JCheckBox include_shaders = new JCheckBox("Include shaders");

    private final static HashSet<String> selected_worlds = new HashSet<>();
    private final static HashSet<String> selected_textures = new HashSet<>();
    private final static HashSet<String> selected_shaders = new HashSet<>();

    /**
     * Initialize the frame to show it
     * to the user
     */
    public final void initialize() {
        if (!initialized) {
            //Main frame icon, loaded from cache
            Cache cache = new Cache();
            main_frame.setIconImage(cache.getIco());

            //Set the main frame size
            main_frame.setMinimumSize(size);
            main_frame.setMaximumSize(size);
            main_frame.setPreferredSize(size);
            main_frame.setSize(size);
            main_frame.setResizable(false);

            //What will do the main frame on close
            main_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            //Un-decorate the frame (remove borders)
            main_frame.setUndecorated(true);

            //Split panes
            JSplitPane t_shaders = new JSplitPane(JSplitPane.VERTICAL_SPLIT, include_textures, include_shaders);
            JSplitPane world_pan = new JSplitPane(JSplitPane.VERTICAL_SPLIT, include_worlds, selector_scroll);
            JSplitPane ts_world = new JSplitPane(JSplitPane.VERTICAL_SPLIT, t_shaders, world_pan);
            JSplitPane ver_tswol = new JSplitPane(JSplitPane.VERTICAL_SPLIT, versions, ts_world);

            //Component options
            {
                t_shaders.setEnabled(false);
                world_pan.setEnabled(false);
                ts_world.setEnabled(false);
                ver_tswol.setEnabled(false);

                selector.setLayout(new BoxLayout(selector, BoxLayout.Y_AXIS));
                selector_scroll.getVerticalScrollBar().setUnitIncrement(15);
                selector_scroll.getHorizontalScrollBar().setUnitIncrement(15);
            }

            //Add all the buttons
            main_frame.add(ver_tswol);

            //Show the frame
            main_frame.pack();
            main_frame.setVisible(true);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new AsyncScheduler(() -> {
                        int value = selector_scroll.getVerticalScrollBar().getValue();
                        try {
                            String selected = versions.getItemAt(versions.getSelectedIndex());

                            versions.removeAllItems();
                            selector.removeAll();
                            try {
                                for (String str : listing.versions()) {
                                    versions.addItem(str);
                                }
                            } catch (Throwable ex) {
                                Text text = new Text(ex);
                                text.format(ml.karmaconfigs.modpackupdater.utils.Color.INDIANRED, 14);

                                Debug.util.add(text, true);
                            }

                            JLabel w_title = new JLabel("Worlds_Title");
                            selector.add(w_title);
                            selector.add(new JLabel("\n"));
                            w_title.setText("<html>" + Text.util.create("Worlds", ml.karmaconfigs.modpackupdater.utils.Color.LIGHTCYAN, 12).getText(true) + "</html>");
                            for (String world : listing.getWorlds()) {
                                JCheckBox box = new JCheckBox("World=" + world);
                                box.setText(world);
                                box.addActionListener(e -> {
                                    if (selected_worlds.contains(world)) {
                                        selected_worlds.remove(world);
                                    } else {
                                        selected_worlds.add(world);
                                    }
                                });
                                box.setName(world);
                                box.setSelected(selected_worlds.contains(world));
                                selector.add(box);

                                SwingUtilities.invokeLater(() -> {
                                    SwingUtilities.updateComponentTreeUI(selector);
                                    for (Component component : selector.getComponents()) {
                                        if (component != null) {
                                            SwingUtilities.updateComponentTreeUI(component);
                                        }
                                    }
                                });
                            }

                            JLabel t_title = new JLabel("Textures_Title");
                            selector.add(new JLabel("<html>" + Text.util.create("______________", ml.karmaconfigs.modpackupdater.utils.Color.WHITE, 12).getText(true) + "</html>"));
                            selector.add(t_title);
                            selector.add(new JLabel("\n"));
                            t_title.setText("<html>" + Text.util.create("Texture Packs", ml.karmaconfigs.modpackupdater.utils.Color.LIGHTCYAN, 12).getText(true) + "</html>");
                            for (String texture : listing.getTextures()) {
                                JCheckBox box = new JCheckBox("Texture=" + texture);
                                box.setText(texture);
                                box.addActionListener(e -> {
                                    if (selected_textures.contains(texture)) {
                                        selected_textures.remove(texture);
                                    } else {
                                        selected_textures.add(texture);
                                    }
                                });
                                box.setName(texture);
                                box.setSelected(selected_textures.contains(texture));
                                selector.add(box);

                                SwingUtilities.invokeLater(() -> {
                                    SwingUtilities.updateComponentTreeUI(selector);
                                    for (Component component : selector.getComponents()) {
                                        if (component != null) {
                                            SwingUtilities.updateComponentTreeUI(component);
                                        }
                                    }
                                });
                            }

                            JLabel s_title = new JLabel("Textures_Title");
                            selector.add(new JLabel("<html>" + Text.util.create("______________", ml.karmaconfigs.modpackupdater.utils.Color.WHITE, 12).getText(true) + "</html>"));
                            selector.add(s_title);
                            selector.add(new JLabel("\n"));
                            s_title.setText("<html>" + Text.util.create("Shader Packs", ml.karmaconfigs.modpackupdater.utils.Color.LIGHTCYAN, 12).getText(true) + "</html>");
                            for (String shader : listing.getShaders()) {
                                JCheckBox box = new JCheckBox("Shader=" + shader);
                                box.setText(shader);
                                box.addActionListener(e -> {
                                    if (selected_shaders.contains(shader)) {
                                        selected_shaders.remove(shader);
                                    } else {
                                        selected_shaders.add(shader);
                                    }
                                });
                                box.setName(shader);
                                box.setSelected(selected_shaders.contains(shader));
                                selector.add(box);

                                SwingUtilities.invokeLater(() -> {
                                    SwingUtilities.updateComponentTreeUI(selector);
                                    for (Component component : selector.getComponents()) {
                                        if (component != null) {
                                            SwingUtilities.updateComponentTreeUI(component);
                                        }
                                    }
                                });
                            }

                            SwingUtilities.invokeLater(() -> selector_scroll.getVerticalScrollBar().setValue(value));

                            try {
                                versions.setSelectedItem(selected);
                            } catch (Throwable ignored) {
                            }
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(ml.karmaconfigs.modpackupdater.utils.Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }).run();
                }
            }, 0, TimeUnit.SECONDS.toMillis(5));

            main_frame.setAlwaysOnTop(true);

            initialized = true;
        } else {
            main_frame.setVisible(true);
        }
    }

    public interface manager {

        static void setLocation(int x, int y) {
            if (x == -1)
                x = main_frame.getX();

            if (y == -1)
                y = main_frame.getY();

            main_frame.setLocation(x, y);
        }

        static void show() {
            main_frame.setVisible(true);
        }

        static void hide() {
            main_frame.setVisible(false);
        }

        static String getVersion() {
            return versions.getItemAt(versions.getSelectedIndex());
        }

        static boolean includeSaves() {
            return include_worlds.isSelected();
        }

        static boolean includeTextures() {
            return include_textures.isSelected();
        }

        static boolean includeShaders() {
            return include_shaders.isSelected();
        }

        static boolean isWorldIncluded(final String name) {
            return selected_worlds.contains(name);
        }

        static boolean isTextureIncluded(final String name) {
            return selected_textures.contains(name.substring(0, name.length() - 4));
        }

        static boolean isShaderIncluded(final String name) {
            return selected_shaders.contains(name.substring(0, name.length() - 4));
        }
    }

    interface listing {

        static ArrayList<String> versions() throws Throwable {
            Cache cache = new Cache();

            File vFolder = new File(cache.getMcFolder() + "/versions");
            File[] versions = vFolder.listFiles();
            ArrayList<String> names = new ArrayList<>();
            if (versions != null && !Arrays.asList(versions).isEmpty()) {
                for (File version : versions) {
                    String name = version.getName();
                    if (name.contains("forge") || name.contains("Forge") || name.contains("liteloader") || name.contains("LiteLoader")) {
                        File json = new File(version, name + ".json");
                        if (json.exists()) {
                            FileReader reader = new FileReader(json);
                            Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                            JsonReader json_reader = new JsonReader(reader);
                            json_reader.setLenient(true);

                            JsonObject info = gson.fromJson(json_reader, JsonObject.class);

                            if (info.has("id")) {
                                if (info.get("id").toString().contains("forge") || info.get("id").toString().contains("LiteLoader")) {
                                    names.add(name);
                                }
                            }
                        }
                    }
                }
            }

            return names;
        }

        static String getRealVersion(String version) throws Throwable {
            Cache cache = new Cache();

            File json = new File(cache.getMcFolder() + "/versions/" + version + "/" + version + ".json");
            if (json.exists()) {
                FileReader reader = new FileReader(json);
                Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                JsonReader json_reader = new JsonReader(reader);
                json_reader.setLenient(true);

                JsonObject info = gson.fromJson(json_reader, JsonObject.class);

                if (info.has("inheritsFrom")) {
                    return info.get("inheritsFrom").toString();
                }
            }
            return "";
        }

        static ArrayList<String> getWorlds() {
            Cache cache = new Cache();

            File wFolder = new File(cache.getMcFolder() + "/saves");
            File[] worlds = wFolder.listFiles();
            ArrayList<String> names = new ArrayList<>();
            if (worlds != null && !Arrays.asList(worlds).isEmpty()) {
                for (File world : worlds) {
                    if (world.isDirectory()) {
                        names.add(world.getName());
                    }
                }
            }

            return names;
        }

        static ArrayList<String> getTextures() {
            Cache cache = new Cache();

            File tFolder = new File(cache.getMcFolder() + "/resourcepacks");
            File[] textures = tFolder.listFiles();
            ArrayList<String> names = new ArrayList<>();
            if (textures != null && !Arrays.asList(textures).isEmpty()) {
                for (File texture : textures) {
                    if (texture.getName().endsWith(".zip") || texture.getName().endsWith(".rar")) {
                        names.add(texture.getName().substring(0, texture.getName().length() - 4));
                    }
                }
            }

            return names;
        }

        static ArrayList<String> getShaders() {
            Cache cache = new Cache();

            File sFolder = new File(cache.getMcFolder() + "/shaderpacks");
            File[] shaders = sFolder.listFiles();
            ArrayList<String> names = new ArrayList<>();
            if (shaders != null && !Arrays.asList(shaders).isEmpty()) {
                for (File shader : shaders) {
                    if (shader.getName().endsWith(".zip") || shader.getName().endsWith(".rar")) {
                        names.add(shader.getName().substring(0, shader.getName().length() - 4));
                    }
                }
            }

            return names;
        }
    }
}