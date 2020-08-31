package ml.karmaconfigs.ModPackUpdater.Utils.ModPack;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.CustomFile;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.Unzip;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class Downloader implements Runnable {

    private static final Utils utils = new Utils();

    private static String url;
    private final static ArrayList<String> mods = new ArrayList<>();
    private final static ArrayList<String> versions = new ArrayList<>();
    private final static ArrayList<String> configs = new ArrayList<>();
    private final static HashMap<String, URL> downloads = new HashMap<>();

    private static boolean zipVersion = false;
    private static boolean isZip = false;

    private static URL zipURL;
    private static Modpack modpack;

    public Downloader(String url) throws Throwable {
        Downloader.url = url;
        modpack = new Modpack(utils.getModpackName(url));
        utils.saveCurrentModpack(modpack);
        versions.clear();
    }

    @lombok.SneakyThrows
    @Override
    public void run() {
        boolean hasTexturepacks = false;
        boolean hasShaderpacks = false;
        boolean hasConfigs = false;
        try {
            URL downloadURL = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(downloadURL.openStream());
            FileOutputStream fos = new FileOutputStream(FilesUtilities.getFileFromURL(url));
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            CustomFile file = new CustomFile(FilesUtilities.getFileFromURL(url), true);

            hasTexturepacks = file.getBoolean("TEXTURES", false);
            hasShaderpacks = file.getBoolean("SHADERS", false);
            hasConfigs = file.getBoolean("CONFIGS", false);

            HashMap<String, String> urlData = new HashMap<>();
            if (!file.getList("URLS").isEmpty()) {
                List<Object> urls = file.getList("URLS");

                for (Object obj : urls) {
                    if (obj.toString().contains("=")) {
                        String mURL = obj.toString().split("=")[0];
                        String mName = obj.toString().replace(mURL + "=", "");

                        downloads.put(mName, new URL(mURL));
                        mods.add(mName);

                        urlData.put("Detected mod " + mURL, "Mod name: " + mName);
                    }
                }
            } else {
                urlData.put("Detected modpack.zip", "Will download a .zip and unzip it in " + FilesUtilities.getPath(FilesUtilities.getModpackDownloadDir(modpack)));
                zipURL = new URL(file.getString("URL", null));
                isZip = true;
            }

            if (!file.getList("V_URLS").isEmpty()) {
                zipVersion = false;
                List<Object> versionURLs= file.getList("V_URLS");

                for (Object obj : versionURLs) {
                    if (obj.toString().contains("=")) {
                        String mURL = obj.toString().split("=")[0];
                        String mName = obj.toString().replace(mURL + "=", "");

                        versions.add(mName);
                        downloads.put(mName, new URL(mURL));

                        urlData.put("Detected version download url " + url, "Version info: " + file.getString("VERSION", "NULL"));
                    }
                }
            } else {
                zipVersion = true;
            }

            if (!file.getList("CFG_URLS").isEmpty()) {
                zipVersion = false;
                List<Object> configURLs = file.getList("CFG_URLS");

                for (Object obj : configURLs) {
                    if (obj.toString().contains("=")) {
                        String cURL = obj.toString().split("=")[0];
                        String cName = obj.toString().replace(cURL + "=", "");

                        configs.add(cName);
                        downloads.put(cName, new URL(cURL));
                    }
                }
            }

            utils.setDebug((utils.rgbColor(urlData, 155, 240, 175, 120, 225, 255)), true);

            moveMods();
            if (hasTexturepacks) {
                renameTextures();
            }
            if (hasShaderpacks) {
                renameShaders();
            }
            if (hasConfigs) {
                renameConfigs();
            }
        } catch (Throwable e) {
            utils.log(e);
        }

        ArrayList<String> green = new ArrayList<>();
        ArrayList<String> red = new ArrayList<>();

        if (!isZip) {
            int totalDownloads = 0;
            int modAmount = 0;
            int versionAmount = 0;
            int configAmount = 0;
            int unknown = 0;
            int success = 0;
            int error = 0;
            for (String name : downloads.keySet()) {
                URL url = downloads.get(name);
                totalDownloads++;
                try {
                    int count;
                    try {
                        URLConnection connection = url.openConnection();
                        connection.connect();

                        int lengthOfFile = connection.getContentLength();

                        File destination;
                        if (mods.contains(name)) {
                            destination = new File(FilesUtilities.getModpackMods(modpack), name);
                            modAmount++;
                        } else {
                            if (versions.contains(name)) {
                                destination = new File(FilesUtilities.getModpackDownloadDir(modpack) + "/versions/" + name);
                                versionAmount++;
                            } else {
                                if (configs.contains(name)) {
                                    destination = new File(FilesUtilities.getModpackDownloadDir(modpack) + "/config/" + name);
                                    configAmount++;
                                } else {
                                    destination = new File(FilesUtilities.getModpackDownloadDir(modpack) + "/unknown/" + name);
                                    unknown++;
                                }
                            }
                        }

                        File destFolder = new File(FilesUtilities.getPath(destination).replace("/" + name, ""));

                        if (!destFolder.exists() && destFolder.mkdirs()) {
                            System.out.println("Executed");
                        }

                        InputStream input = new BufferedInputStream(url.openStream(), 8192);
                        OutputStream output = new FileOutputStream(destination);

                        byte[] data = new byte[1024];

                        long percentage = 0;
                        while ((count = input.read(data)) != -1) {
                            long total = destination.length();
                            if (percentage != total * 100 / lengthOfFile) {
                                percentage = total * 100 / lengthOfFile;
                                if (percentage < 98) {
                                    if (mods.contains(name)) {
                                        utils.setProgress("Downloading mod " + name, (int) percentage);
                                    } else {
                                        if (versions.contains(name)) {
                                            utils.setProgress("Downloading version " + name, (int) percentage);
                                        } else {
                                            if (configs.contains(name)) {
                                                utils.setProgress("Downloading mod config " + name, (int) percentage);
                                            } else {
                                                utils.setProgress("Downloading non-defined file " + name + "<span style=\"color: rgb(220, 100, 100)\">COULD CONTAIN MALWARE</span>", (int) percentage);
                                            }
                                        }
                                    }
                                }
                            }

                            output.write(data, 0, count);
                        }

                        output.flush();
                        output.close();
                        input.close();
                    } catch (Exception e) {
                        utils.log(e);
                    }
                    success++;
                    green.add("Downloaded file " + name);
                } catch (Throwable e) {
                    error++;
                    red.add("Failed to download file " + name);
                } finally {
                    if (totalDownloads == downloads.keySet().size()) {
                        utils.setProgress("Download status bar", 1);

                        utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
                        utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());

                        utils.setDebug(utils.rgbColor("Tried to download a total of " + totalDownloads + " files [ {mod} mods, {version} version files, {config} config files, {unknown} unknown files ] <span style=\" color: green;\">".replace("{mod}", String.valueOf(modAmount)).replace("{version}", String.valueOf(versionAmount)).replace("{config}", String.valueOf(configAmount)).replace("{unknown}", String.valueOf(unknown)) + success + "</span> were success and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);

                        moveContentsToMinecraft("mods", false);
                        if (hasConfigs) {
                            moveContentsToMinecraft("config", true);
                        }
                    }
                }
            }

            if (modpack.hasVersion()) {
                modpack.installVersion();
            }
        } else {
            utils.setDebug(utils.rgbColor("Started the download of modpack.zip from " + zipURL, 120, 200, 155), true);

            URL url = zipURL;
            int count;
            try {
                URLConnection connection = url.openConnection();
                connection.connect();

                int lengthOfFile = connection.getContentLength();

                File destDir = new File(FilesUtilities.getModpackDownloadDir(modpack), "modpack.zip");
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(destDir);

                byte[] data = new byte[1024];

                long percentage = 0;
                while ((count = input.read(data)) != -1) {
                    long total = destDir.length();

                    if (percentage != total * 100 / lengthOfFile) {
                        percentage = total * 100 / lengthOfFile;
                        if (percentage < 98) {
                            utils.setProgress("Downloading modpack.zip ", (int) percentage);
                        }
                    }

                    output.write(data, 0, count);
                }

                output.flush();

                output.close();
                input.close();
            } catch (Throwable e) {
                utils.log(e);
            } finally {
                utils.setDebug(utils.rgbColor("Downloaded modpack.zip", 120, 200, 155), true);

                try {
                    Unzip unzip = new Unzip(new File(FilesUtilities.getModpackDownloadDir(modpack), "modpack.zip"),
                            FilesUtilities.getModpackDownloadDir(modpack), false);

                    Thread thread = new Thread(unzip, "Unzipping");
                    thread.start();

                    Timer installTimer = new Timer();
                    boolean finalHasShaderpacks = hasShaderpacks;
                    boolean finalHasTexturepacks = hasTexturepacks;
                    boolean finalHashConfigs = hasConfigs;
                    installTimer.schedule(new TimerTask() {
                        @SneakyThrows
                        @Override
                        public void run() {
                            if (unzip.isEnded()) {
                                installTimer.cancel();
                                moveContentsToMinecraft("mods", false);
                                if (finalHasShaderpacks) {
                                    moveContentsToMinecraft("shaderpacks", false);
                                }
                                if (finalHasTexturepacks) {
                                    moveContentsToMinecraft("resourcepacks", false);
                                }
                                if (finalHashConfigs) {
                                    moveContentsToMinecraft("config", true);
                                }
                                if (modpack.hasVersion()) {
                                    if (zipVersion) {
                                        modpack.installVersion();
                                    } else {
                                        int totalDownloads = 0;
                                        int success = 0;
                                        int error = 0;
                                        for (String ver : versions) {
                                            URL url = downloads.get(ver);
                                            totalDownloads++;
                                            try {
                                                int count;
                                                try {
                                                    URLConnection conection = url.openConnection();
                                                    conection.connect();

                                                    int lengthOfFile = conection.getContentLength();

                                                    File versionFolder = new File(FilesUtilities.getModpackDownloadDir(modpack) + "/versions/");
                                                    if (!versionFolder.exists()) {
                                                        if (versionFolder.mkdirs()) {
                                                            System.out.println("Executed");
                                                        }
                                                    }

                                                    File destVer = new File(FilesUtilities.getModpackDownloadDir(modpack) + "/versions/", ver);

                                                    InputStream input = new BufferedInputStream(url.openStream(), 8192);
                                                    OutputStream output = new FileOutputStream(destVer);

                                                    byte[] data = new byte[1024];

                                                    long percentage = 0;
                                                    while ((count = input.read(data)) != -1) {
                                                        long total = destVer.length();
                                                        if (percentage != total * 100 / lengthOfFile) {
                                                            percentage = total * 100 / lengthOfFile;
                                                            if (percentage < 98) {
                                                                utils.setProgress("Downloading version file " + ver, (int) percentage);
                                                            }
                                                        }

                                                        output.write(data, 0, count);
                                                    }

                                                    output.flush();
                                                    output.close();
                                                    input.close();
                                                } catch (Exception e) {
                                                    utils.log(e);
                                                }
                                                success++;
                                                green.add("Downloaded version file " + ver);
                                            } catch (Throwable e) {
                                                error++;
                                                red.add("Failed to download version file " + ver);
                                            } finally {
                                                utils.setProgress("Download status bar", 1);

                                                modpack.installVersion();
                                            }

                                            utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
                                            utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());

                                            utils.setDebug(utils.rgbColor("Tried to download a total of " + totalDownloads + " files <span style=\" color: green;\">" + success + "</span> were success and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
                                        }
                                    }
                                }
                            }
                        }
                    }, 0, TimeUnit.SECONDS.toMillis(1));
                } catch (Throwable e) {
                    utils.log(e);
                }
            }
        }
    }

    private boolean copy(File file, File directory) {
        try {
            FileUtils.copyFileToDirectory(file, directory);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @SneakyThrows
    private void moveContentsToMinecraft(String folder, boolean massive) {
        if (!massive) {
            File[] files = new File(FilesUtilities.getModpackDownloadDir(modpack) + "/" + folder).listFiles();
            if (files != null) {
                File destDir = new File(FilesUtilities.getMinecraftDir() + "/" + folder + "/");
                if (!destDir.exists()) {
                    if (destDir.mkdirs()) {
                        System.out.println("Executed");
                    }
                }

                ArrayList<String> green = new ArrayList<>();
                ArrayList<String> red = new ArrayList<>();
                int total = 0;
                int success = 0;
                int error = 0;
                for (File file : files) {
                    total++;

                    if (copy(file, destDir)) {
                        success++;
                        green.add("Moved file " + file.getName() + " to " + FilesUtilities.getPath(destDir));
                    } else {
                        error++;
                        red.add("Couldn't move file " + file.getName() + " to " + FilesUtilities.getPath(destDir));
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());
                }
                utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " files <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
            } else {
                utils.setDebug(utils.rgbColor("Something was wrong with directory: " + FilesUtilities.getModpackDownloadDir(modpack) + "/" + folder, 220, 100, 100), true);
            }
        } else {
            File inPackDir = new File(FilesUtilities.getModpackDownloadDir(modpack) + "/" + folder);
            File mcDir = new File(FilesUtilities.getMinecraftDir());

            utils.setDebug(utils.rgbColor("Starting a massive folder import from " + FilesUtilities.getPath(inPackDir) + " to " + FilesUtilities.getPath(mcDir), 255, 100, 100), true);
            try {
                FileUtils.copyDirectoryToDirectory(inPackDir, mcDir);
            } catch (Throwable e) {
                utils.log(e);
            }
        }
    }

    @SneakyThrows
    private void moveMods() {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack mods folder...", 155, 240, 175), true);
            File[] mods = FilesUtilities.getModpackMods(modpack).listFiles();
            if (mods != null && !Arrays.asList(mods).isEmpty()) {
                ArrayList<String> green = new ArrayList<>();
                ArrayList<String> red = new ArrayList<>();

                int total = 0;
                int success = 0;
                int error = 0;
                for (File mod : mods) {
                    if (isMod(mod)) {
                        total++;
                        if (mod.delete()) {
                            green.add("Removed old mod " + mod.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackMods(modpack)));
                            success++;
                        } else {
                            red.add("Failed to remove mod " + mod.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                            error++;
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());
                }
                utils.setDebug(utils.rgbColor("Tried to remove a total of " + total + " mods <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
            }
        }
        utils.setDebug(utils.rgbColor("Cleaning minecraft mods folder...", 155, 240, 175), false);
        File[] mods = new File(FilesUtilities.getMinecraftDir() + "/mods").listFiles();
        if (mods != null && !Arrays.asList(mods).isEmpty()) {
            ArrayList<String> green = new ArrayList<>();
            ArrayList<String> red = new ArrayList<>();

            int total = 0;
            int success = 0;
            int error = 0;
            for (File mod : mods) {
                if (isMod(mod)) {
                    total++;
                    if (mod.delete()) {
                        green.add("Removed old mod " + mod.getName() + " from " + FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir())) + "/mods");
                        success++;
                    } else {
                        red.add("Failed to remove mod " + mod.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is minecraft running?</span>");
                        error++;
                    }
                }
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());
            }
            utils.setDebug(utils.rgbColor("Tried to remove a total of " + total + " mods <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
        }
    }

    @SneakyThrows
    private void renameTextures() {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack updater resourcepacks folder...", 155, 240, 175), true);
            File[] texturepacks = FilesUtilities.getModpackTextures(modpack).listFiles();
            if (texturepacks != null && !Arrays.asList(texturepacks).isEmpty()) {
                ArrayList<String> green = new ArrayList<>();
                ArrayList<String> red = new ArrayList<>();

                int total = 0;
                int success = 0;
                int error = 0;
                for (File texturepack : texturepacks) {
                    if (isZip(texturepack)) {
                        total++;
                        if (texturepack.delete()) {
                            green.add("Removed old texturepack " + texturepack.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackTextures(modpack)));
                            success++;
                        } else {
                            red.add("Failed to remove old texturepack " + texturepack.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                            error++;
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());
                }
                utils.setDebug(utils.rgbColor("Tried to remove a total of " + total + " resourcepacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
            }
        }
        utils.setDebug(utils.rgbColor("Cleaning minecraft resourcepacks folder...", 155, 240, 175), true);
        File[] texturepacks = new File(FilesUtilities.getMinecraftDir() + "/resourcepacks").listFiles();
        if (texturepacks != null && !Arrays.asList(texturepacks).isEmpty()) {
            ArrayList<String> green = new ArrayList<>();
            ArrayList<String> red = new ArrayList<>();

            int total = 0;
            int success = 0;
            int error = 0;
            for (File texturepack : texturepacks) {
                if (isZip(texturepack)) {
                    total++;
                    if (texturepack.delete()) {
                        green.add("Removed old texturepack " + texturepack.getName() + " from " + FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir())) + "/resourcepacks");
                        success++;
                    } else {
                        red.add("Failed to remove old texturepack " + texturepack.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is minecraft running?</span>");
                        error++;
                    }
                }
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());
            }
            utils.setDebug(utils.rgbColor("Tried to remove a total of " + total + " resourcepacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
        }
    }

    @SneakyThrows
    private void renameShaders() {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack updater shaderpacks folder...", 155, 240, 175), true);
            File[] shaderpacks = FilesUtilities.getModpackShaders(modpack).listFiles();
            if (shaderpacks != null && !Arrays.asList(shaderpacks).isEmpty()) {
                ArrayList<String> green = new ArrayList<>();
                ArrayList<String> red = new ArrayList<>();

                int total = 0;
                int success = 0;
                int error = 0;
                for (File shaderpack : shaderpacks) {
                    if (isZip(shaderpack)) {
                        total++;
                        if (shaderpack.delete()) {
                            green.add("Removed old shaderpack " + shaderpack.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackShaders(modpack)));
                            success++;
                        } else {
                            red.add("Failed to remove old shaderpack " + shaderpack.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                            error++;
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());
                }
                utils.setDebug(utils.rgbColor("Tried to remove a total of " + total + " shaderpacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
            }
        }
        utils.setDebug(utils.rgbColor("Cleaning minecraft shaderpacks folder...", 155, 240, 175), true);
        File[] shaderpacks = new File(FilesUtilities.getMinecraftDir() + "/shaderpacks").listFiles();
        if (shaderpacks != null && !Arrays.asList(shaderpacks).isEmpty()) {
            ArrayList<String> green = new ArrayList<>();
            ArrayList<String> red = new ArrayList<>();

            int total = 0;
            int success = 0;
            int error = 0;
            for (File shaderpack : shaderpacks) {
                if (isZip(shaderpack)) {
                    total++;
                    if (shaderpack.delete()) {
                        green.add("Removed old shaderpack " + shaderpack.getName() + " from " + FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/shaderpacks")));
                        success++;
                    } else {
                        red.add("Failed to remove old shaderpack " + shaderpack.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is minecraft running?</span>");
                        error++;
                    }
                }
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());
            }
            utils.setDebug(utils.rgbColor("Tried to remove a total of " + total + " shaderpacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
        }
    }

    @SneakyThrows
    private void renameConfigs() {
        {
            utils.setDebug(utils.rgbColor("Cleaning minecraft updater mod configs folder...", 155, 240, 175), true);
            File[] configs = new File(FilesUtilities.getMinecraftDir() + "/config").listFiles();

            if (configs != null) {
                Arrays.asList(configs).forEach(file -> {
                    if (file.delete()) {
                        System.out.println("Executed");
                    }
                });
            }
        }
        utils.setDebug(utils.rgbColor("Cleaning minecraft mod configs folder...", 155, 240, 175), true);

        File[] configs = new File(FilesUtilities.getModpackDownloadDir(modpack) + "/config").listFiles();

        if (configs != null) {
            Arrays.asList(configs).forEach(file -> {
                if (file.delete()) {
                    System.out.println("Executed");
                }
            });
        }
    }

    public static boolean isMod(File jarFile) {
        String extension = FilenameUtils.getExtension(jarFile.getName());
        return extension.equals("jar");
    }

    public static boolean isZip(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        return extension.equals("zip");
    }

    public static boolean isConfig(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        return extension.equals("cfg") || extension.equals("properties");
    }
}
