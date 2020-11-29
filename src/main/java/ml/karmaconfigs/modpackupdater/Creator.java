package ml.karmaconfigs.modpackupdater;

import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.utils.Color;
import ml.karmaconfigs.modpackupdater.utils.Debug;
import ml.karmaconfigs.modpackupdater.utils.Text;
import ml.karmaconfigs.modpackupdater.utils.Utils;
import ml.karmaconfigs.modpackupdater.utils.creator.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Creator implements Utils {

    private static boolean initialized = false;

    private final static JFrame main_frame = new JFrame();

    private final static Dimension size = new Dimension(700, 400);

    private final static JLabel url_text = new JLabel("Host url");
    private final static JLabel aut_text = new JLabel("Authors ( Separated by comma )");
    private final static JLabel ver_text = new JLabel("Version ( Example: 1.0.0 )");
    private final static JLabel name_text = new JLabel("Modpack name");
    private final static JLabel desc_text = new JLabel("Modpack description ( markdown format allowed )");

    private final static JTextArea url_input = new JTextArea();
    private final static JTextArea authors_input = new JTextArea();
    private final static JTextArea version_input = new JTextArea();
    private final static JTextArea name_input = new JTextArea();
    private final static JTextArea desc_input = new JTextArea();

    private final static JButton options = new JButton("Options");
    private final static JButton create = new JButton("Create");

    private final static JScrollPane desc_scrollable = new JScrollPane(desc_input);

    private final static JSplitPane url = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, url_text, url_input);
    private final static JSplitPane aut = new JSplitPane(JSplitPane.VERTICAL_SPLIT, aut_text, authors_input);
    private final static JSplitPane ver = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ver_text, version_input);
    private final static JSplitPane name = new JSplitPane(JSplitPane.VERTICAL_SPLIT, name_text, name_input);
    private final static JSplitPane desc = new JSplitPane(JSplitPane.VERTICAL_SPLIT, desc_text, desc_scrollable);

    private final static JSplitPane aut_ver = new JSplitPane(JSplitPane.VERTICAL_SPLIT, aut, ver);
    private final static JSplitPane av_desc = new JSplitPane(JSplitPane.VERTICAL_SPLIT, aut_ver, desc);
    private final static JSplitPane avd_name = new JSplitPane(JSplitPane.VERTICAL_SPLIT, name, av_desc);
    private final static JSplitPane opt_cre = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, options, create);
    private final static JSplitPane avd_create = new JSplitPane(JSplitPane.VERTICAL_SPLIT, opt_cre, avd_name);
    private final static JSplitPane url_tool = new JSplitPane(JSplitPane.VERTICAL_SPLIT, url, avd_create);

    private final static Options options_pane = new Options();

    /**
     * Initialize the frame to show it
     * to the user
     */
    public final void initialize() {
        if (!initialized) {
            //This won't work when testing, but it should show the version
            //on built jar instances
            final String app_ver = Updater.class.getPackage().getImplementationVersion();

            //Some frame utils
            main_frame.setIconImage(app_ico);
            main_frame.setTitle("ModpackCreator [ %s ]".replace("%s", app_ver != null ? app_ver : "?"));

            //Set the main frame size
            main_frame.setMinimumSize(size);
            main_frame.setMaximumSize(size);
            main_frame.setPreferredSize(size);
            main_frame.setSize(size);
            main_frame.setResizable(false);

            //What will do the main frame on close
            main_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            //Just show the frame
            main_frame.pack();
            main_frame.setVisible(true);

            //Center the frame
            Utils.toCenter(main_frame);

            //Setting up the app GUI
            {
                //Splitters
                url.setEnabled(false);
                aut.setEnabled(false);
                ver.setEnabled(false);
                name.setEnabled(false);
                desc.setEnabled(false);
                aut_ver.setEnabled(false);
                av_desc.setEnabled(false);
                avd_name.setEnabled(false);
                opt_cre.setEnabled(false);
                avd_create.setEnabled(false);
                url_tool.setEnabled(false);

                opt_cre.setDividerLocation(main_frame.getSize().width / 2);


                //Text areas
                url_input.setToolTipText("Put here the url of your host" +
                        "\nDO NOT ADD THE <name>.mpu or any" +
                        "\nextension since it will cause the modpack" +
                        "\nto create it incorrectly and being unable to" +
                        "\nbe used remotely");

                authors_input.setToolTipText("Put here the authors of the modpack." +
                        "\nYou should put here your nick and maybe" +
                        "\nor the nicks of the modpack creators network" +
                        "\nif it's the case");

                version_input.setToolTipText("Please, use the right format since this" +
                        "\nis used by the tool to check for modpack updates." +
                        "\nA correct format example would be: SNAPSHOT-1.0.0" +
                        "\nor 1.0.0-SNAPSHOT, always having at least 3 numbers");

                name_input.setToolTipText("Put here the modpack name" +
                        "\nspaces are allowed");

                desc_input.setToolTipText("Fill up this box with the modpack info" +
                        "\nsuch the amount of mods, libraries and maybe" +
                        "\na list of recommended PC specs");

                Font font = Font.getFont("Consolas");
                desc_input.setFont(font);

                //Memory manager...
                boolean authors_valid = true;
                boolean version_valid = true;

                if (authors_input.getText().replaceAll("\\s", "").contains(",")) {
                    for (String str : authors_input.getText().replaceAll("\\s", "").split(",")) {
                        if (str == null) {
                            authors_valid = false;
                            break;
                        }
                    }
                }

                try {
                    Integer.parseInt(version_input.getText().replaceAll("[aA-zZ]", "").replaceAll("\\s", "").replace(".", ""));
                } catch (Throwable ex) {
                    version_valid = false;
                }

                if (url_input.getText().replaceAll("\\s", "").isEmpty() || !UrlValidator.getInstance().isValid(url_input.getText()))
                    url_input.setText(cr_memory.getUrl());

                if (name_input.getText().replaceAll("\\s", "").isEmpty())
                    name_input.setText(cr_memory.getName());

                if (authors_input.getText().replaceAll("\\s", "").isEmpty() || !authors_valid)
                    authors_input.setText(cr_memory.getAuthors().toString()
                            .replace("[", "")
                            .replaceFirst("]", "")
                            .replace(",", ", "));

                if (version_input.getText().replaceAll("\\s", "").isEmpty() || !version_valid)
                    version_input.setText(cr_memory.getVersion());

                if (desc_input.getText().replaceAll("\\s", "").isEmpty())
                    fillDesc(cr_memory.getDescription());

                create.setEnabled(checkInfo());

                //Buttons
                options.setToolTipText("Manage modpack options like" +
                        "\nincluding shaders, textures and other misc...");
                create.setToolTipText("Click here to create the modpack" +
                        "\n" +
                        "\nMAKE SURE EVERYTHING IS CORRECTLY DEFINED BEFORE" +
                        "\nTAKING THIS STEP, SINCE YOU WILL HAVE TO RE-WRITE" +
                        "\nALMOST EVERYTHING AFTER CLICKING HERE");
            }

            main_frame.add(url_tool);

            StringBuilder authors = new StringBuilder();
            for (String author : cr_memory.getAuthors())
                authors.append(author).append(", ");

            url_input.setText(cr_memory.getUrl());
            name_input.setText(cr_memory.getName());
            authors_input.setText(replaceLast(authors.toString(), ", "));
            version_input.setText(cr_memory.getVersion());
            fillDesc(cr_memory.getDescription());

            create.setEnabled(checkInfo());

            name_input.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    create.setEnabled(checkInfo());
                }
            });

            authors_input.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    create.setEnabled(checkInfo());
                }
            });

            version_input.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    create.setEnabled(checkInfo());
                }
            });

            desc_input.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    create.setEnabled(checkInfo());
                }
            });

            options.addActionListener(e -> {
                if (options.getText().equals("Hide options")) {
                    Options.manager.hide();
                    options.setText("Options");
                } else {
                    Options.manager.show();
                    options.setText("Hide options");
                }
            });

            create.addActionListener(e -> {
                String version_to_use = Options.manager.getVersion();
                if (version_to_use != null && !version_to_use.replaceAll("\\s", "").isEmpty()) {
                    if (checkInfo()) {
                        try {
                            File upload_dir = new File(Utils.getPackDir(name_input.getText()), "upload");
                            if (upload_dir.exists() && upload_dir.delete())
                                Debug.util.add(Text.util.create("Deleted old upload folder " + Utils.findPath(upload_dir), Color.LIGHTGREEN, 12), true);

                            String version = Options.listing.getRealVersion(Options.manager.getVersion());

                            ModDuplicator mod_dupe = new ModDuplicator(version);
                            mod_dupe.copyTo(name_input.getText());
                            VersionDuplicator ver_dupe = new VersionDuplicator(version_to_use);
                            ver_dupe.copyTo(name_input.getText());

                            if (Options.manager.includeSaves()) {
                                WorldDuplicator wor_dupe = new WorldDuplicator();
                                wor_dupe.copyTo(name_input.getText());
                            }
                            if (Options.manager.includeTextures()) {
                                TextureDuplicator tex_dupe = new TextureDuplicator();
                                tex_dupe.copyTo(name_input.getText());
                            }
                            if (Options.manager.includeShaders()) {
                                ShaderDuplicator sha_dupe = new ShaderDuplicator();
                                sha_dupe.copyTo(name_input.getText());
                            }

                            File modpack_file = new File(Utils.getPackDir(name_input.getText()), name_input.getText() + ".mpu");
                            MPUExt mod = new MPUExt(modpack_file);

                            mod.setName(name_input.getText());
                            mod.setAuthors(authors_input.getText().replaceAll("\\s", "").split(","));
                            mod.setVersion(version_input.getText());

                            List<String> lines = new ArrayList<>();
                            desc_input.setText(desc_input.getText().replace("<br>", "\n"));
                            boolean line_skip = false;
                            for (String str : desc_input.getText().split("\n")) {
                                if (!str.replaceAll("\\s", "").isEmpty()) {
                                    line_skip = false;
                                    List<Extension> ext = new ArrayList<>();
                                    ext.add(TablesExtension.create());

                                    Parser parser = Parser.builder().extensions(ext).build();
                                    Node root = parser.parse(str);

                                    HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(ext).build();
                                    String html_format = htmlRenderer.render(root);
                                    lines.add(html_format.replace("\n", "").replace("<p>", "<span>").replace("</p>", "</span>"));
                                } else {
                                    if (!line_skip) {
                                        lines.add("<br>");
                                        line_skip = true;
                                    }
                                }
                            }

                            mod.setDescription(lines);
                            mod.setMainURL(url_input.getText());

                            cr_memory.saveUrl(url_input.getText());
                            cr_memory.saveName(name_input.getText());
                            cr_memory.saveVersion(version_input.getText());
                            cr_memory.saveAuthors(authors_input.getText().replaceAll("\\s", "").split(","));
                            cr_memory.saveDescription(getDescription());
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }
                } else {
                    Debug.util.add(Text.util.create("Please select a valid forge - fabric version from options pane", Color.INDIANRED, 12), false);
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

            options_pane.initialize();
            options.setText("Hide options");
            main_frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    Options.manager.setLocation(main_frame.getX() + main_frame.getWidth(), main_frame.getY());
                }
            });

            main_frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    Options.manager.hide();
                }

                @Override
                public void windowIconified(WindowEvent e) {
                    Options.manager.hide();
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                    if (options.getText().equals("Hide options")) {
                        Options.manager.show();
                    } else {
                        Options.manager.hide();
                    }
                }
            });

            main_frame.setAlwaysOnTop(true);

            initialized = true;
        } else {
            if (options.getText().equals("Hide options")) {
                Options.manager.show();
            } else {
                Options.manager.hide();
            }

            main_frame.setVisible(true);
            main_frame.toFront();

            //I want to do it everytime it gets
            //opened
            boolean authors_valid = true;
            boolean version_valid = true;

            if (authors_input.getText().replaceAll("\\s", "").contains(",")) {
                for (String str : authors_input.getText().replaceAll("\\s", "").split(",")) {
                    if (str == null) {
                        authors_valid = false;
                        break;
                    }
                }
            }

            try {
                Integer.parseInt(version_input.getText().replaceAll("[aA-zZ]", "").replaceAll("\\s", "").replace(".", ""));
            } catch (Throwable ex) {
                version_valid = false;
            }

            if (url_input.getText().replaceAll("\\s", "").isEmpty() || !UrlValidator.getInstance().isValid(url_input.getText())) {
                url_input.setText(cr_memory.getUrl());
                url_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            }

            if (name_input.getText().replaceAll("\\s", "").isEmpty()) {
                name_input.setText(cr_memory.getName());
                name_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            }

            if (authors_input.getText().replaceAll("\\s", "").isEmpty() || !authors_valid) {
                authors_input.setText(cr_memory.getAuthors().toString()
                        .replace("[", "")
                        .replaceFirst("]", "")
                        .replace(",", ", "));
                authors_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            }

            if (version_input.getText().replaceAll("\\s", "").isEmpty() || !version_valid) {
                version_input.setText(cr_memory.getVersion());
                version_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            }

            if (desc_input.getText().replaceAll("\\s", "").isEmpty()) {
                fillDesc(cr_memory.getDescription());
                desc_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            }

            create.setEnabled(checkInfo());
        }
    }

    /**
     * Fill the description with the
     * specified information in the
     * arraylist
     *
     * @param lines the arraylist
     */
    private void fillDesc(final List<String> lines) {
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                builder.append(line).append("\n");
            }

            desc_input.setText(replaceLast(builder.toString(), "\n"));
        } catch (Throwable ignored) {
        }
    }

    /**
     * Check the info boxes of the tool
     */
    private boolean checkInfo() {
        return checkName() &&
                checkAuthors() &&
                checkVersion() &&
                checkDescription();
    }

    /**
     * Check the modpack name
     * info
     */
    private boolean checkName() {
        boolean name_valid = !name_input.getText().replaceAll("\\s", "").isEmpty();

        if (!name_valid) {
            name_input.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 2));

            name_input.setToolTipText("Please put a valid modpack name");

            return false;
        } else {
            name_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            name_input.setToolTipText("Put here the modpack name" +
                    "\nspaces are allowed");

            return true;
        }
    }

    /**
     * Check modpack authors
     * info
     */
    private boolean checkAuthors() {
        boolean authors_valid = true;

        if (authors_input.getText().replaceAll("\\s", "").contains(",")) {
            for (String str : authors_input.getText().replaceAll("\\s", "").split(",")) {
                if (str == null) {
                    authors_valid = false;
                    break;
                }
            }
        }

        boolean auth_valid = !authors_input.getText().replaceAll("\\s", "").isEmpty() && authors_valid;

        if (!auth_valid) {
            authors_input.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 2));

            if (authors_input.getText().replaceAll("\\s", "").isEmpty()) {
                authors_input.setToolTipText("Please provide a valid" +
                        "\nauthor or a list of authors" +
                        "\nseparated by comma");
            } else {
                authors_input.setToolTipText("Make sure authors syntax is correct");
            }

            return false;
        } else {
            authors_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            authors_input.setToolTipText("Put here the authors of the modpack." +
                    "\nYou should put here your nick and maybe" +
                    "\nor the nicks of the modpack creators network" +
                    "\nif it's the case");

            return true;
        }
    }

    /**
     * Check modpack version
     * info
     */
    private boolean checkVersion() {
        boolean version_valid = true;

        try {
            Integer.parseInt(version_input.getText().replaceAll("[aA-zZ]", "").replaceAll("\\s", "").replace(".", ""));
        } catch (Throwable ex) {
            version_valid = false;
        }

        boolean ver_valid = !version_input.getText().replaceAll("\\s", "").isEmpty() && version_valid;

        if (!ver_valid) {
            version_input.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 2));

            if (version_input.getText().replaceAll("\\s", "").isEmpty()) {
                version_input.setToolTipText("Please provide a valid" +
                        "\nversion format, for example:" +
                        "\nSNAPSHOT-1.0.0");
            } else {
                version_input.setToolTipText("Please include a version identifier" +
                        "\nto your version input, for example" +
                        "\n1.0.0");
            }

            return false;
        } else {
            version_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            version_input.setToolTipText("Please, use the right format since this" +
                    "\nis used by the tool to check for modpack updates." +
                    "\nA correct format example would be: SNAPSHOT-1.0.0" +
                    "\nor 1.0.0-SNAPSHOT, always having at least 3 numbers");

            return true;
        }
    }

    /**
     * Check the modpack description
     * info
     */
    private boolean checkDescription() {
        boolean desc_valid = !desc_input.getText().replaceAll("\\s", "").isEmpty();

        if (!desc_valid) {
            desc_input.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 2));

            desc_input.setToolTipText("Please fill this box with" +
                    "\nmodpack info, html format is enabled");

            return false;
        } else {
            desc_input.setBorder(BorderFactory.createLineBorder(main_frame.getBackground(), 0));
            desc_input.setToolTipText("Please, use the right format since this" +
                    "\nis used by the tool to check for modpack updates." +
                    "\nA correct format example would be: SNAPSHOT-1.0.0" +
                    "\nor 1.0.0-SNAPSHOT, always having at least 3 numbers");

            return true;
        }
    }

    /**
     * Replace the last value from the specified string that
     * matches with the regex
     *
     * @param text the string
     * @param regex the regex
     * @return the replaced string
     */
    private String replaceLast(String text, final String regex) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", "");
    }

    /**
     * Get the description as a list
     * of strings
     *
     * @return the modpack description
     */
    private List<String> getDescription() {
        ArrayList<String> lines = new ArrayList<>();

        for (String str : desc_input.getText().split("\n")) {
            lines.add(str
                    .replace("[", "{open}")
                    .replace("]", "{close}")
                    .replace(",", "{comma}"));
        }

        return lines;
    }
}
