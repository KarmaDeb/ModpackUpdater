package ml.karmaconfigs.ModPackUpdater;

import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public final class CreateFrame {

    private final Utils utils = new Utils();

    public static JFrame creatorFrame;

    public static final JFileChooser chooser = new JFileChooser();

    private static JTextArea url;
    private static JTextArea name;

    private static JButton createModPack;
    private static JButton openDownloadDir;

    private static JCheckBox createAsZip;
    private static JCheckBox includeShaders;
    private static JCheckBox includeTextures;
    private static JCheckBox unzipDebug;

    private static JComboBox<String> version;

    private File mcFolder = FilesUtilities.getConfig.getMinecraftDir();

    public CreateFrame() {
        version = new JComboBox<>(ListVersions.listing.versions());
        if (creatorFrame == null) {
            creatorFrame = new JFrame();
            creatorFrame.setPreferredSize(new Dimension(900, 600));

            try {
                creatorFrame.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
            } catch (Throwable e) {
                e.printStackTrace();
            }

            chooser.setCurrentDirectory(mcFolder);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setToolTipText("");
            chooser.setApproveButtonToolTipText("");

            if (url == null) {
                url = new JTextArea(FilesUtilities.getConfig.getCreatorURL());
            }
            if (name == null) {
                name = new JTextArea(FilesUtilities.getConfig.getCreatorName());
            }
            if (createModPack == null) {
                createModPack = new JButton("Create modpack");
            }
            if (openDownloadDir == null) {
                openDownloadDir = new JButton("Open modpack files dir");
            }
            if (createAsZip == null) {
                createAsZip = new JCheckBox("Zip modpack");
                createAsZip.setSelected(FilesUtilities.getConfig.createAsZip());
            }
            if (includeShaders == null) {
                includeShaders = new JCheckBox("Zip shaders");
                if (createAsZip.isSelected()) {
                    includeShaders.setSelected(FilesUtilities.getConfig.zipShaders());
                } else {
                    includeShaders.setSelected(false);
                    includeShaders.setEnabled(false);
                }
            }
            if (includeTextures == null) {
                includeTextures = new JCheckBox("Zip texture packs");
                if (createAsZip.isSelected()) {
                    includeTextures.setSelected(FilesUtilities.getConfig.zipTextures());
                } else {
                    includeTextures.setSelected(false);
                    includeTextures.setEnabled(false);
                }
            }
            if (unzipDebug == null) {
                unzipDebug = new JCheckBox("Perform unzip debug");
                if (createAsZip.isSelected()) {
                    unzipDebug.setSelected(FilesUtilities.getConfig.zipDebug());
                } else {
                    unzipDebug.setSelected(false);
                    unzipDebug.setEnabled(false);
                }
            }

            JSplitPane optionsSplitOne = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createModPack, openDownloadDir);
            JSplitPane zipOptionsOne = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, includeShaders, includeTextures);
            JSplitPane zipOptionsTwo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, zipOptionsOne, unzipDebug);
            JSplitPane zipOptionsThree = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, zipOptionsTwo, version);
            JSplitPane zipOptionsFinal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, zipOptionsThree, new JPanel());
            JSplitPane optionPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createAsZip, zipOptionsFinal);
            JSplitPane buildOptions = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, optionsSplitOne, optionPanel);
            JSplitPane chooserTitle = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JLabel("<html><h1>Select your minecraft folder where /mods are located</h1></html>"), chooser);
            JSplitPane buildPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildOptions, chooserTitle);
            JSplitPane urlSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, name, url);
            JSplitPane urlField = new JSplitPane(JSplitPane.VERTICAL_SPLIT, urlSplitter, buildPane);

            JPanel panel = new JPanel();
            panel.add(urlField);

            disableResize(buildOptions, optionsSplitOne, zipOptionsOne, zipOptionsTwo, zipOptionsThree,  zipOptionsFinal, urlSplitter, optionPanel, chooserTitle, buildPane, urlField);

            creatorFrame.setTitle("Create a new modpack");
            creatorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            creatorFrame.pack();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            creatorFrame.setLocation(dim.width / 2 - creatorFrame.getSize().width / 2, dim.height / 2 - creatorFrame.getSize().height / 2);
            creatorFrame.setResizable(false);
            creatorFrame.add(urlField);
        }
    }

    public void display() {
        if (version.getItemCount() > 0) {
            creatorFrame.setVisible(true);

            createAsZip.addActionListener(e -> {
                includeTextures.setEnabled(createAsZip.isSelected());
                includeShaders.setEnabled(createAsZip.isSelected());
                unzipDebug.setEnabled(createAsZip.isSelected());

                if (!createAsZip.isSelected()) {
                    FilesUtilities.getConfig.saveCreatorOptions(createAsZip.isSelected(), includeTextures.isSelected(), includeShaders.isSelected(), unzipDebug.isSelected());
                    includeTextures.setSelected(false);
                    includeShaders.setSelected(false);
                    unzipDebug.setSelected(false);
                } else {
                    includeTextures.setSelected(FilesUtilities.getConfig.zipTextures());
                    includeShaders.setSelected(FilesUtilities.getConfig.zipShaders());
                    unzipDebug.setSelected(FilesUtilities.getConfig.zipDebug());
                    FilesUtilities.getConfig.saveCreatorOptions(createAsZip.isSelected(), includeTextures.isSelected(), includeShaders.isSelected(), unzipDebug.isSelected());
                }
            });

            includeTextures.addActionListener(e -> FilesUtilities.getConfig.saveCreatorOptions(createAsZip.isSelected(), includeTextures.isSelected(), includeShaders.isSelected(), unzipDebug.isSelected()));

            includeShaders.addActionListener(e -> FilesUtilities.getConfig.saveCreatorOptions(createAsZip.isSelected(), includeTextures.isSelected(), includeShaders.isSelected(), unzipDebug.isSelected()));

            unzipDebug.addActionListener(e -> FilesUtilities.getConfig.saveCreatorOptions(createAsZip.isSelected(), includeTextures.isSelected(), includeShaders.isSelected(), unzipDebug.isSelected()));

            chooser.addActionListener(e -> {
                if (e.getActionCommand().contains("Approve")) {
                    if (!chooser.getSelectedFile().equals(mcFolder)) {
                        mcFolder = chooser.getSelectedFile();
                        utils.setDebug(utils.rgbColor("Changed minecraft directory to: " + FilesUtilities.getPath(mcFolder), 120, 200, 155), true);
                        FilesUtilities.getConfig.saveMinecraftDir();
                    } else {
                        utils.setDebug(utils.rgbColor("Same minecraft directory selected, nothing changed", 110, 150, 150), false);
                    }
                } else {
                    if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
                        creatorFrame.setVisible(false);
                        SwingUtilities.updateComponentTreeUI(MainFrame.frame);
                    }
                }
            });

            createModPack.addActionListener(e -> {
                String urlDir;

                if (!url.getText().contains("http://") && !url.getText().contains("https://")) {
                    urlDir = "http://" + url.getText();
                } else {
                    urlDir = url.getText();
                }

                if (urlDir.endsWith("/")) {
                    urlDir = urlDir.substring(0, urlDir.length() - 1);
                }

                Object loaderVersion = version.getSelectedItem();

                try {
                    assert loaderVersion != null;
                    utils.setupCreator(urlDir, name.getText(), loaderVersion.toString(), createAsZip.isSelected(), includeShaders.isSelected(), includeTextures.isSelected(), unzipDebug.isSelected());
                    Thread creator = new Thread(utils, "Creating");
                    creator.start();
                } catch (Throwable ex) {
                    utils.log(ex);
                    utils.setDebug(utils.rgbColor("Error while creating modpack", 220, 100, 100), true);
                }
            });

            openDownloadDir.addActionListener(e -> {
                Modpack modpack = new Modpack(name.getText());

                if (modpack.exists()) {
                    try {
                        Desktop.getDesktop().open(FilesUtilities.getModpackUploadDir(modpack));
                    } catch (Throwable ex) {
                        utils.log(ex);
                    }
                } else {
                    utils.setDebug(utils.rgbColor("Create the modpack before opening modpack uploads folder!", 220, 100, 100), true);
                }
            });

            url.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    FilesUtilities.getConfig.saveCreatorURL(url.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    FilesUtilities.getConfig.saveCreatorURL(url.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    FilesUtilities.getConfig.saveCreatorURL(url.getText());
                }
            });

            name.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    FilesUtilities.getConfig.saveCreatorName(name.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    FilesUtilities.getConfig.saveCreatorName(name.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    FilesUtilities.getConfig.saveCreatorName(name.getText());
                }
            });
        } else {
            JFrame errorFrame = new JFrame();
            errorFrame.setPreferredSize(new Dimension(500, 100));

            try {
                errorFrame.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
            } catch (Throwable e) {
                e.printStackTrace();
            }

            JPanel infoPanel = new JPanel();
            JLabel error = new JLabel();
            error.setText("<html>No forge/fabric version detected,<br>please install a forge or fabric<br>version before using the modpack creator</html>");

            infoPanel.add(error);

            errorFrame.setTitle("Something went wrong...");
            errorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            errorFrame.pack();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            errorFrame.setLocation(dim.width / 2 - errorFrame.getSize().width / 2, dim.height / 2 - errorFrame.getSize().height / 2);
            errorFrame.setResizable(false);
            errorFrame.add(infoPanel);
            errorFrame.setVisible(true);
        }
    }

    private void disableResize(JSplitPane... panes) {
        for (JSplitPane pane : panes) {
            pane.setEnabled(false);
        }
    }
}

class ListVersions {


    public interface listing {
        static String[] versions() {
            File vFolder = new File(FilesUtilities.getMinecraftDir() + "/versions");
            File[] versions = vFolder.listFiles();
            ArrayList<String> names = new ArrayList<>();
            if (versions != null && !Arrays.asList(versions).isEmpty()) {
                for (File version : versions) {
                    String name = version.getName();
                    if (name.contains("forge") || name.contains("fabric")) {
                        names.add(name);
                    }
                }
            }

            String[] versionNames = new String[names.size()];
            for (int i = 0; i < versionNames.length; i++) {
                versionNames[i] = names.get(i);
            }

            return versionNames;
        }
    }
}