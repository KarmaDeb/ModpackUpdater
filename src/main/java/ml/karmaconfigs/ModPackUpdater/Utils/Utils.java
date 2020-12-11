package ml.karmaconfigs.modpackupdater.utils;

import ml.karmaconfigs.modpackupdater.files.memory.ClientMemory;
import ml.karmaconfigs.modpackupdater.files.memory.CreatorMemory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

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
        double cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        double cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = cmax;
        if (cmax != 0)
            saturation = (double) (cmax - cmin) / cmax;
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

    static String findPath(final File file) {
        return file.getAbsolutePath().replaceAll("\\\\", "/");
    }

    File getUpdaterDir = new File(getDataFolder(), "MPU");
    File htmlCacheDir = new File(getUpdaterDir, "html_cache");
    File logsDir = new File(getUpdaterDir, "logs");
    File getPacksDir = new File(getUpdaterDir, "modpacks");
    File defaultMC = new File(getDataFolder(), ".minecraft");

    BufferedImage app_ico = new Cache().getIco();

    ClientMemory c_memory = new ClientMemory();
    CreatorMemory cr_memory = new CreatorMemory();
}
