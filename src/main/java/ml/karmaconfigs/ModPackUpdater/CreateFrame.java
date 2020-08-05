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

public final class CreateFrame {

    private final Utils utils = new Utils();

    public static JFrame creatorFrame;

    private static final JFileChooser chooser = new JFileChooser();
    private static final JPanel emptyPanel = new JPanel();

    private static JTextArea url;
    private static JTextArea name;

    private static JButton createModPack;
    private static JButton openDownloadDir;

    private static JCheckBox createAsZip;
    private static JCheckBox includeShaders;
    private static JCheckBox includeTextures;

    private static JSplitPane optionPanel;

    private static JSplitPane buildOptions;

    private File mcFolder = FilesUtilities.getConfig.getMinecraftDir();

    public CreateFrame() {
        if (creatorFrame == null) {
            creatorFrame = new JFrame();

            creatorFrame.setPreferredSize(new Dimension(750, 550));

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
                createAsZip.setSelected(true);
            }
            if (includeShaders == null) {
                includeShaders = new JCheckBox("Zip shaders");
            }
            if (includeTextures == null) {
                includeTextures = new JCheckBox("Zip texture packs");
            }

            JSplitPane optionsSplitOne = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createModPack, openDownloadDir);
            JSplitPane optionsSplitTwo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createAsZip, includeShaders);
            JSplitPane optionsSplitThree = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, includeTextures, new JPanel());
            if (optionPanel == null) {
                optionPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, optionsSplitTwo, optionsSplitThree);
            }

            if (buildOptions == null) {
                buildOptions = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, optionsSplitOne, optionPanel);
            }
            JSplitPane chooserTitle = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JLabel("<html><h1>Select your minecraft folder where /mods are located</h1></html>"), chooser);
            JSplitPane buildPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildOptions, chooserTitle);
            JSplitPane urlSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, name, url);
            JSplitPane urlField = new JSplitPane(JSplitPane.VERTICAL_SPLIT, urlSplitter, buildPane);

            JPanel panel = new JPanel();
            panel.add(urlField);

            disableResize(buildOptions, optionsSplitOne, optionsSplitTwo, optionsSplitThree, urlSplitter, optionPanel, chooserTitle, buildPane, urlField);

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
        creatorFrame.setVisible(true);

        createAsZip.addActionListener(e -> {
            if (createAsZip.isSelected()) {
                buildOptions.remove(emptyPanel);
                buildOptions.add(optionPanel);
            } else {
                includeShaders.setSelected(false);
                includeTextures.setSelected(false);
                buildOptions.remove(optionPanel);
                buildOptions.add(emptyPanel);
            }
        });

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

            try {
                utils.setupCreator(urlDir, name.getText(), createAsZip.isSelected(), includeShaders.isSelected(), includeTextures.isSelected());
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
    }

    private void disableResize(JSplitPane... panes) {
        for (JSplitPane pane : panes) {
            pane.setEnabled(false);
        }
    }
}
