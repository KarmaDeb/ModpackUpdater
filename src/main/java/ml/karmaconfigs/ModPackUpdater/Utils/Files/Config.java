package ml.karmaconfigs.modpackupdater.utils.files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public final class Config {

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
                    } else {
                        alreadySet = true;
                        sets.add(path + ": " + value);
                    }
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
            } catch (Throwable ignored) {
            }
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
            } catch (Throwable ignored) {
            }
        }
        return val;
    }

    public final void setRunningId(String id) {
        write("RUNTIME", id);
    }

    public final void saveTheme(String theme) {
        //System.out.printInt("Saved theme");
        write("THEME", theme);
    }

    public final void saveMinecraftDir(File dir) {
        //System.out.printInt("Saved mc dir");
        write("MINECRAFT_DIR", FilesUtilities.getPath(dir));
    }

    public final void saveCreatorURL(String url) {
        //System.out.printInt("Saved creator url");
        write("CREATOR_URL", url);
    }

    public final void saveCreatorName(String name) {
        //System.out.printInt("Saved creator name");
        write("CREATOR_NAME", name);
    }

    public final void saveDownloadURL(String url) {
        //System.out.printInt("Saved download url");
        write("DOWNLOAD_URL", url);
    }

    public final void saveCreatorOptions(boolean asZip, boolean textures, boolean shaders, boolean debug) {
        //System.out.printInt("Saved creator config");
        write("CREATOR_ZIP", String.valueOf(asZip));
        write("CREATOR_TEXTURES", String.valueOf(textures));
        write("CREATOR_SHADERS", String.valueOf(shaders));
        write("CREATOR_DEBUG", String.valueOf(debug));
    }

    public final void saveVersionOptions(boolean value) {
        //System.out.printInt("Saved check version config");
        write("CHECK_VERSION", String.valueOf(value));
    }

    public final void saveClientName(String name) {
        //System.out.println("Saved client name: " + name);
        write("CLIENT", name);
    }

    public final void saveClientMem(String mem) {
        //System.out.println("Saved client memory: " + mem);
        write("CLIENT_MEM", mem);
    }

    public final void saveMcDownloadDir(File mcFolder) {
        //System.out.printInt("Saved mc download folder")
        write("DOWNLOAD_DIR", FilesUtilities.getPath(mcFolder));
    }

    public final void saveSkipSelector(final boolean skip) {
        write("SKIP_SELECTOR", String.valueOf(skip));
    }

    public final void saveLaunchType(final boolean isSimple) {
        write("OPEN_SIMPLE", String.valueOf(isSimple));
    }

    public final String getRuntime() {
        return get("RUNTIME", null);
    }

    public final String getTheme() {
        return get("THEME", "Light");
    }

    public final File getMinecraftDir() {
        String path = get("MINECRAFT_DIR", FilesUtilities.getMinecraftDir());

        return new File(path);
    }

    public final File getDownloadDir() {
        String path = get("DOWNLOAD_DIR", FilesUtilities.getMinecraftDir());

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

    public final String getClientName() {
        return get("CLIENT", "Player");
    }

    public final String getClientMemory() {
        return get("CLIENT_MEM", "2048");
    }

    public final String getFakeClientMemory() {
        String mem = getClientMemory();

        int memToInt = Integer.parseInt(mem);
        memToInt = memToInt + 1000;

        return String.valueOf(memToInt);
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
            return false;
        }
    }

    public final boolean checkVersions() {
        String value = get("CHECK_VERSION", "true");

        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else {
            return true;
        }
    }

    public final boolean skipSelector() {
        String value = get("SKIP_SELECTOR", "false");

        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else {
            return false;
        }
    }

    public final boolean openSimple() {
        String value = get("OPEN_SIMPLE", "false");

        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else {
            return false;
        }
    }
}