package ml.karmaconfigs.ModPackUpdater.Utils.Files;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class CopyFile implements Runnable {

    private static File version;
    private static Modpack modpack;
    private static boolean textures, shaders, configs;

    private final boolean debug;
    private boolean finished = false;

    private final Utils utils = new Utils();

    public CopyFile(File loaderVersion, Modpack modpack, boolean textures, boolean shaders, boolean configs, boolean debug) {
        CopyFile.version = loaderVersion;
        CopyFile.modpack = modpack;
        CopyFile.textures = textures;
        CopyFile.configs = configs;
        CopyFile.shaders = shaders;
        this.debug = debug;
    }

    @SneakyThrows
    @Override
    public void run() {
        File destZipFile = new File(FilesUtilities.getModpackUploadDir(modpack), "modpack.zip");
        File srcMods = new File(FilesUtilities.getModpackUploadDir(modpack), "/mods");
        File srcTextures = new File(FilesUtilities.getModpackUploadDir(modpack), "/resourcepacks");
        File srcShaders = new File(FilesUtilities.getModpackUploadDir(modpack), "/shaderpacks");
        File srcConfigs = new File(FilesUtilities.getModpackUploadDir(modpack), "/config");
        boolean hasVersion = version != null;
        File srcVersions = null;
        if (hasVersion) {
            srcVersions = new File(FilesUtilities.getModpackUploadDir(modpack), "/versions");
        }

        int modsAmount = 0;
        int texturesAmount = 0;
        int shadersAmount = 0;
        int versionsAmount = 0;
        int configsAmounts = 0;

        if (srcMods.listFiles() != null) {
            modsAmount = srcMods.listFiles().length;
        }
        if (srcTextures.listFiles() != null) {
            texturesAmount = srcTextures.listFiles().length;
        }
        if (srcShaders.listFiles() != null) {
            shadersAmount = srcShaders.listFiles().length;
        }
        if (srcConfigs.listFiles() != null) {
            configsAmounts = srcConfigs.listFiles().length;
        }
        if (hasVersion) {
            if (srcVersions.listFiles() != null) {
                versionsAmount = srcVersions.listFiles().length;
            }
        }

        int totalAmount = modsAmount + texturesAmount + shadersAmount + configsAmounts + versionsAmount;
        int zippedAmount = 0;

        try {
            OutputStream zipFile = new FileOutputStream(destZipFile);
            ZipOutputStream zip = new ZipOutputStream(zipFile);

            File[] mods = srcMods.listFiles();
            if (mods != null) {
                for (File fileName : mods) {
                    zippedAmount++;
                    byte[] buf = new byte[1024];
                    int len;
                    try (FileInputStream in = new FileInputStream(fileName)) {
                        String name = fileName.getName();
                        utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), zippedAmount == 1);
                        zip.putNextEntry(new ZipEntry("mods/" + name));
                        while ((len = in.read(buf)) > 0) {
                            zip.write(buf, 0, len);
                        }
                    }

                    int percentage = zippedAmount * 100 / totalAmount;
                    utils.setProgress("Zipping files...", percentage);
                }
            }
            if (textures) {
                File[] textures = srcTextures.listFiles();
                if (textures != null) {
                    for (File fileName : textures) {
                        zippedAmount++;
                        byte[] buf = new byte[1024];
                        int len;
                        try (FileInputStream in = new FileInputStream(fileName)) {
                            String name = fileName.getName();
                            utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), zippedAmount == 1);
                            zip.putNextEntry(new ZipEntry("resourcepacks/" + name));
                            while ((len = in.read(buf)) > 0) {
                                zip.write(buf, 0, len);
                            }
                        }

                        int percentage = zippedAmount * 100 / totalAmount;
                        utils.setProgress("Zipping files...", percentage);
                    }
                }
            }
            if (shaders) {
                File[] shaders = srcShaders.listFiles();
                if (shaders != null) {
                    for (File fileName : shaders) {
                        zippedAmount++;
                        byte[] buf = new byte[1024];
                        int len;
                        try (FileInputStream in = new FileInputStream(fileName)) {
                            String name = fileName.getName();
                            utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), zippedAmount == 1);
                            zip.putNextEntry(new ZipEntry("shaderpacks/" + name));
                            while ((len = in.read(buf)) > 0) {
                                zip.write(buf, 0, len);
                            }
                        }

                        int percentage = zippedAmount * 100 / totalAmount;
                        utils.setProgress("Zipping files...", percentage);
                    }
                }
            }
            if (configs) {
                File[] configs = srcConfigs.listFiles();
                if (configs != null) {
                    for (File fileName : configs) {
                        if (!fileName.isDirectory()) {
                            zippedAmount++;

                            byte[] buf = new byte[1024];
                            int len;
                            try (FileInputStream in = new FileInputStream(fileName)) {
                                String name = fileName.getName();
                                utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), zippedAmount == 1);
                                zip.putNextEntry(new ZipEntry("config/" + name));
                                while ((len = in.read(buf)) > 0) {
                                    zip.write(buf, 0, len);
                                }
                            }

                            int percentage = zippedAmount * 100 / totalAmount;
                            utils.setProgress("Zipping files...", percentage);
                        } else {
                            zipConfigFolder(zippedAmount, totalAmount, zip, fileName);
                        }
                    }
                }
            }
            if (hasVersion) {
                File[] versionData = version.listFiles();
                if (versionData != null) {
                    for (File fileName : versionData) {
                        zippedAmount++;
                        byte[] buf = new byte[1024];
                        int len;
                        try (FileInputStream in = new FileInputStream(fileName)) {
                            String name = fileName.getName();
                            utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), zippedAmount == 1);
                            zip.putNextEntry(new ZipEntry("versions/" + name));
                            while ((len = in.read(buf)) > 0) {
                                zip.write(buf, 0, len);
                            }
                        }

                        int percentage = zippedAmount * 100 / totalAmount;
                        utils.setProgress("Zipping files...", percentage);
                    }
                }
            }

            zip.closeEntry();
            zip.close();
            zipFile.close();
        } catch (Throwable e) {
            utils.log(e);
        } finally {
            utils.setProgress("Download bar status", 1);
            utils.setDebug(utils.rgbColor("Modpack zipped, click on \"Open modpack files dir\" to get the download files you have to put in your host", 155, 240, 175), true);
            if (debug) {
                utils.setDebug(utils.rgbColor("Performing an unzip debug...", 155, 240, 175), true);

                Unzip unzip = new Unzip(new File(FilesUtilities.getModpackUploadDir(modpack), "modpack.zip"),
                        FilesUtilities.getModpackDownloadDir(modpack), true);

                Thread thread = new Thread(unzip, "Unzipping");
                thread.start();
            }

            finished = true;
        }
    }

    public final boolean isFinished() {
        return finished;
    }

    /**
     * Zip folder main content
     *
     * @param zippedAmount the amount of files zipped
     * @param totalAmount the amount of files that should be zipped
     * @param zip the zip file
     * @param mainFolder the main folder to read from
     */
    private void zipConfigFolder(int zippedAmount, int totalAmount, ZipOutputStream zip, File mainFolder) throws Throwable {
        File[] files = mainFolder.listFiles();
        if (files != null) {
            for (File fileName : files) {
                if (!fileName.isDirectory()) {
                    zippedAmount++;
                    byte[] buf = new byte[1024];
                    int len;
                    try (FileInputStream in = new FileInputStream(fileName)) {
                        String name = fileName.getName();
                        utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), zippedAmount == 1);
                        zip.putNextEntry(new ZipEntry("config/" + FilesUtilities.constructFolder("config", mainFolder) + "/" + name));
                        while ((len = in.read(buf)) > 0) {
                            zip.write(buf, 0, len);
                        }
                    }

                    int percentage = zippedAmount * 100 / totalAmount;
                    utils.setProgress("Zipping files...", percentage);
                } else {
                    zipConfigFolder(zippedAmount, totalAmount, zip, fileName);
                }
            }
        }
    }
}
