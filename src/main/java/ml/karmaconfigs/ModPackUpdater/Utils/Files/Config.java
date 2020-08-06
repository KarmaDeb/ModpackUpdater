package ml.karmaconfigs.ModPackUpdater.Utils.Files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public final class Config {

    private static String getOS() {
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

    private static File getUpdaterDir() {
        if (getOS().equals("Windows")) {
            return new File(System.getenv("APPDATA") + "/ModPackUpdater");
        }
        if (getOS().equals("Linux")) {
            return new File(System.getProperty("user.home") + "/ModPackUpdater");
        }
        if (getOS().equals("Mac")) {
            return new File(System.getProperty("user.home") + "/Library/Application Support/ModPackUpdater");
        }
        return new File("");
    }

    private final static File cfg = new File(getUpdaterDir() + "/config.mpconfig");

    static {
        if (!getUpdaterDir().exists()) {
            if (getUpdaterDir().mkdirs()) {
                System.out.println("Executed");
            }
        }
        if (!cfg.exists()) {
            try {
                if (cfg.createNewFile()) {
                    System.out.println("Created config file");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void write(String path, String value) {
        InputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader reader = null;
        try {
            in = new FileInputStream(cfg);
            inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            reader = new BufferedReader(inReader);
            ArrayList<String> sets = new ArrayList<>();
            boolean alreadySet = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String currentPath = line.split(":")[0];
                    if (!currentPath.equals(path)) {
                        sets.add(line);
                        continue;
                    }
                    alreadySet = true;
                    sets.add(path + ": " + value);
                }
            }
            if (!alreadySet)
                sets.add(path + ": " + value);
            FileWriter writer = new FileWriter(cfg);
            for (Object str : sets)
                writer.write(str + "\n");
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            e.printStackTrace();
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
            } catch (Throwable ignored) {}
        }
    }

    private String get(String path, String deffault) {
        String val = deffault;
        InputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader reader = null;
        try {
            in = new FileInputStream(cfg);
            inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            reader = new BufferedReader(inReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String actualPath = line.split(":")[0];
                    if (actualPath.equals(path))
                        val = line.replace(actualPath + ": ", "");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
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
            } catch (Throwable ignored) {}
        }
        return val;
    }

    public final void saveTheme(String theme) {
        write("THEME", theme);
    }

    public final void saveMinecraftDir() {
        write("MINECRAFT_DIR", FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir())));
    }

    public final void saveCreatorURL(String url) {
        write("CREATOR_URL", url);
    }

    public final void saveCreatorName(String name) {
        write("CREATOR_NAME", name);
    }

    public final void saveDownloadURL(String url) {
        write("DOWNLOAD_URL", url);
    }

    public final void saveCreatorOptions(boolean asZip, boolean textures, boolean shaders, boolean debug) {
        write("CREATOR_ZIP", String.valueOf(asZip));
        write("CREATOR_TEXTURES", String.valueOf(textures));
        write("CREATOR_SHADERS", String.valueOf(shaders));
        write("CREATOR_DEBUG", String.valueOf(debug));
    }

    public final String getTheme() {
        return get("THEME", "Light");
    }

    public final File getMinecraftDir() {
        String path = get("MINECRAFT_DIR", FilesUtilities.getMinecraftDir());

        return new File(path);
    }

    public final String getCreatorURL() {
        return get("CREATOR_URL", "Host url (where download.txt will be uploaded)");
    }

    public final String getCreatorName() {
        return get("CREATOR_NAME", "Modpack name");
    }

    public final String getDownloadURL() {
        return get("DOWNLOAD_URL", "Modpack download.txt url");
    }

    public final boolean createAsZip() {
        String value = get("CREATOR_ZIP", "false");

        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else {
            return true;
        }
    }

    public final boolean zipTextures() {
        String value = get("CREATOR_TEXTURES", "false");

        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else {
            return false;
        }
    }

    public final boolean zipShaders() {
        String value = get("CREATOR_SHADERS", "false");

        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else {
            return false;
        }
    }

    public final boolean zipDebug() {
        String value = get("CREATOR_DEBUG", "false");

        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else {
            return true;
        }
    }
}