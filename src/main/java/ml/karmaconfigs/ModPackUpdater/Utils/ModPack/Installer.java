package ml.karmaconfigs.ModPackUpdater.Utils.ModPack;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.SelectiveSelection;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.Unzip;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class Installer {

    private final Utils utils = new Utils();
    private static Modpack pack;

    private final boolean fullInstall;
    
    /**
     * Install the specified modpack
     *
     * @param modpack the modpack
     */
    public Installer(Modpack modpack, boolean hardInstall) {
        Installer.pack = modpack;
        fullInstall = hardInstall;
        utils.saveCurrentModpack(modpack);
    }

    /**
     * Install the modpack
     *
     * @return if the modpack was able
     * to be installed
     */
    @SneakyThrows
    public String install() {
        ArrayList<File> currentMods = getMods();
        ArrayList<File> currentInMods = getInMods();

        if (fullInstall) {
            moveMods();
            try {
                if (pack.isZip()) {
                    File zFile = new File(FilesUtilities.getModpackDownloadDir(pack), "modpack.zip");

                    if (zFile.exists()) {
                        Unzip unzip = new Unzip(new File(FilesUtilities.getModpackDownloadDir(pack), "modpack.zip"),
                                FilesUtilities.getModpackDownloadDir(pack), false);

                        Thread thread = new Thread(unzip, "Unzipping");
                        thread.start();

                        Timer installTimer = new Timer();
                        installTimer.schedule(new TimerTask() {
                            @SneakyThrows
                            @Override
                            public void run() {
                                if (unzip.isEnded()) {
                                    installTimer.cancel();
                                    moveContentsToMinecraft("mods", false);
                                    if (pack.hasShaders()) {
                                        renameShaders();
                                        moveContentsToMinecraft("shaderpacks", false);
                                    }
                                    if (pack.hasTextures()) {
                                        renameTextures();
                                        moveContentsToMinecraft("resourcepacks", false);
                                    }
                                    if (pack.hasConfigs()) {
                                        renameConfigs();
                                        moveContentsToMinecraft("config", true);
                                    }
                                    if (pack.hasVersion()) {
                                        pack.installVersion();
                                    }
                                }
                            }
                        }, 0, TimeUnit.SECONDS.toMillis(1));
                        return "SUCCESS";
                    } else {
                        return "DOWNLOAD_NEED";
                    }
                } else {
                    if (pack.hasVersion()) {
                        pack.installVersion();
                    }
                    if (pack.hasConfigs()) {
                        renameConfigs();
                        moveContentsToMinecraft("config", true);
                    }

                    moveContentsToMinecraft("mods", false);
                    return "SUCCESS";
                }
            } finally {
                utils.setProgress("Download status bar", 1);
            }
        } else {
            SelectiveSelection notIn = new SelectiveSelection();

            SelectiveSelection notShaders = new SelectiveSelection();

            SelectiveSelection notResources = new SelectiveSelection();

            for (File mod : currentInMods) {
                if (!currentMods.contains(mod)) {
                    notIn.addSelectedFile(mod);
                }
            }

            File[] packShaders = FilesUtilities.getModpackShaders(pack).listFiles();
            if (packShaders != null && !Arrays.asList(packShaders).isEmpty()) {
                ArrayList<File> mcShaders = new ArrayList<>();
                File[] mcShadersZips = new File(FilesUtilities.getMinecraftDir() + "/shaderpacks").listFiles();
                if (mcShadersZips != null && !Arrays.asList(mcShadersZips).isEmpty()) {
                    for (File shader : mcShadersZips) {
                        if (Downloader.isZip(shader)) {
                            mcShaders.add(shader);
                        }
                    }
                }

                for (File shader : packShaders) {
                    if (!mcShaders.contains(shader)) {
                        notShaders.addSelectedFile(shader);
                    }
                }
            }

            File[] packResources = FilesUtilities.getModpackTextures(pack).listFiles();
            if (packResources != null && !Arrays.asList(packResources).isEmpty()) {
                ArrayList<File> mcResources = new ArrayList<>();
                File[] mcResourcesZips = new File(FilesUtilities.getMinecraftDir() + "/resourcepacks").listFiles();
                if (mcResourcesZips != null && !Arrays.asList(mcResourcesZips).isEmpty()) {
                    for (File shader : mcResourcesZips) {
                        if (Downloader.isZip(shader)) {
                            mcResources.add(shader);
                        }
                    }
                }

                for (File shader : packResources) {
                    if (!mcResources.contains(shader)) {
                        notResources.addSelectedFile(shader);
                    }
                }
            }

            try {
                if (pack.isZip()) {
                    File zFile = new File(FilesUtilities.getModpackDownloadDir(pack), "modpack.zip");

                    if (zFile.exists()) {
                        Unzip unzip = new Unzip(new File(FilesUtilities.getModpackDownloadDir(pack), "modpack.zip")
                                , FilesUtilities.getModpackDownloadDir(pack), false);

                        Thread thread = new Thread(unzip, "Unzipping");
                        thread.start();

                        Timer installTimer = new Timer();
                        installTimer.schedule(new TimerTask() {
                            @SneakyThrows
                            @Override
                            public void run() {
                                if (unzip.isEnded()) {
                                    installTimer.cancel();
                                    moveContentsToMinecraft("mods", notIn);
                                    if (pack.hasShaders()) {
                                        moveContentsToMinecraft("shaderpacks", notShaders);
                                    }
                                    if (pack.hasTextures()) {
                                        moveContentsToMinecraft("resourcepacks", notResources);
                                    }
                                    if (pack.hasConfigs()) {
                                        moveContentsToMinecraft("config", true);
                                    }
                                    if (pack.hasVersion()) {
                                        pack.installVersion();
                                    }
                                }
                            }
                        }, 0, TimeUnit.SECONDS.toMillis(1));
                        return "SUCCESS";
                    } else {
                        return "DOWNLOAD_NEED";
                    }
                } else {
                    if (pack.hasVersion()) {
                        pack.installVersion();
                    }
                    if (pack.hasConfigs()) {
                        moveContentsToMinecraft("config", true);
                    }

                    moveContentsToMinecraft("mods", notIn);
                    return "SUCCESS";
                }
            } finally {
                utils.setProgress("Download status bar", 1);
            }
        }
    }

    private boolean copy(File file, File dest) {
        try {
            FileUtils.copyFileToDirectory(file, dest);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @SneakyThrows
    private void moveContentsToMinecraft(String folder, boolean massive) {
        if (!massive) {
            File[] files = new File(FilesUtilities.getModpackDownloadDir(pack) + "/" + folder).listFiles();
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
            }
        } else {
            File inPackDir = new File(FilesUtilities.getModpackDownloadDir(pack) + "/" + folder);
            File mcDir = new File(FilesUtilities.getMinecraftDir());

            utils.setDebug(utils.rgbColor("Starting a massive folder import from " + FilesUtilities.getPath(inPackDir) + " to " + FilesUtilities.getPath(mcDir), 255, 100, 100), true);
            try {
                FileUtils.copyDirectoryToDirectory(inPackDir, mcDir);
            } catch (Throwable e) {
                utils.log(e);
            }
        }
    }

    private void moveContentsToMinecraft(String folder, SelectiveSelection notIn) {
        File[] files = new File(FilesUtilities.getModpackDownloadDir(pack) + "/" + folder).listFiles();
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
                File destFile = new File(destDir, file.getName());

                if (notIn.isSelectiveFile(destFile)) {
                    if (copy(file, destDir)) {
                        success++;
                        green.add("Moved file " + file.getName() + " to " + FilesUtilities.getPath(destDir));
                    } else {
                        error++;
                        red.add("Couldn't move file " + file.getName() + " to " + FilesUtilities.getPath(destDir));
                    }
                }
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), true);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), green.isEmpty());
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " files <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
        }
    }

    @SneakyThrows
    private void moveMods() {
        utils.setDebug(utils.rgbColor("Cleaning minecraft mods folder...", 155, 240, 175), true);
        File[] mods = new File(FilesUtilities.getMinecraftDir() + "/mods").listFiles();
        if (mods != null && !Arrays.asList(mods).isEmpty()) {
            ArrayList<String> green = new ArrayList<>();
            ArrayList<String> red = new ArrayList<>();

            int total = 0;
            int success = 0;
            int error = 0;
            for (File mod : mods) {
                if (Downloader.isMod(mod)) {
                    total++;
                    if (mod.delete()) {
                        green.add("Removed old mod " + mod.getName() + " from " + FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/mods")));
                        success++;
                    } else {
                        red.add("Failed to move mod " + mod.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is minecraft running?</span>");
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
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " mods <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
        }
    }
    
    @SneakyThrows
    private void renameTextures() {
        utils.setDebug(utils.rgbColor("Cleaning minecraft resourcepacks folder...", 155, 240, 175), true);
        File[] texturepacks = new File(FilesUtilities.getMinecraftDir() + "/resourcepacks").listFiles();
        if (texturepacks != null && !Arrays.asList(texturepacks).isEmpty()) {
            ArrayList<String> green = new ArrayList<>();
            ArrayList<String> red = new ArrayList<>();

            int total = 0;
            int success = 0;
            int error = 0;
            for (File texturepack : texturepacks) {
                if (Downloader.isZip(texturepack)) {
                    total++;
                    if (texturepack.delete()) {
                        green.add("Removed old texturepack " + texturepack.getName() + " from " + FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/resourcepacks")));
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
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " resourcepacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
        }
    }

    @SneakyThrows
    private void renameShaders() {
        utils.setDebug(utils.rgbColor("Cleaning minecraft shaderpacks folder...", 155, 240, 175), true);
        File[] shaderpacks = new File(FilesUtilities.getMinecraftDir() + "/shaderpacks").listFiles();
        if (shaderpacks != null && !Arrays.asList(shaderpacks).isEmpty()) {
            ArrayList<String> green = new ArrayList<>();
            ArrayList<String> red = new ArrayList<>();

            int total = 0;
            int success = 0;
            int error = 0;
            for (File shaderpack : shaderpacks) {
                if (Downloader.isZip(shaderpack)) {
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
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " shaderpacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), true);
        }
    }

    @SneakyThrows
    private void renameConfigs() {
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
    
    private ArrayList<File> getMods() {
        ArrayList<File> mods = new ArrayList<>();

        File[] currentMods = new File(FilesUtilities.getMinecraftDir() + "/mods").listFiles();
        if (currentMods != null && !Arrays.asList(currentMods).isEmpty()) {
            for (File mod : currentMods) {
                if (Downloader.isMod(mod)) {
                    mods.add(mod);
                }
            }
        }

        return mods;
    }

    private ArrayList<File> getInMods() {
        ArrayList<File> mods = new ArrayList<>();

        File[] currentMods = new File(FilesUtilities.getModpackDownloadDir(pack) + "/mods").listFiles();
        if (currentMods != null && !Arrays.asList(currentMods).isEmpty()) {
            for (File mod : currentMods) {
                if (Downloader.isMod(mod)) {
                    mods.add(mod);
                }
            }
        }

        ArrayList<File> staticMods = new ArrayList<>();
        for (File mod : mods) {
            File staticMod = new File(FilesUtilities.getMinecraftDir() + "/mods", mod.getName());

            staticMods.add(staticMod);
        }

        return staticMods;
    }
}
