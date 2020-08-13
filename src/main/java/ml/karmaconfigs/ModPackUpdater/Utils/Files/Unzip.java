package ml.karmaconfigs.ModPackUpdater.Utils.Files;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Unzip implements Runnable {

    private final ArrayList<File> zips = new ArrayList<>();
    private final File destDir;

    private final boolean debug;

    private boolean ended = false;

    private final static Utils utils = new Utils();

    public Unzip(File zip, File dest, boolean debugging) {
        zips.add(zip);
        destDir = dest;
        debug = debugging;
    }

    public Unzip(ArrayList<File> files, File dest, boolean debugging) {
        zips.addAll(files);
        destDir = dest;
        debug = debugging;
    }

    private int getContents(File zipFile) throws Throwable {
        utils.setDebug(utils.rgbColor("Please wait until we try to count zip contents...", 125, 255, 195), true);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        ZipEntry entry;

        int amount = 0;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                amount++;
            }
        }

        return amount;
    }

    @SneakyThrows
    @Override
    public void run() {
        ArrayList<File> files = new ArrayList<>();
        int zipped = 0;
        try {
            for (File zipFile : zips) {
                ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile));
                ZipEntry entry;

                int amount = getContents(zipFile);
                int unzipped = 0;
                while ((entry = zip.getNextEntry()) != null) {
                    File file = new File(destDir, entry.getName());
                    files.add(file);

                    if (file.toPath().normalize().startsWith(destDir.toPath())) {
                        unzipped++;
                        if (entry.isDirectory()) {
                            if (file.mkdirs()) {
                                utils.setDebug(utils.rgbColor("Unzipping folder " + entry.getName() + " to " + FilesUtilities.getPath(file), 125, 255, 195), unzipped == 1);
                            } else {
                                continue;
                            }
                        } else {
                            File path = new File(file.getPath().replace(file.getName(), ""));
                            if (!path.exists() && path.mkdirs()) {
                                System.out.println("Executed");
                            }

                            utils.setDebug(utils.rgbColor("Unzipping file " + entry.getName() + " to " + FilesUtilities.getPath(file), 125, 255, 195), unzipped == 1);

                            byte[] buffer = new byte[2048];

                            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                            int count;

                            while ((count = zip.read(buffer)) != -1) {
                                out.write(buffer, 0, count);
                            }
                            out.close();
                        }
                    }

                    int percentage = unzipped * 100 / amount;
                    utils.setProgress("Unzipping files...", percentage);
                }
                zip.closeEntry();
                zip.close();
                zipped++;
            }
        } catch (Throwable e) {
            utils.log(e);
        } finally {
            utils.setProgress("Download bar status", 1);

            if (debug) {
                int amount = files.size();
                for (File file : files) {
                    try {
                        if (file.delete()) {
                            amount--;
                        }
                    } catch (Throwable ex) {
                        utils.log(ex);
                    }
                }

                if (amount == 0) {
                    utils.setDebug(utils.rgbColor("Unzip debug complete", 155, 240, 175), true);
                } else {
                    utils.setDebug(utils.rgbColor("Unzip debug complete, but wasn't able to remove debug data", 155, 240, 175), true);
                }
            }

            ended = zipped == zips.size();
        }
    }

    public final boolean isEnded() {
        return ended;
    }
}
