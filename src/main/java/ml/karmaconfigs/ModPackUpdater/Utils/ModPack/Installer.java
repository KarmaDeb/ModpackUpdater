package ml.karmaconfigs.ModPackUpdater.Utils.ModPack;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.MainFrame;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.SelectiveSelection;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;
import net.lingala.zip4j.core.ZipFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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
        ArrayList<File> packMods = pack.getMods();

        if (!packMods.isEmpty()) {
            int success = 0;
            for (File mod : packMods) {
                if (currentMods.contains(mod)) {
                    success++;
                }
            }

            if (success != packMods.size()) {
                if (fullInstall) {
                    moveMods();
                    try {
                        if (pack.isZip()) {
                            File zFile = new File(FilesUtilities.getModpackDownloadDir(pack), "modpack.zip");

                            if (zFile.exists()) {
                                ZipFile zipFile = new ZipFile(zFile);
                                net.lingala.zip4j.progress.ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

                                zipFile.extractAll(FilesUtilities.getModpackDownloadDir(pack).getPath());
                                utils.setProgress("Unzipping modpack.zip", progressMonitor.getPercentDone());

                                moveContentsToMinecraft("mods");
                                if (pack.hasShaders()) {
                                    renameShaders();
                                    moveContentsToMinecraft("shaderpacks");
                                }
                                if (pack.hasTextures()) {
                                    renameTextures();
                                    moveContentsToMinecraft("resourcepacks");
                                }
                                return "SUCCESS";
                            } else {
                                return "DOWNLOAD_NEED";
                            }
                        } else {
                            moveContentsToMinecraft("mods");
                            return "SUCCESS";
                        }
                    } finally {
                        utils.setProgress("Download status bar", 1);
                    }
                } else {
                    SelectiveSelection already = new SelectiveSelection();
                    SelectiveSelection notIn = new SelectiveSelection();

                    SelectiveSelection alreadyShaders = new SelectiveSelection();
                    SelectiveSelection notShaders = new SelectiveSelection();

                    SelectiveSelection alreadyResources = new SelectiveSelection();
                    SelectiveSelection notResources = new SelectiveSelection();
                    
                    for (File mod : packMods) {
                        if (currentMods.contains(mod)) {
                            already.addSelectedFile(mod);
                        } else {
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
                            if (mcShaders.contains(shader)) {
                                alreadyShaders.addSelectedFile(shader);
                            } else {
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
                            if (mcResources.contains(shader)) {
                                alreadyResources.addSelectedFile(shader);
                            } else {
                                notResources.addSelectedFile(shader);
                            }
                        }
                    }
                    
                    moveMods(already);
                    try {
                        if (pack.isZip()) {
                            File zFile = new File(FilesUtilities.getModpackDownloadDir(pack), "modpack.zip");

                            if (zFile.exists()) {
                                ZipFile zipFile = new ZipFile(zFile);
                                net.lingala.zip4j.progress.ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

                                zipFile.extractAll(FilesUtilities.getModpackDownloadDir(pack).getPath());
                                utils.setProgress("Unzipping modpack.zip", progressMonitor.getPercentDone());

                                moveContentsToMinecraft("mods", notIn);
                                if (pack.hasShaders()) {
                                    renameShaders(alreadyShaders);
                                    moveContentsToMinecraft("shaderpacks", notShaders);
                                }
                                if (pack.hasTextures()) {
                                    renameTextures(alreadyResources);
                                    moveContentsToMinecraft("resourcepacks", notResources);
                                }
                                return "SUCCESS";
                            } else {
                                return "DOWNLOAD_NEED";
                            }
                        } else {
                            moveContentsToMinecraft("mods", notIn);
                            return "SUCCESS";
                        }
                    } finally {
                        utils.setProgress("Download status bar", 1);
                    }
                }
            } else {
                return "ALREADY_INSTALLED";
            }
        } else {
            return "EMPTY";
        }
    }

    @SneakyThrows
    private void moveContentsToMinecraft(String folder) {
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

                if (file.renameTo(destFile)) {
                    success++;
                    green.add("Moved file " + file.getName() + " to " + FilesUtilities.getPath(destDir));
                } else {
                    error++;
                    red.add("Couldn't move file " + file.getName() + " to " + FilesUtilities.getPath(destDir));
                }
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " files <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
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
                    if (file.renameTo(destFile)) {
                        success++;
                        green.add("Moved file " + file.getName() + " to " + FilesUtilities.getPath(destDir));
                    } else {
                        error++;
                        red.add("Couldn't move file " + file.getName() + " to " + FilesUtilities.getPath(destDir));
                    }
                }
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " files <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
        }
    }

    @SneakyThrows
    private void moveMods() {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack mods folder...", 155, 240, 175), false);
            File[] mods = FilesUtilities.getModpackMods(pack).listFiles();
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
                            green.add("Removed old mod " + mod.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackMods(pack)));
                            success++;
                        } else {
                            red.add("Failed to move mod " + mod.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                            error++;
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
                }
                utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " mods <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
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
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " mods <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
        }
    }

    private void moveMods(SelectiveSelection ignore) {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack mods folder...", 155, 240, 175), false);
            File[] mods = FilesUtilities.getModpackMods(pack).listFiles();
            if (mods != null && !Arrays.asList(mods).isEmpty()) {
                ArrayList<String> green = new ArrayList<>();
                ArrayList<String> red = new ArrayList<>();

                int total = 0;
                int success = 0;
                int error = 0;
                for (File mod : mods) {
                    if (!ignore.isSelectiveFile(mod)) {
                        if (Downloader.isMod(mod)) {
                            total++;
                            if (mod.delete()) {
                                green.add("Removed old mod " + mod.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackMods(pack)));
                                success++;
                            } else {
                                red.add("Failed to move mod " + mod.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                                error++;
                            }
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
                }
                utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " mods <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
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
                if (!ignore.isSelectiveFile(mod)) {
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
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " mods <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
        }
    }
    
    @SneakyThrows
    private void renameTextures() {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack updater resourcepacks folder...", 155, 240, 175), false);
            File[] texturepacks = FilesUtilities.getModpackTextures(pack).listFiles();
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
                            green.add("Removed old texturepack " + texturepack.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackTextures(pack)));
                            success++;
                        } else {
                            red.add("Failed to remove old texturepack " + texturepack.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                            error++;
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
                }
                utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " resourcepacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
            }
        }
        utils.setDebug(utils.rgbColor("Cleaning minecraft resourcepacks folder...", 155, 240, 175), false);
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
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " resourcepacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
        }
    }

    @SneakyThrows
    private void renameTextures(SelectiveSelection ignore) {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack updater resourcepacks folder...", 155, 240, 175), false);
            File[] texturepacks = FilesUtilities.getModpackTextures(pack).listFiles();
            if (texturepacks != null && !Arrays.asList(texturepacks).isEmpty()) {
                ArrayList<String> green = new ArrayList<>();
                ArrayList<String> red = new ArrayList<>();

                int total = 0;
                int success = 0;
                int error = 0;
                for (File texturepack : texturepacks) {
                    if (!ignore.isSelectiveFile(texturepack)) {
                        if (Downloader.isZip(texturepack)) {
                            total++;
                            if (texturepack.delete()) {
                                green.add("Removed old texturepack " + texturepack.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackTextures(pack)));
                                success++;
                            } else {
                                red.add("Failed to remove old texturepack " + texturepack.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                                error++;
                            }
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
                }
                utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " resourcepacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
            }
        }
        utils.setDebug(utils.rgbColor("Cleaning minecraft resourcepacks folder...", 155, 240, 175), false);
        File[] texturepacks = new File(FilesUtilities.getMinecraftDir() + "/resourcepacks").listFiles();
        if (texturepacks != null && !Arrays.asList(texturepacks).isEmpty()) {
            ArrayList<String> green = new ArrayList<>();
            ArrayList<String> red = new ArrayList<>();

            int total = 0;
            int success = 0;
            int error = 0;
            for (File texturepack : texturepacks) {
                if (!ignore.isSelectiveFile(texturepack)) {
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
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " resourcepacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
        }
    }

    @SneakyThrows
    private void renameShaders() {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack updater shaderpacks folder...", 155, 240, 175), false);
            File[] shaderpacks = FilesUtilities.getModpackShaders(pack).listFiles();
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
                            green.add("Removed old shaderpack " + shaderpack.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackShaders(pack)));
                            success++;
                        } else {
                            red.add("Failed to remove old shaderpack " + shaderpack.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                            error++;
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
                }
                utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " shaderpacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
            }
        }
        utils.setDebug(utils.rgbColor("Cleaning minecraft shaderpacks folder...", 155, 240, 175), false);
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
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " shaderpacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
        }
    }

    @SneakyThrows
    private void renameShaders(SelectiveSelection ignore) {
        {
            utils.setDebug(utils.rgbColor("Cleaning modpack updater shaderpacks folder...", 155, 240, 175), false);
            File[] shaderpacks = FilesUtilities.getModpackShaders(pack).listFiles();
            if (shaderpacks != null && !Arrays.asList(shaderpacks).isEmpty()) {
                ArrayList<String> green = new ArrayList<>();
                ArrayList<String> red = new ArrayList<>();

                int total = 0;
                int success = 0;
                int error = 0;
                for (File shaderpack : shaderpacks) {
                    if (!ignore.isSelectiveFile(shaderpack)) {
                        if (Downloader.isZip(shaderpack)) {
                            total++;
                            if (shaderpack.delete()) {
                                green.add("Removed old shaderpack " + shaderpack.getName() + " from " + FilesUtilities.getPath(FilesUtilities.getModpackShaders(pack)));
                                success++;
                            } else {
                                red.add("Failed to remove old shaderpack " + shaderpack.getName() + " <span style=\"color rgb(100, 100, 255);\"> Is the modpack updater using the folder?</span>");
                                error++;
                            }
                        }
                    }
                }

                if (!green.isEmpty()) {
                    utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
                }
                if (!red.isEmpty()) {
                    utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
                }
                utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " shaderpacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
            }
        }
        utils.setDebug(utils.rgbColor("Cleaning minecraft shaderpacks folder...", 155, 240, 175), false);
        File[] shaderpacks = new File(FilesUtilities.getMinecraftDir() + "/shaderpacks").listFiles();
        if (shaderpacks != null && !Arrays.asList(shaderpacks).isEmpty()) {
            ArrayList<String> green = new ArrayList<>();
            ArrayList<String> red = new ArrayList<>();

            int total = 0;
            int success = 0;
            int error = 0;
            for (File shaderpack : shaderpacks) {
                if (!ignore.isSelectiveFile(shaderpack)) {
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
            }

            if (!green.isEmpty()) {
                utils.setDebug(utils.rgbColor(green, 155, 240, 175), false);
            }
            if (!red.isEmpty()) {
                utils.setDebug(utils.rgbColor(red, 220, 100, 100), false);
            }
            utils.setDebug(utils.rgbColor("Tried to move a total of " + total + " shaderpacks <span style=\" color: green;\">" + success + "</span> moved and <span style=\" color: red;\">" + error + "</span> failed", 255, 100, 100), false);
        }
    }
    
    private ArrayList<File> getMods() {
        ArrayList<File> mods = new ArrayList<>();

        File[] currentMods = new File(MainFrame.mcFolder + "/mods").listFiles();
        if (currentMods != null && !Arrays.asList(currentMods).isEmpty()) {
            for (File mod : currentMods) {
                if (Downloader.isMod(mod)) {
                    mods.add(mod);
                }
            }
        }

        return mods;
    }
}
