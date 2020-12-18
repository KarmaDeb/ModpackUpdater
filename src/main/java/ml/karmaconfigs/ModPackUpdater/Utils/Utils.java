package ml.karmaconfigs.modpackupdater.utils;

import ml.karmaconfigs.modpackupdater.Updater;
import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.files.memory.ClientMemory;
import ml.karmaconfigs.modpackupdater.files.memory.CreatorMemory;
import ml.karmaconfigs.modpackupdater.files.memory.LauncherMemory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public interface Utils {

    static void displayToolTip(final Component component) {
        final ToolTipManager ttm = ToolTipManager.sharedInstance();
        final MouseEvent event = new MouseEvent(component, 0, 0, 0,
                0, 0,
                0, false);
        ttm.mouseMoved(event);
        ttm.mouseMoved(null);

        ToolTipManager.sharedInstance().setInitialDelay(750);
    }

    static void toCenter(final Component component) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        Point center = new Point(screen.width / 2 - component.getSize().width / 2, screen.height / 2 - component.getSize().height / 2);
        component.setLocation(center);
    }

    static void downloadJava() {
        new AsyncScheduler(() -> {
            String java_ulr = "https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/ModpackUpdater/java.zip";
            File dest_file = new File(getUpdaterDir, "java.zip");
            if (!javaDir.exists() || javaDir.length() != 4096) {
                Cache cache = new Cache();
                cache.setDownloadingJava(true);
                try {
                    URL url = new URL(java_ulr);

                    if (!dest_file.exists() || url.openConnection().getContentLengthLong() != dest_file.length()) {
                        Debug.util.add(Text.util.create("Downloading java for minecraft launching purposes, please wait...", Color.LIGHTGREEN, 12), true);

                        BufferedInputStream in = new BufferedInputStream(url.openStream());
                        OutputStream out = new FileOutputStream(dest_file);

                        int size = url.openConnection().getContentLength();

                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        double sumCount = 0.0;
                        long start = System.nanoTime();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (dest_file.length() < size) {
                                    long current = System.nanoTime();
                                    String remaining = getTimeRemaining(size, dest_file.length(), current - start);
                                    tool_bar.setLabel("<html>Downloading:<br>java for minecraft ( " + remaining + " )</html>");
                                } else {
                                    cancel();
                                }
                            }
                        }, 0, TimeUnit.SECONDS.toMillis(1));
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            out.write(dataBuffer, 0, bytesRead);

                            sumCount += bytesRead;
                            if (size > 0) {
                                double percentage_db = (sumCount / size * 100.0);
                                String to_str = String.valueOf(percentage_db);

                                int percentage = Integer.parseInt(to_str.split("\\.")[0]);

                                tool_bar.show(percentage);
                                tool_bar.setLocation(Updater.external.frame);
                            }
                        }

                        in.close();
                        out.close();
                    }
                } catch (Throwable ex) {
                    Text text = new Text(ex);
                    text.format(Color.INDIANRED, 14);

                    Debug.util.add(text, true);
                } finally {
                    if (javaDir.length() != 4096) {
                        Debug.util.add(Text.util.create("Java for minecraft downloaded, unzipping contents...", Color.LIGHTGREEN, 12), true);
                        try {
                            byte[] buffer = new byte[1024];
                            ZipInputStream zis = new ZipInputStream(new FileInputStream(dest_file));
                            ZipEntry zipEntry = zis.getNextEntry();
                            int passed = 0;
                            while (zipEntry != null) {
                                passed++;
                                File newFile = new File(getUpdaterDir, zipEntry.getName());
                                if (zipEntry.isDirectory() && !newFile.exists()) {
                                    Files.createDirectories(newFile.toPath());
                                } else {
                                    File parent = newFile.getParentFile();
                                    if (parent.isDirectory() && !parent.exists()) {
                                        Files.createDirectories(parent.toPath());
                                    }

                                    FileOutputStream fos = new FileOutputStream(newFile);
                                    int len;
                                    while ((len = zis.read(buffer)) > 0) {
                                        fos.write(buffer, 0, len);
                                    }
                                    fos.close();
                                }
                                zipEntry = zis.getNextEntry();

                                double division = (double) passed / 210;
                                long iPart = (long) division;
                                double fPart = division - iPart;

                                double percentage = fPart * 100.0;
                                tool_bar.show(Integer.parseInt(String.valueOf(percentage).split("\\.")[0]));
                                tool_bar.setLocation(Updater.external.frame);
                                if (zipEntry != null)
                                    tool_bar.setLabel("<html>Unzipping:<br>" + zipEntry.getName() + "</html>");
                            }

                            zis.closeEntry();
                            zis.close();
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        } finally {
                            try {
                                Files.delete(dest_file.toPath());
                            } catch (Throwable ex) {
                                dest_file.deleteOnExit();
                            }
                            Debug.util.add(Text.util.create("Java for minecraft downloaded successfully", Color.LIGHTGREEN, 12), true);
                            cache.setDownloadingJava(false);
                        }
                    } else {
                        if (dest_file.exists()) {
                            try {
                                Files.delete(dest_file.toPath());
                            } catch (Throwable ex) {
                                dest_file.deleteOnExit();
                            }
                        }
                    }
                }
            } else {
                if (dest_file.exists()) {
                    try {
                        Files.delete(dest_file.toPath());
                    } catch (Throwable ex) {
                        dest_file.deleteOnExit();
                    }
                }
            }
        }).run();
    }

    static void downloadBrowserNatives() {
        new AsyncScheduler(() -> {
            String java_ulr = "https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/ModpackUpdater/natives.zip";
            File dest_file = new File(getUpdaterDir, "natives.zip");
            if (!nativesDir.exists() || nativesDir.length() != 4096) {
                Cache cache = new Cache();
                cache.setDownloadingBrowser(true);
                try {
                    URL url = new URL(java_ulr);

                    if (!dest_file.exists() || url.openConnection().getContentLengthLong() != dest_file.length()) {
                        Debug.util.add(Text.util.create("Downloading browser natives, please wait...", Color.LIGHTGREEN, 12), true);

                        BufferedInputStream in = new BufferedInputStream(url.openStream());
                        OutputStream out = new FileOutputStream(dest_file);

                        int size = url.openConnection().getContentLength();

                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        double sumCount = 0.0;
                        long start = System.nanoTime();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (dest_file.length() < size) {
                                    long current = System.nanoTime();
                                    String remaining = getTimeRemaining(size, dest_file.length(), current - start);
                                    tool_bar.setLabel("<html>Downloading:<br>browser natives ( " + remaining + " )</html>");
                                } else {
                                    cancel();
                                }
                            }
                        }, 0, TimeUnit.SECONDS.toMillis(1));
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            out.write(dataBuffer, 0, bytesRead);

                            sumCount += bytesRead;
                            if (size > 0) {
                                double percentage_db = (sumCount / size * 100.0);
                                String to_str = String.valueOf(percentage_db);

                                int percentage = Integer.parseInt(to_str.split("\\.")[0]);

                                tool_bar.show(percentage);
                                tool_bar.setLocation(Updater.external.frame);
                            }
                        }

                        in.close();
                        out.close();
                    }
                } catch (Throwable ex) {
                    Text text = new Text(ex);
                    text.format(Color.INDIANRED, 14);

                    Debug.util.add(text, true);
                } finally {
                    if (nativesDir.length() != 4096) {
                        Debug.util.add(Text.util.create("Browser natives downloaded successfully, unzipping contents...", Color.LIGHTGREEN, 12), true);
                        try {
                            byte[] buffer = new byte[1024];
                            ZipInputStream zis = new ZipInputStream(new FileInputStream(dest_file));
                            ZipEntry zipEntry = zis.getNextEntry();
                            int passed = 0;
                            while (zipEntry != null) {
                                passed++;
                                File newFile = new File(getUpdaterDir, zipEntry.getName());
                                if (zipEntry.isDirectory() && !newFile.exists()) {
                                    Files.createDirectories(newFile.toPath());
                                } else {
                                    File parent = newFile.getParentFile();
                                    if (parent.isDirectory() && !parent.exists()) {
                                        Files.createDirectories(parent.toPath());
                                    }

                                    FileOutputStream fos = new FileOutputStream(newFile);
                                    int len;
                                    while ((len = zis.read(buffer)) > 0) {
                                        fos.write(buffer, 0, len);
                                    }
                                    fos.close();
                                }
                                zipEntry = zis.getNextEntry();

                                double division = (double) passed / 75;
                                long iPart = (long) division;
                                double fPart = division - iPart;

                                double percentage = fPart * 100.0;
                                tool_bar.show(Integer.parseInt(String.valueOf(percentage).split("\\.")[0]));
                                tool_bar.setLocation(Updater.external.frame);
                                if (zipEntry != null)
                                    tool_bar.setLabel("<html>Unzipping:<br>" + zipEntry.getName() + "</html>");
                            }

                            zis.closeEntry();
                            zis.close();
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        } finally {
                            try {
                                Files.delete(dest_file.toPath());
                            } catch (Throwable ex) {
                                dest_file.deleteOnExit();
                            }
                            Debug.util.add(Text.util.create("Browser natives downloaded successfully", Color.LIGHTGREEN, 12), true);
                            cache.setDownloadingBrowser(false);
                        }
                    } else {
                        if (dest_file.exists()) {
                            try {
                                Files.delete(dest_file.toPath());
                            } catch (Throwable ex) {
                                dest_file.deleteOnExit();
                            }
                        }
                    }
                }
            } else {
                if (dest_file.exists()) {
                    try {
                        Files.delete(dest_file.toPath());
                    } catch (Throwable ex) {
                        dest_file.deleteOnExit();
                    }
                }
            }
        }).run();
    }

    static String getTimeRemaining(final long file_size, final long current_size, final long elapsed) {
        long estimated_time;
        try {
            estimated_time = (file_size - current_size) * elapsed/current_size;
        } catch (Throwable ex) {
            estimated_time = elapsed;
        }
        long seconds = TimeUnit.NANOSECONDS.toSeconds(estimated_time);
        long minutes = TimeUnit.NANOSECONDS.toMinutes(estimated_time);
        long hours = TimeUnit.NANOSECONDS.toHours(estimated_time);

        if (seconds <= 59) {
            return seconds + " sec(s) left";
        } else {
            if (minutes <= 59) {
                return minutes + " min(s) and " + Math.abs((minutes * 60) - seconds) + " sec(s) left";
            } else {
                return hours + " h(s) and " + Math.abs((hours * 60) - minutes) + " min(s)";
            }
        }
    }

    static String findArgument(final String[] arguments, final String path, final Object def) {
        final Iterator<String> args = Arrays.stream(arguments).iterator();
        while (args.hasNext()) {
            if (args.next().startsWith(path))
                return args.next().replace(path + "=", "");
        }

        return def.toString();
    }

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

    static double[] HSBtoRGB(double hue, double saturation, double brightness) {
        // normalize the hue
        double normalizedHue = ((hue % 360) + 360) % 360;
        hue = normalizedHue/360;

        double r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = brightness;
        } else {
            double h = (hue - Math.floor(hue)) * 6.0;
            double f = h - java.lang.Math.floor(h);
            double p = brightness * (1.0 - saturation);
            double q = brightness * (1.0 - saturation * f);
            double t = brightness * (1.0 - (saturation * (1.0 - f)));
            switch ((int) h) {
                case 0:
                    r = brightness;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = brightness;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = brightness;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = brightness;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = brightness;
                    break;
                case 5:
                    r = brightness;
                    g = p;
                    b = q;
                    break;
            }
        }
        double[] f = new double[3];
        f[0] = r;
        f[1] = g;
        f[2] = b;
        return f;
    }

    static double[] RGBtoHSB(double r, double g, double b) {
        double hue, saturation, brightness;
        double[] hsbvals = new double[3];
        double cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        double cmin = Math.min(r, g);
        if (b < cmin) cmin = b;

        brightness = cmax;
        if (cmax != 0)
            saturation = (cmax - cmin) / cmax;
        else
            saturation = 0;

        if (saturation == 0) {
            hue = 0;
        } else {
            double redc = (cmax - r) / (cmax - cmin);
            double greenc = (cmax - g) / (cmax - cmin);
            double bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0 + redc - bluec;
            else
                hue = 4.0 + greenc - redc;
            hue = hue / 6.0;
            if (hue < 0)
                hue = hue + 1.0;
        }
        hsbvals[0] = hue * 360;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    static File getDataFolder() {
        if (getOS().equals("Windows")) {
            return new File(System.getenv("APPDATA"));
        }
        if (getOS().equals("Linux")) {
            return new File(System.getProperty("user.home"));
        }
        if (getOS().equals("Mac")) {
            return new File(System.getProperty("user.home") + "/Library/Application Support/");
        }
        return new File("");
    }

    static File getPackDir(final String name) {
        return new File(getPacksDir, name);
    }

    static File getPackMc(final MPUExt modpack) {
        File instances = new File(getUpdaterDir, "instances");
        File mc = new File(instances, "." + modpack.getName());
        try {
            if (!instances.exists())
                Files.createDirectories(instances.toPath());

            if (!mc.exists())
                Files.createDirectories(mc.toPath());
        } catch (Throwable ignored) {}

        return mc;
    }

    static String findPath(final File file) {
        return file.getAbsolutePath().replaceAll("\\\\", "/");
    }

    static ArrayList<MPUExt> getModpacks() {
        ArrayList<MPUExt> modpacks = new ArrayList<>();

        File[] internal_modpacks = getPacksDir.listFiles();
        for (File mpu_folder : internal_modpacks) {
            if (mpu_folder.isDirectory()) {
                File[] contents = mpu_folder.listFiles();
                for (File content : contents) {
                    if (content.getName().endsWith(".mpu")) {
                        try {
                            MPUExt modpack = new MPUExt(content);
                            if (!modpacks.contains(modpack)) {
                                modpacks.add(modpack);
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            } else {
                if (mpu_folder.getName().endsWith(".mpu")) {
                    try {
                        MPUExt modpack = new MPUExt(mpu_folder);
                        if (!modpacks.contains(modpack)) {
                            modpacks.add(modpack);
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }

        return modpacks;
    }

    File getUpdaterDir = new File(getDataFolder(), "MPU");
    File logsDir = new File(getUpdaterDir, "logs");
    File getPacksDir = new File(getUpdaterDir, "modpacks");
    File defaultMC = new File(getDataFolder(), ".minecraft");
    File javaDir = new File(getUpdaterDir, "java");
    File nativesDir = new File(getUpdaterDir, "natives");

    BufferedImage app_ico = new Cache().getIco();

    ClientMemory c_memory = new ClientMemory();
    CreatorMemory cr_memory = new CreatorMemory();
    LauncherMemory l_memory = new LauncherMemory();

    ProgressBar tool_bar = new ProgressBar();
}
