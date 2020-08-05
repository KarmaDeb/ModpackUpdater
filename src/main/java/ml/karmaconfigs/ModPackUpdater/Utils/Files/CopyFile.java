package ml.karmaconfigs.ModPackUpdater.Utils.Files;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class CopyFile implements Runnable {

    private static File file;
    private static Modpack modpack;
    private static boolean textures, shaders;

    private final Utils utils = new Utils();

    public CopyFile(File file, Modpack modpack, boolean textures, boolean shaders) {
        CopyFile.file = file;
        CopyFile.modpack = modpack;
        CopyFile.textures = textures;
        CopyFile.shaders = shaders;
    }

    public void copy(Modpack modpack, String target) {
        File inFolder = new File(FilesUtilities.getModpackUploadDir(modpack) + "/" + target + "/");
        try {
            if (!inFolder.exists()) {
                if (inFolder.mkdirs()) {
                    utils.setDebug(utils.rgbColor("Created in-updater mods folder for zipping", 255, 100, 100), true);
                }
            }
            FileUtils.copyFileToDirectory(file, inFolder);
        } catch (Throwable e) {
            utils.log(e);
        }
    }

    @SneakyThrows
    @Override
    public void run() {
        File destZipFile = new File(FilesUtilities.getModpackUploadDir(modpack), "modpack.zip");
        File srcMods = new File(FilesUtilities.getModpackUploadDir(modpack), "/mods");
        File srcTextures = new File(FilesUtilities.getModpackUploadDir(modpack), "/resourcepacks");
        File srcShaders = new File(FilesUtilities.getModpackUploadDir(modpack), "/shaderpacks");
        try {
            FileOutputStream fileWriter = new FileOutputStream(destZipFile);
            ZipOutputStream zip = new ZipOutputStream(fileWriter);

            File[] mods = srcMods.listFiles();
            if (mods != null) {
                for (File fileName : mods) {
                    byte[] buf = new byte[1024];
                    int len;
                    try (FileInputStream in = new FileInputStream(fileName)) {
                        String name = fileName.getName();
                        utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), false);
                        zip.putNextEntry(new ZipEntry("mods/" + name));
                        while ((len = in.read(buf)) > 0) {
                            zip.write(buf, 0, len);
                        }
                    }
                }
            }
            if (textures) {
                File[] textures = srcTextures.listFiles();
                if (textures != null) {
                    for (File fileName : textures) {
                        byte[] buf = new byte[1024];
                        int len;
                        try (FileInputStream in = new FileInputStream(fileName)) {
                            String name = fileName.getName();
                            utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), false);
                            zip.putNextEntry(new ZipEntry("resourcepacks/" + name));
                            while ((len = in.read(buf)) > 0) {
                                zip.write(buf, 0, len);
                            }
                        }
                    }
                }
            }
            if (shaders) {
                File[] shaders = srcShaders.listFiles();
                if (shaders != null) {
                    for (File fileName : shaders) {
                        byte[] buf = new byte[1024];
                        int len;
                        try (FileInputStream in = new FileInputStream(fileName)) {
                            String name = fileName.getName();
                            utils.setDebug(utils.rgbColor("Zipping file " + FilesUtilities.getPath(fileName), 125, 255, 195), false);
                            zip.putNextEntry(new ZipEntry("shaderpacks/" + name));
                            while ((len = in.read(buf)) > 0) {
                                zip.write(buf, 0, len);
                            }
                        }
                    }
                }
            }
        } finally {
            utils.setDebug(utils.rgbColor("Modpack zipped, click on \"Open modpack files dir\" to get the download files you have to put in your host", 155, 240, 175), true);
        }
    }
}
