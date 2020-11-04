package ml.karmaconfigs.modpackupdater.utils;

import lombok.SneakyThrows;
import ml.karmaconfigs.modpackupdater.CreateFrame;
import ml.karmaconfigs.modpackupdater.MainFrame;
import ml.karmaconfigs.modpackupdater.SimpleFrame;
import ml.karmaconfigs.modpackupdater.utils.files.CopyFile;
import ml.karmaconfigs.modpackupdater.utils.files.CustomFile;
import ml.karmaconfigs.modpackupdater.utils.files.FilesUtilities;
import ml.karmaconfigs.modpackupdater.utils.modpack.Downloader;
import ml.karmaconfigs.modpackupdater.utils.modpack.ListMods;
import ml.karmaconfigs.modpackupdater.utils.modpack.Modpack;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.*;

public final class Utils extends MainFrame implements Runnable {

    public static JFrame info;
    public static JLabel infoScrollable;
    public static JLabel bPane = new JLabel();

    static {
        bPane.setOpaque(true);
        bPane.setBackground(Color.DARK_GRAY);
    }

    static int configsAmount = 0;
    static HashMap<String, String> debugData = new HashMap<>();
    private static String baseURL = "";
    private static String name = "";
    private static String version = "NULL_VERSION";
    private static String inheritVersion = "NULL_VERSION";
    private static boolean asZip = false;
    private static boolean includeShaders = false;
    private static boolean includeTextures = false;
    private static boolean includeConfigs = false;
    private static boolean enableDebug = false;

    public Utils() {
        if (!FilesUtilities.getUpdaterDir().exists()) {
            executeBoolean(FilesUtilities.getUpdaterDir().mkdirs());
        }
        File data = new File(FilesUtilities.getUpdaterDir() + "/modpacks");
        if (!data.exists()) {
            executeBoolean(data.mkdirs());
        }
        File logs = new File(FilesUtilities.getUpdaterDir() + "/logs");
        if (!logs.exists()) {
            executeBoolean(logs.mkdirs());
        }
        File downloads = new File(FilesUtilities.getUpdaterDir() + "/downloads");
        if (!downloads.exists()) {
            executeBoolean(downloads.mkdirs());
        }
        File uploads = new File(FilesUtilities.getUpdaterDir() + "/uploads");
        if (!uploads.exists()) {
            executeBoolean(uploads.mkdirs());
        }
    }

    public final boolean ModExists(File mod) {
        return mod.exists();
    }

    public final void log(Throwable throwable) {
        try {
            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            String date = dateFormat.format(today).replace("/", "-");
            File logsFolder = new File(FilesUtilities.getUpdaterDir() + "/logs");
            if (!logsFolder.exists()) {
                executeBoolean(logsFolder.mkdirs());
            }
            File newLog = new File(logsFolder, "log_" + date + ".txt");

            if (!newLog.exists()) {
                executeBoolean(newLog.createNewFile());
            }

            StringBuilder builder = new StringBuilder();
            for (StackTraceElement info : throwable.getStackTrace()) {
                builder.append(info.toString()).append("<br>");
            }

            setDebug(rgbColor("Exception: " + new Throwable(throwable).fillInStackTrace(), 255, 100, 100), true);
            setDebug(rgbColor(builder.toString(), 120, 100, 100), false);
            setDebug(rgbColor("Log " + FilesUtilities.getPath(newLog) + " have been saved", 120, 200, 155), false);

            modifyLog(newLog, "Exception info: " + new Throwable(throwable).fillInStackTrace(), throwable);
        } catch (Throwable e) {
            StringBuilder exception = new StringBuilder();

            for (StackTraceElement element : e.getStackTrace()) {
                exception.append(element).append("<br>");
            }

            setDebug(rgbColor("Exception: " + e.fillInStackTrace(), 255, 100, 100), true);
            setDebug(rgbColor(exception.toString(), 120, 100, 100), false);
        }
    }

    public final void setupCreator(String baseUrl, String modpackName, String loaderVersion, String realVersion, boolean zip, boolean withShaders, boolean withTextures, boolean configs, boolean debug) {
        baseURL = baseUrl;
        name = modpackName;
        version = loaderVersion;
        inheritVersion = realVersion;
        asZip = zip;
        includeShaders = withShaders;
        includeTextures = withTextures;
        includeConfigs = configs;
        enableDebug = debug;
    }

    @Override
    public void run() {
        CopyFile copy = null;
        try {
            Modpack modpack = new Modpack(name);
            modpack.createFile();
            if (!name.isEmpty()) {
                modpack.getFile().write("NAME", name);
            } else {
                modpack.getFile().write("NAME", "Null name");
            }
            modpack.getFile().write("DOWNLOAD", baseURL + "/download.txt");
            modpack.getFile().write("SHADERS", includeShaders);
            modpack.getFile().write("TEXTURES", includeTextures);
            modpack.getFile().write("VERSION", version);
            modpack.getFile().write("INHERIT", inheritVersion);

            delZip();
            deleteInModFolder(modpack);
            deleteInVersionFolder(modpack);

            File downloadTxt = new File(FilesUtilities.getModpackUploadDir(modpack), "download.txt");
            if (downloadTxt.exists() && downloadTxt.delete()) {
                setDebug(rgbColor("Removed old download.txt file", 155, 240, 175), true);
            }

            setDebug(rgbColor("Creating modpack download file...", 155, 240, 175), true);
            File modsFolder = new File(mcFolder + "/mods");
            if (modsFolder.exists()) {
                File[] mods = modsFolder.listFiles();

                ArrayList<String> urls = new ArrayList<>();
                ArrayList<String> modNames = new ArrayList<>();
                if (mods != null && !Arrays.asList(mods).isEmpty()) {
                    int modsAmount = 0;
                    for (File mod : mods) {
                        if (Downloader.isMod(mod)) {
                            modNames.add(mod.getName());
                            modsAmount++;
                            String url;

                            File destDir = new File(FilesUtilities.getModpackUploadDir(modpack) + "/mods");
                            if (!destDir.exists() && destDir.mkdirs()) {
                                System.out.println("Executed");
                            }
                            FileUtils.copyFileToDirectory(mod, destDir);

                            if (!asZip) {
                                url = baseURL + "/download/" + mod.getName() + "=" + mod.getName();
                                setDebug(rgbColor("Mod detected " + FilesUtilities.getPath(mod) + " ( <span style=\"color: rgb(95, 210, 210);\">" + url.split("=")[0] + "</span> )", 95, 140, 210), modsAmount == 1);
                                urls.add(url);
                            } else {
                                setDebug(rgbColor("Mod detected " + FilesUtilities.getPath(mod) + " ( <span style=\"color: rgb(95, 210, 210);\"> Will be added to modpack.zip </span> )", 95, 140, 210), modsAmount == 1);
                            }
                        }
                    }

                    setDebug(rgbColor("Detected a total of " + modsAmount + " mods", 120, 200, 155), true);

                    if (includeTextures) {
                        includeTextures = copyTexturePacks(modpack);
                    }
                    if (includeShaders) {
                        includeShaders = copyShaderPacks(modpack);
                    }
                    if (includeConfigs) {
                        includeConfigs = copyConfigs(modpack);
                    }

                    File vFile = null;
                    if (version != null && !version.isEmpty() && new File(FilesUtilities.getMinecraftDir() + "/versions/" + version).exists()) {
                        vFile = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version);
                        File destDir = new File(FilesUtilities.getModpackUploadDir(modpack) + "/versions/");

                        if (!destDir.exists() && destDir.mkdirs()) {
                            System.out.println("Executed");
                        }

                        File[] files = vFile.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (!file.isDirectory()) {
                                    FileUtils.copyFileToDirectory(file, destDir);
                                }
                            }
                        }
                        vFile = destDir;
                    }

                    if (asZip) {
                        copy = new CopyFile(vFile, modpack, includeTextures, includeShaders, includeConfigs, enableDebug);
                        Thread thread = new Thread(copy, "Zipping");
                        thread.start();
                    }
                } else {
                    throw new Exception("Mods folder is null or empty");
                }

                File dlFile = new File(FilesUtilities.getModpackUploadDir(modpack), "download.txt");

                if (!dlFile.exists()) {
                    if (dlFile.createNewFile()) {
                        setDebug(rgbColor("Created file " + dlFile.getName(), 120, 200, 155), true);
                    }
                }

                CustomFile file = new CustomFile(dlFile, true);
                if (!name.isEmpty()) {
                    file.write("NAME", name);
                }
                file.write("DOWNLOAD", baseURL + "/download.txt");
                file.write("SHADERS", includeShaders);
                file.write("TEXTURES", includeTextures);
                file.write("CONFIGS", includeConfigs);
                if (version != null && !version.isEmpty() && new File(FilesUtilities.getMinecraftDir() + "/versions/" + version).exists()) {
                    file.write("VERSION", version);
                    file.write("INHERIT", inheritVersion);
                    if (!asZip) {
                        File[] versionsFiles = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version).listFiles();
                        ArrayList<String> vURLs = new ArrayList<>();
                        if (versionsFiles != null) {
                            for (File vFile : versionsFiles) {
                                vURLs.add(baseURL + "/download/versions/" + vFile.getName() + "=" + vFile.getName());
                            }
                        }
                        file.write("V_URLS", vURLs);
                        file.write("CFG_URLS", new ArrayList<>(getConfigFiles(baseURL, modpack)));
                    }
                } else {
                    file.write("VERSION", "NULL");
                }
                if (!asZip) {
                    file.write("URLS", urls);
                    modpack.getFile().write("URLS", urls);
                } else {
                    file.write("URL", baseURL + "/download/modpack.zip");
                    modpack.getFile().write("URL", baseURL + "/download/modpack.zip");
                }
                file.write("MODS", modNames);
                modpack.getFile().write("MODS", modNames);

                if (asZip) {
                    assert copy != null;

                    Timer timer = new Timer();
                    CopyFile finalCopy = copy;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (finalCopy.isFinished()) {
                                displayPackInfo(modpack);
                                timer.cancel();
                            }
                        }
                    }, 0, 1);
                } else {
                    displayPackInfo(modpack);
                }
            } else {
                setDebug(rgbColor("Couldn't find mods folder, is it the minecraft folder? ( <span style=\"color: rgb(100, 100, 255);\">" + FilesUtilities.getPath(mcFolder) + "</span> )", 120, 100, 100), true);
            }

            Dimension size = modpacks.getPreferredSize();
            modpackLabel.remove(modpacks);
            modpacks.removeAllItems();
            modpacks = new JComboBox<>(Modpack.listing.modpacks());
            modpacks.setPreferredSize(size);
            modpackLabel.add(modpacks);
            restartModpacksListeners();
            SimpleFrame.restartModpacksListeners();
        } catch (Throwable e) {
            log(e);
        }
    }

    public final void setDebug(String newLine, boolean doubleLine) {
        String oldText = bPane.getText().replace("<html>", "").replace("</html>", "");

        String separator = "<br>";
        if (doubleLine) {
            separator = "<br><br>";
        }

        bPane.setText("<html>" + oldText + separator + newLine + "</html>");
        internal_label.setText("<html>" + oldText + separator + newLine + "</html>");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MainFrame.jsp.getVerticalScrollBar().setValue(MainFrame.jsp.getVerticalScrollBar().getMaximum());
                SimpleFrame.jsp.getVerticalScrollBar().setValue(SimpleFrame.jsp.getVerticalScrollBar().getMaximum());
            }
        }, 500);
    }

    public final void setProgress(String title, int progress) {
        if (progress == 1) {
            progress = 0;
        }
        bar.setValue(progress);
        barLabel.setText("<html><div><h3>" + title + "</h3></div></html>");
    }

    public final void displayPackInfo(Modpack modpack) {
        if (info == null) {
            info = new JFrame();

            try {
                info.setIconImage(ImageIO.read((MainFrame.class).getResourceAsStream("/logo.png")));
            } catch (Throwable e) {
                e.printStackTrace();
            }

            info.setPreferredSize(new Dimension(800, 800));
        }

        JLabel nameAndVersion = new JLabel();
        nameAndVersion.setHorizontalAlignment(SwingConstants.CENTER);
        nameAndVersion.setText("<html><h2 style=\" color: rgb(65, 110, 225);\">" + modpack.getName() + " [ " + modpack.getVersionName() + " ]" + "</h2></html>");
        nameAndVersion.setPreferredSize(new Dimension(400, nameAndVersion.getHeight()));

        JTextField download = new JTextField();
        download.setPreferredSize(new Dimension(50, download.getHeight()));
        download.setText(modpack.getDownloadURL());
        download.setEditable(false);

        JCheckBox shaders = new JCheckBox("Shaders");
        shaders.setEnabled(false);
        shaders.setSelected(modpack.hasShaders());
        JCheckBox textures = new JCheckBox("Resourcepacks");
        textures.setEnabled(false);
        textures.setSelected(modpack.hasTextures());
        JCheckBox configs = new JCheckBox("Configs");
        configs.setEnabled(false);
        configs.setSelected(modpack.hasConfigs());

        infoScrollable = new JLabel();
        JScrollPane modList = new JScrollPane(infoScrollable);

        if (FilesUtilities.getConfig.getTheme().equals("System default")) {
            infoScrollable.setOpaque(true);
            infoScrollable.setBackground(Color.GRAY);
        } else {
            infoScrollable.setOpaque(true);
            infoScrollable.setBackground(Color.DARK_GRAY);
        }

        ArrayList<String> modNames = new ArrayList<>();
        for (File modF : modpack.getMods()) {
            modNames.add(modF.getName());
        }

        infoScrollable.setText("<html>" + rgbColor(modNames, 100, 170, 245) + "</html>");

        JSplitPane textualInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT, nameAndVersion, download);
        textualInfo.setEnabled(false);
        JSplitPane zipInfo1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, shaders, textures);
        zipInfo1.setEnabled(false);
        JSplitPane zipInfo2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, zipInfo1, configs);
        zipInfo2.setEnabled(false);
        JSplitPane zipInfo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, zipInfo2, new JPanel());
        zipInfo.setEnabled(false);
        JSplitPane finalInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textualInfo, zipInfo);
        finalInfo.setEnabled(false);
        JSplitPane modInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT, finalInfo, modList);
        modInfo.setEnabled(false);

        info.setTitle("Modpack \"" + modpack.getName() + "\" info");
        info.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        info.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        info.setLocation(dim.width / 2 - info.getSize().width / 2, dim.height / 2 - info.getSize().height / 2);
        info.setResizable(false);
        info.add(modInfo);
        info.setVisible(true);

        info.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                info = null;
            }

            @Override
            public void windowClosed(WindowEvent e) {
                info = null;
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
    }

    public final void saveCurrentModpack(Modpack modpack) {
        File modpackInfo = new File(FilesUtilities.getUpdaterDir(), "modpack.info");

        CustomFile file = new CustomFile(modpackInfo, true);

        file.write("CURRENT", modpack.getName());
    }

    /**
     * Reload everything in the tool
     */
    public final void reloadTool() {
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(frame);
            for (Component component : frame.getComponents()) {
                if (component != null) {
                    SwingUtilities.updateComponentTreeUI(component);
                }
            }

            SwingUtilities.updateComponentTreeUI(SimpleFrame.frame);
            for (Component component : SimpleFrame.frame.getComponents()) {
                if (component != null) {
                    SwingUtilities.updateComponentTreeUI(component);
                }
            }


            SwingUtilities.updateComponentTreeUI(CreateFrame.chooser);
            for (Component component : CreateFrame.chooser.getComponents()) {
                if (component != null) {
                    SwingUtilities.updateComponentTreeUI(component);
                }
            }

            if (CreateFrame.creatorFrame != null) {
                SwingUtilities.updateComponentTreeUI(CreateFrame.creatorFrame);
                for (Component component : CreateFrame.creatorFrame.getComponents()) {
                    if (component != null) {
                        SwingUtilities.updateComponentTreeUI(component);
                    }
                }
            }

            if (CreateFrame.errorFrame != null) {
                SwingUtilities.updateComponentTreeUI(CreateFrame.errorFrame);
                for (Component component : CreateFrame.errorFrame.getComponents()) {
                    if (component != null) {
                        SwingUtilities.updateComponentTreeUI(component);
                    }
                }
            }

            if (info != null) {
                SwingUtilities.updateComponentTreeUI(info);
                for (Component component : info.getComponents()) {
                    if (component != null) {
                        SwingUtilities.updateComponentTreeUI(component);
                    }
                }
            }
        });
    }

    public final boolean isOutdated(String url) {
        int error = 0;

        ListMods listMods = new ListMods(url);
        ArrayList<File> listedMods = listMods.getMods();
        if (!listedMods.isEmpty()) {
            for (File listedMod : listedMods) {
                if (Downloader.isMod(listedMod)) {
                    if (!ModExists(listedMod)) {
                        error++;
                    }
                }
            }
        }

        return error > 0;
    }

    private int blankPacks() {
        int amount = 0;

        File[] packs = new File(FilesUtilities.getUpdaterDir() + "/modpacks/").listFiles();
        if (packs != null && !Arrays.asList(packs).isEmpty()) {
            for (File pack : packs) {
                String name = pack.getName();
                if (name.contains("_")) {
                    if (name.split("_")[0].equals("blank")) {
                        amount++;
                    }
                }
            }
        }

        return amount;
    }

    public final String rgbColor(String text, int red, int blue, int green) {
        return "<span style=\"color: rgb({red}, {blue}, {green});\">".replace("{red}", String.valueOf(red)).replace("{blue}", String.valueOf(blue)).replace("{green}", String.valueOf(green)) + text + "</span>";
    }

    public final String rgbColor(ArrayList<String> list, int red, int blue, int green) {
        return "<span style=\"color: rgb({red}, {blue}, {green});\">".replace("{red}", String.valueOf(red)).replace("{blue}", String.valueOf(blue)).replace("{green}", String.valueOf(green)) + list.toString().replace("[", "").replace("]", "").replace(",", "<br>") + "</span>";
    }

    public final String rgbColor(HashMap<String, String> map, int redForKey, int blueForKey, int greenForKey, int redForValue, int blueForValue, int greenForValue) {
        ArrayList<String> toString = new ArrayList<>();

        for (String key : map.keySet()) {
            toString.add("<span style=\"color: rgb(" + redForKey + ", " + blueForKey + ", " + greenForKey + ");\">" + key + "</span>");
            toString.add("<span style=\"color: rgb(" + redForValue + ", " + blueForValue + ", " + greenForValue + ");\">" + map.get(key) + "</span>");
        }

        return "<span>" + toString.toString().replace("[", "").replace("]", "").replace(",", "<br>") + "</span>";
    }

    public final String getModpackName(String url) throws Throwable {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new URL(url).openStream()));

        String name = "";
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains(":")) {
                String path = inputLine.split(":")[0];
                String value = inputLine.replace(path + ": ", "");

                if (path.equals("NAME")) {
                    name = value;
                    break;
                }
            }
        }

        if (name.isEmpty()) name = "blank_" + blankPacks();

        return name;
    }

    /**
     * Get the current modpack name
     *
     * @return a String
     */
    public final String getCurrentModpack() {
        File modpackInfo = new File(FilesUtilities.getUpdaterDir(), "modpack.info");

        if (modpackInfo.exists()) {
            CustomFile file = new CustomFile(modpackInfo, false);

            return file.getString("CURRENT", "");
        }
        return "";
    }

    private void deleteInModFolder(Modpack modpack) {
        File inModFolder = new File(FilesUtilities.getModpackUploadDir(modpack) + "/mods/");
        if (inModFolder.exists()) {
            if (inModFolder.delete()) {
                setDebug(rgbColor("Removed old in-mods folder", 155, 240, 175), true);
            } else {
                File[] mods = inModFolder.listFiles();
                if (mods != null) {
                    int removed = 0;
                    for (File mod : mods) {
                        removed++;
                        if (mod.delete()) {
                            setDebug(rgbColor("Removed old in-mod file " + mod.getName(), 155, 240, 175), removed == 1);
                        } else {
                            setDebug(rgbColor("Couldn't remove old in-mod file " + mod.getName(), 220, 100, 100), removed == 1);
                        }
                    }
                    setDebug(rgbColor("Removed a total of " + removed + " old mod files", 155, 240, 175), true);
                }
            }
        }
    }

    private void deleteInVersionFolder(Modpack modpack) {
        File inVersionFile = new File(FilesUtilities.getModpackUploadDir(modpack) + "/versions/");
        if (inVersionFile.exists()) {
            if (inVersionFile.delete()) {
                setDebug(rgbColor("Removed old in-versions folder", 155, 240, 175), true);
            } else {
                File[] files = inVersionFile.listFiles();
                if (files != null) {
                    int removed = 0;
                    for (File file : files) {
                        removed++;
                        if (file.delete()) {
                            setDebug(rgbColor("Removed old in-version file " + file.getName(), 155, 240, 175), removed == 1);
                        } else {
                            setDebug(rgbColor("Couldn't remove old in-version file " + file.getName(), 220, 100, 100), removed == 1);
                        }
                    }
                    setDebug(rgbColor("Removed a total of " + removed + " old version files", 155, 240, 175), true);
                }
            }
        }
    }

    private void deleteInTextureFolder(Modpack modpack) {
        File inTexturesFolder = new File(FilesUtilities.getModpackUploadDir(modpack) + "/texturepacks/");
        if (inTexturesFolder.exists()) {
            if (inTexturesFolder.delete()) {
                setDebug(rgbColor("Removed old in-textures folder", 155, 240, 175), true);
            } else {
                File[] textures = inTexturesFolder.listFiles();
                if (textures != null) {
                    int removed = 0;
                    for (File texture : textures) {
                        removed++;
                        if (texture.delete()) {
                            setDebug(rgbColor("Removed old in-texture file " + texture.getName(), 155, 240, 175), removed == 1);
                        } else {
                            setDebug(rgbColor("Couldn't remove old in-texture file " + texture.getName(), 220, 100, 100), removed == 1);
                        }
                    }
                    setDebug(rgbColor("Removed a total of " + removed + " old texture files", 155, 240, 175), true);
                }
            }
        }
    }

    private void deleteInShaderFolder(Modpack modpack) {
        File inShadersFolder = new File(FilesUtilities.getModpackUploadDir(modpack) + "/shaderpacks/");
        if (inShadersFolder.exists()) {
            if (inShadersFolder.delete()) {
                setDebug(rgbColor("Removed old in-shader folder", 155, 240, 175), true);
            } else {
                File[] shaders = inShadersFolder.listFiles();
                if (shaders != null) {
                    int removed = 0;
                    for (File shader : shaders) {
                        removed++;
                        if (shader.delete()) {
                            setDebug(rgbColor("Removed old in-shaders file " + shader.getName(), 155, 240, 175), removed == 1);
                        } else {
                            setDebug(rgbColor("Couldn't remove old in-shaders file " + shader.getName(), 220, 100, 100), removed == 1);
                        }
                    }
                    setDebug(rgbColor("Removed a total of " + removed + " old shaders files", 155, 240, 175), true);
                }
            }
        }
    }

    private void delZip() {
        File destZip = new File(FilesUtilities.getUpdaterDir(), "modpack.zip");
        if (destZip.exists()) {
            if (destZip.delete()) {
                setDebug(rgbColor("Removed old modpack.zip", 155, 240, 175), true);
            } else {
                setDebug(rgbColor("Couldn't remove modpack.zip", 220, 100, 100), true);
            }
        }
    }

    private void executeBoolean(boolean bool) {
        try {
            if (bool) {
                System.out.println("Executed");
            }
        } catch (Throwable ignored) {
        }
    }

    private void modifyLog(File logFile, String firstLine, Throwable info) {
        InputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader reader = null;
        try {
            Date now = new Date();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");

            String prefix = "[ " + dateFormat.format(now) + " ] ";

            in = new FileInputStream(logFile);
            inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            reader = new BufferedReader(inReader);

            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line + "\n");
            }

            lines.add(prefix + firstLine + "\n\n");
            StackTraceElement[] elements = info.getStackTrace();
            for (int i = 0; i < elements.length; i++) {
                StackTraceElement element = elements[i];
                if (i != elements.length - 1) {
                    lines.add(element + "\n");
                } else {
                    lines.add(element + "\n\n");
                }
            }

            FileWriter writer = new FileWriter(logFile);
            for (int i = 0; i < lines.size(); i++) {
                if (i != lines.size() - 1) {
                    writer.write(lines.get(i));
                } else {
                    writer.write(lines.get(i) + "\n-------------------------------------------------------------------");
                }
            }
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            StringBuilder exception = new StringBuilder();

            for (StackTraceElement element : e.getStackTrace()) {
                exception.append(element).append("<br>");
            }

            setDebug(rgbColor("Exception: " + e.fillInStackTrace(), 255, 100, 100), true);
            setDebug(rgbColor(exception.toString(), 120, 100, 100), false);
        } finally {
            try {
                if (in != null) {
                    in.close();
                    if (inReader != null) {
                        inReader.close();
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @SneakyThrows
    private boolean copyShaderPacks(Modpack modpack) {
        try {
            deleteInShaderFolder(modpack);
            setDebug(rgbColor("Looking for shaderpacks packs...", 155, 240, 175), true);
            File shadersFolder = new File(mcFolder + "/shaderpacks");
            if (shadersFolder.exists()) {
                File[] shaderPacks = shadersFolder.listFiles();
                if (shaderPacks != null && !Arrays.asList(shaderPacks).isEmpty()) {
                    HashMap<String, String> debugData = new HashMap<>();
                    int shadersAmount = 0;
                    for (File shaderpack : shaderPacks) {
                        if (Downloader.isZip(shaderpack)) {
                            shadersAmount++;
                            File destDir = new File(FilesUtilities.getModpackUploadDir(modpack) + "/shaderpacks");
                            if (!destDir.exists() && destDir.mkdirs()) {
                                System.out.println("Executed");
                            }
                            FileUtils.copyFileToDirectory(shaderpack, destDir);
                            debugData.put("Detected shaderpack " + FilesUtilities.getPath(shaderpack), "Shaderpack " + shaderpack.getName() + " have been added to modpack.zip");
                        }
                    }

                    setDebug(rgbColor(debugData, 100, 100, 255, 155, 0, 155), true);
                    setDebug(rgbColor("Detected a total of " + shadersAmount + " shaderpacks", 120, 200, 155), true);
                    return true;
                }
            } else {
                setDebug(rgbColor("Couldn't find shaderpacks folder, is it the minecraft folder? ( <span style=\"color: rgb(100, 100, 255);\">" + FilesUtilities.getPath(mcFolder) + "</span> )", 120, 100, 100), true);
            }
        } catch (Throwable e) {
            log(e);
        }
        return false;
    }

    @SneakyThrows
    private boolean copyTexturePacks(Modpack modpack) {
        try {
            deleteInTextureFolder(modpack);
            setDebug(rgbColor("Looking for texture packs...", 155, 240, 175), true);
            File texturesFolder = new File(mcFolder + "/resourcepacks");
            if (texturesFolder.exists()) {
                File[] texturePacks = texturesFolder.listFiles();
                if (texturePacks != null && !Arrays.asList(texturePacks).isEmpty()) {
                    HashMap<String, String> debugData = new HashMap<>();
                    int texturesAmount = 0;
                    for (File texturepack : texturePacks) {
                        if (Downloader.isZip(texturepack)) {
                            texturesAmount++;
                            File destDir = new File(FilesUtilities.getModpackUploadDir(modpack) + "/resourcepacks");
                            if (!destDir.exists() && destDir.mkdirs()) {
                                System.out.println("Executed");
                            }
                            FileUtils.copyFileToDirectory(texturepack, destDir);
                            debugData.put("Detected texturepack " + FilesUtilities.getPath(texturepack), "Texturepack " + texturepack.getName() + " have been added to modpack.zip");
                        }
                    }

                    setDebug(rgbColor(debugData, 100, 100, 255, 155, 0, 155), true);
                    setDebug(rgbColor("Detected a total of " + texturesAmount + " texturepacks", 120, 200, 155), true);
                    return true;
                }
            } else {
                setDebug(rgbColor("Couldn't find texturepacks folder, is it the minecraft folder? ( <span style=\"color: rgb(100, 100, 255);\">" + FilesUtilities.getPath(mcFolder) + "</span> )", 120, 100, 100), true);
            }
        } catch (Throwable e) {
            log(e);
        }
        return false;
    }

    @SneakyThrows
    private boolean copyConfigs(Modpack modpack) {
        try {
            configsAmount = 0;
            debugData.clear();
            File inConfig = new File(FilesUtilities.getModpackUploadDir(modpack) + "/config");
            if (inConfig.exists() && inConfig.delete()) {
                System.out.println("Executed");
            }

            setDebug(rgbColor("Looking for mod configs...", 155, 240, 175), true);
            File configFolder = new File(mcFolder + "/config");
            if (configFolder.exists()) {
                File[] configs = configFolder.listFiles();
                if (configs != null && !Arrays.asList(configs).isEmpty()) {
                    for (File config : configs) {
                        if (config.isDirectory()) {
                            copyConfigsFolder(modpack, config);
                        } else if (config.isFile()) {
                            configsAmount++;
                            File destDir = new File(FilesUtilities.getModpackUploadDir(modpack) + "/config");
                            if (!destDir.exists() && destDir.mkdirs()) {
                                System.out.println("Executed");
                            }
                            FileUtils.copyFileToDirectory(config, destDir);
                            debugData.put("Detected mod config " + FilesUtilities.getPath(config), "Config file " + config.getName() + " have been added to modpack.zip");
                        }
                    }

                    setDebug(rgbColor(debugData, 100, 100, 255, 155, 0, 155), true);
                    setDebug(rgbColor("Detected a total of " + configsAmount + " config files", 120, 200, 155), true);
                    return true;
                }
            }
        } catch (Throwable e) {
            log(e);
        }
        return false;
    }

    /**
     * Copy configs folder
     */
    private void copyConfigsFolder(Modpack modpack, File mainFolder) {
        try {
            File[] inConfigFiles = mainFolder.listFiles();

            if (inConfigFiles != null) {
                for (File config : inConfigFiles) {
                    if (config.isDirectory()) {
                        copyConfigsFolder(modpack, config);
                    } else {
                        if (Downloader.isConfig(config)) {
                            configsAmount++;
                            File destDir = new File(FilesUtilities.getModpackUploadDir(modpack) + "/config/" + FilesUtilities.constructFolder("config", config).replace("/" + config.getName(), ""));
                            if (!destDir.exists() && destDir.mkdirs()) {
                                System.out.println("Executed");
                            }
                            FileUtils.copyFileToDirectory(config, destDir);
                            debugData.put("Detected mod config " + FilesUtilities.getPath(config), "Config file " + config.getName() + " have been added to modpack.zip");
                        }
                    }
                }
            }
        } catch (Throwable e) {
            log(e);
        }
    }

    /**
     * Get the config files directories in an arraylist
     *
     * @return an arraylist
     */
    private HashSet<String> getConfigFiles(String baseURL, Modpack modpack) {
        HashSet<String> configs = new HashSet<>();

        File[] configFiles = new File(FilesUtilities.getModpackUploadDir(modpack) + "/config").listFiles();

        if (configFiles != null) {
            for (File file : configFiles) {
                if (!file.isDirectory()) {
                    if (Downloader.isConfig(file)) {
                        configs.add(baseURL + "/download/config/" + file.getName() + "=" + file.getName());
                    }
                } else {
                    configs.addAll(getConfigFilesFromDir(baseURL, file, configs));
                }
            }
        }

        return configs;
    }

    /**
     * Get the config files from directories in an arraylist
     *
     * @param baseURL      the base download url
     * @param mainFolder   the main config folder
     * @param originalList the original config file list
     */
    private HashSet<String> getConfigFilesFromDir(String baseURL, File mainFolder, HashSet<String> originalList) {
        try {
            File[] inConfigFiles = mainFolder.listFiles();

            if (inConfigFiles != null) {
                for (File config : inConfigFiles) {
                    if (config.isDirectory()) {
                        getConfigFilesFromDir(baseURL, config, originalList);
                    } else {
                        if (Downloader.isConfig(config)) {
                            originalList.add(baseURL + "/download/config/" + FilesUtilities.constructFolder("config", config) + "=" + FilesUtilities.constructFolder("config", config));
                        }
                    }
                }
            }
        } catch (Throwable e) {
            log(e);
        }

        return originalList;
    }

    public interface os {
        static String getOS() {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

            if ((OS.contains("mac")) || (OS.contains("darwin"))) {
                return ("Mac");
            } else if (OS.contains("win")) {
                return ("Windows");
            } else if (OS.contains("nux")) {
                return ("Linux");
            } else {
                return ("Linux");
            }
        }
    }
}