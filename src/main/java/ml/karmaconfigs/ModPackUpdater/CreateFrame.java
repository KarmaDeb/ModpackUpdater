package ml.karmaconfigs.ModPackUpdater;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.Config;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CreateFrame {

    private final static Utils utils = new Utils();

    private final static List<String> versions = new ArrayList<>();

    public static JFrame creatorFrame;
    public static JFrame errorFrame;

    public static final JFileChooser chooser = new JFileChooser();

    private static JTextArea url;
    private static JTextArea name;

    private static JButton createModPack;
    private static JButton openDownloadDir;

    private static JCheckBox createAsZip;
    private static JCheckBox includeShaders;
    private static JCheckBox includeTextures;
    private static JCheckBox unzipDebug;

    private static final JLabel chooserTitle = new JLabel();

    public static final JComboBox<String> version = new JComboBox<>();
    private static String selected = "";

    private File mcFolder = FilesUtilities.getConfig.getMinecraftDir();

    public CreateFrame() {
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
        JSplitPane chooserSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chooserTitle, chooser);
        JSplitPane buildPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildOptions, chooserSplitter);
        JSplitPane urlSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, name, url);
        JSplitPane urlField = new JSplitPane(JSplitPane.VERTICAL_SPLIT, urlSplitter, buildPane);

        JPanel panel = new JPanel();
        panel.add(urlField);

        disableResize(buildOptions, optionsSplitOne, zipOptionsOne, zipOptionsTwo, zipOptionsThree, zipOptionsFinal, urlSplitter, optionPanel, chooserSplitter, buildPane, urlField);

        creatorFrame.setTitle("Create a new modpack");
        creatorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        creatorFrame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        creatorFrame.setLocation(dim.width / 2 - creatorFrame.getSize().width / 2, dim.height / 2 - creatorFrame.getSize().height / 2);
        creatorFrame.setResizable(false);
        creatorFrame.add(urlField);
    }

    public void display() {
        chooser.setSelectedFile(mcFolder);
        chooser.setCurrentDirectory(mcFolder);
        versioning.checkVersions();

        utils.reloadTool();
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
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                boolean changed = false;
                if (!chooser.getSelectedFile().equals(mcFolder)) {
                    changed = true;
                    mcFolder = chooser.getSelectedFile();
                    utils.setDebug(utils.rgbColor("Changed minecraft directory to: " + FilesUtilities.getPath(mcFolder), 120, 200, 155), true);
                    FilesUtilities.getConfig.saveMinecraftDir(mcFolder);
                }

                versioning.checkVersions();
                utils.setDebug(utils.rgbColor("Re-checked for versions in the folder: " + FilesUtilities.getPath(chooser.getSelectedFile()), 120, 200, 155), !changed);

                chooser.setCurrentDirectory(mcFolder);

                utils.reloadTool();
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
                utils.setupCreator(urlDir, name.getText(), loaderVersion.toString(), ListVersions.listing.getRealVersion(loaderVersion.toString()), createAsZip.isSelected(), includeShaders.isSelected(), includeTextures.isSelected(), true, unzipDebug.isSelected());
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

        //TODO Implement this into the actual code...
        url.addKeyListener(new KeyAdapter() {
            
           @Override
           public final void keyPressed(KeyEvent e) {
               boolean isCopy = shift && e.getKeyCode() == KeyEvent.VK_C;
               boolean isPaste = shift && e.getKeyCode() == KeyEvent.VK_V;
               boolean deleting = e.getKeyCode() == KeyEvent.VK_BACKSPACE;
               char character = e.getKeyChar(); 
               
               String letter = String.valueOf(character);
               if (Character.isLetterOrDigit(character) 
                   && letter.equals("/") 
                   && letter.equals(".") 
                   && letter.equals(":")
                   && !isCopy 
                   && !isPaste
                  && !deleting) {
                    e.consume();   
               }
           }
            
           @Override
           public final void keyTyped(KeyEvent e) {
                boolean isPaste = shift && e.getKeyCode() == KeyEvent.VK_V;
               boolean deleting = e.getKeyCode() == KeyEvent.VK_BACKSPACE;
               
               char charater = e.getKeyChar();
               
               String letter = String.valueOf(character);
               if (Character.isLetterOrDigit(character) 
                   && letter.equals("/") 
                   && letter.equals(".") 
                   && letter.equals(":")
                   && !isCopy 
                   && !isPaste 
                   && !deleting) {
                    e.consume();   
               } else {
                  SwingUtilities.invokeLater(() -> {
                        if  (isPaste) {
                            Clipboard clip = ToolKit.getDefaultToolKit().getSystemCliboard();
                            //TODO Take the system clipboard...
                            String data = (String) //System clipboard

                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < data.length(); i++) {
                                char character = data.charAt(i);

                                if (Character.isLetterOrDigit(character) 
                                       || letter.equals("/") 
                                       || letter.equals(".") 
                                       || letter.equals(":")) {
                                    builder.append(character);
                                }
                            }

                            url.setText(builder.toString());
                            
                            if (url.getText().isEmpty())
                                url.setText("https://locahost.com/download.txt");
                            
                            FilesUtilities.getConfig.saveCreatorURL(url.getText());
                      }
                  });
               }
           }
        });
        
        //TODO Implement this into the actual code...
        name.addKeyListener(new keyAdpater() {
            
            @Override
            public final voind keyPressed(KeyEvent e) {
               boolean isCopy = shift && e.getKeyCode() == KeyEvent.VK_C;
               boolean isPaste = shift && e.getKeyCode() == KeyEvent.VK_V;
               boolean deleting = e.getKeyCode() == KeyEvent.VK_BACKSPACE;
               char character = e.getKeyChar(); 
               
               String letter = String.valueOf(character);
               if (Character.isLetter(character) 
                   && letter.equals("_") 
                   && !isCopy 
                   && !isPaste
                    && !deleting) {
                    e.consume();   
               }
            }
            
            @Override
            public final void keyTyped(KeyEvent e) {
                boolean isPaste = shift && e.getKeyCode() == KeyEvent.VK_V;
                boolean deleting = e.getKeyCode() == KeyEvent.VK_BACKSPACE;
                char character = e.getKeyChar(); 
               
                String letter = String.valueOf(character);
                if (Character.isLetter(character) 
                    && letter.equals("_") 
                    && !isCopy 
                    && !isPaste
                   && !deleting) {
                        e.consume();   
                } else {
                       if (isPaste) {
                            Clipboard clip = ToolKit.getDefaultToolKit().getSystemCliboard();
                            //TODO Take the system clipboard...
                            String data = (String) //System clipboard

                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < data.length(); i++) {
                                char character = data.charAt(i);

                                if (Character.isLetter(character) 
                                       || letter.equals("_")) {
                                    builder.append(character);
                                }
                            }
                           
                            name.setText(builder.toString());
                       }
                        
                        if (name.getText().isEmpty())
                            name.setText("Modpack_" + Math.random());   
                      
                        FilesUtilities.getConfig.saveCreatorName(name.getText());
                }
            }
        });

        creatorFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

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
                try {
                    version.removeAllItems();
                    for (String v : ListVersions.listing.versions()) {
                        version.addItem(v);
                    }
                    if (contains(selected)) {
                        version.setSelectedItem(selected);
                    }
                } catch (Throwable ex) {
                    utils.log(ex);
                }
            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        version.addActionListener(e -> selected = String.valueOf(version.getSelectedItem()));
    }

    public interface versioning {

        static void checkVersions() {
            version.removeAllItems();
            versions.clear();
            try {
                for (String v : ListVersions.listing.versions()) {
                    version.addItem(v);
                    versions.add(v);
                }
            } catch (Throwable e) {
                utils.log(e);
            }

            if (versions.size() > 0) {
                createModPack.setEnabled(true);
                chooserTitle.setText("<html><h1>Yay, we detected " + version.getItemCount() + " mod loaders</h1></html>");
            } else {
                createModPack.setEnabled(false);
                chooserTitle.setText("<html><h1>No mod loaders found in this folder, select your minecraft folder in where you have a known mod loader (Forge/Fabric)<h1></html>");
            }
        }
    }

    private void disableResize(JSplitPane... panes) {
        for (JSplitPane pane : panes) {
            pane.setEnabled(false);
        }
    }

    private boolean contains(String value) {
        for (int i = 0; i < version.getItemCount(); i++) {
            if (version.getItemAt(i).equals(value)) {
                return true;
            }
        }
        return false;
    }
}

class ListVersions {

    public interface listing {

        @SneakyThrows
        static String[] versions() throws Throwable {
            Config config = new Config();

            File vFolder = new File(config.getMinecraftDir() + "/versions");
            File[] versions = vFolder.listFiles();
            ArrayList<String> names = new ArrayList<>();
            if (versions != null && !Arrays.asList(versions).isEmpty()) {
                for (File version : versions) {
                    String name = version.getName();
                    if (name.contains("forge") || name.contains("Forge") || name.contains("liteloader") || name.contains("LiteLoader")) {
                        File json = new File(version, name + ".json");
                        if (json.exists()) {
                            FileReader reader = new FileReader(json);
                            JSONParser jsonParser = new JSONParser();
                            JSONObject info = (JSONObject) jsonParser.parse(reader);

                            if (info.containsKey("id")) {
                                if (info.get("id").toString().contains("forge") || info.get("id").toString().contains("LiteLoader")) {
                                    names.add(name);
                                }
                            }
                        }
                    }
                }
            }

            String[] versionNames = new String[names.size()];
            for (int i = 0; i < versionNames.length; i++) {
                versionNames[i] = names.get(i);
            }

            return versionNames;
        }

        static String getRealVersion(String version) throws Throwable  {
            Config config = new Config();

            File json = new File(config.getMinecraftDir() + "/versions/" + version + "/" + version + ".json");
            if (json.exists()) {
                FileReader reader = new FileReader(json);
                JSONParser jsonParser = new JSONParser();
                JSONObject info = (JSONObject) jsonParser.parse(reader);

                if (info.containsKey("inheritsFrom")) {
                    return info.get("inheritsFrom").toString();
                }
            }
            return "";
        }
    }
}
