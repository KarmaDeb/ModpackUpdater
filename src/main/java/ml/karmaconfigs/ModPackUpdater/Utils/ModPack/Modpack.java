package ml.karmaconfigs.modpackupdater.utils.modpack;

import ml.karmaconfigs.modpackupdater.utils.Utils;
import ml.karmaconfigs.modpackupdater.utils.files.CustomFile;
import ml.karmaconfigs.modpackupdater.utils.files.FilesUtilities;
import ml.karmaconfigs.modpackupdater.utils.launcher.Profiler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Modpack {

    private final CustomFile file;
    private final String modpack;

    /**
     * Initialize the modpack class
     *
     * @param name the modpack name
     */
    public Modpack(String name) {
        modpack = name;
        file = new CustomFile(new File(FilesUtilities.getUpdaterDir() + "/modpacks/" + name + ".txt"), false);
    }

    /**
     * Get the modpack name
     *
     * @return a String
     */
    public final String getName() {
        return file.getString("NAME", "Null name");
    }

    /**
     * Get the modpack download url
     *
     * @return a String
     */
    public final String getDownloadURL() {
        return file.getString("DOWNLOAD", "Null url");
    }

    /**
     * Get the modpack version name
     *
     * @return a string
     */
    public final String getVersionName() {
        return file.getString("VERSION", "NULL_VERSION");
    }

    /**
     * Get the real version
     *
     * @return a String
     */
    public final String getRealVersion() {
        return file.getString("INHERIT", "NULL_VERSION");
    }

    private boolean copy(File file, File dest) {
        try {
            FileUtils.copyFileToDirectory(file, dest);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Install the modpack version
     */
    public final void installVersion() {
        Utils utils = new Utils();
        File[] versionFiles = new File(FilesUtilities.getModpackDownloadDir(this) + "/versions").listFiles();

        File destDirVersion = new File(FilesUtilities.getMinecraftDir() + "/versions/" + getVersionName());
        if (!destDirVersion.exists()) {
            if (destDirVersion.mkdirs()) {
                System.out.println("Executed");
            }
        }

        if (versionFiles != null) {
            int moved = 0;
            for (File vFile : versionFiles) {
                moved++;
                File destDir = new File(FilesUtilities.getMinecraftDir() + "/versions/" + getVersionName());
                File destFile = new File(FilesUtilities.getMinecraftDir() + "/versions/" + getVersionName() + "/" + vFile.getName());
                if (destFile.exists()) {
                    if (destFile.delete()) {
                        utils.setDebug(utils.rgbColor("Removed old version file " + FilesUtilities.getPath(destFile), 155, 240, 175), moved == 1);
                    }
                }
                if (copy(vFile, destDir)) {
                    utils.setDebug(utils.rgbColor("Moved version file " + vFile.getName() + " to " + FilesUtilities.getPath(destDir), 155, 240, 175), moved == 1);
                } else {
                    utils.setDebug(utils.rgbColor("Failed to move version file " + vFile.getName() + " to " + FilesUtilities.getPath(destDir), 220, 100, 100), moved == 1);
                }
            }

            Profiler new_profile = new Profiler(this);
            new_profile.insert();
        }
    }

    /**
     * Check if the modpack has a version
     * <p>
     * --- NOTE: This allows legacy modpack updater
     * configs ---
     *
     * @return a boolean
     */
    public final boolean hasVersion() {
        return !getVersionName().equals("NULL");
    }

    /**
     * Check if the modpack is a zip
     * modpack downloader
     *
     * @return a boolean
     */
    public final boolean isZip() {
        return !file.getString("URL", "").isEmpty();
    }

    /**
     * Check if the modpack has shaders
     * to install
     *
     * @return a boolean
     */
    public final boolean hasShaders() {
        return file.getBoolean("SHADERS", false);
    }

    /**
     * Check if the modpack has textures
     * to install
     *
     * @return a boolean
     */
    public final boolean hasTextures() {
        return file.getBoolean("TEXTURES", false);
    }

    public final boolean hasConfigs() {
        return file.getBoolean("CONFIGS", false);
    }

    /**
     * Check if the modpack exists
     *
     * @return a boolean
     */
    public final boolean exists() {
        return new File(FilesUtilities.getUpdaterDir() + "/modpacks/" + modpack + ".txt").exists();
    }

    public final ArrayList<File> getMods() {
        ArrayList<File> mods = new ArrayList<>();

        List<Object> modNames = file.getList("MODS");
        for (Object modName : modNames) {
            File modFile = new File(FilesUtilities.getMinecraftDir() + "/mods/" + modName);

            mods.add(modFile);
        }

        return mods;
    }

    /**
     * Create the modpack file
     */
    public final void createFile() throws IOException {
        File mpFile = new File(FilesUtilities.getUpdaterDir() + "/modpacks/" + modpack + ".txt");
        if (!mpFile.exists()) {
            if (mpFile.createNewFile()) {
                System.out.println("Executed");
            }
        }
    }

    /**
     * Get the modpack file
     *
     * @return a file
     */
    public final CustomFile getFile() {
        return file;
    }

    public interface listing {
        static String[] modpacks() {
            ArrayList<String> nameList = new ArrayList<>();

            File[] modpacks = new File(FilesUtilities.getUpdaterDir() + "/modpacks/").listFiles();
            if (modpacks != null && !Arrays.asList(modpacks).isEmpty()) {
                for (File modpack : modpacks) {
                    String extension = FilenameUtils.getExtension(modpack.getName());
                    String name = modpack.getName().replace("." + extension, "");

                    nameList.add(name);
                }
            }

            String[] names = new String[nameList.size()];
            for (int i = 0; i < names.length; i++) {
                names[i] = nameList.get(i);
            }

            return names;
        }
    }
}
