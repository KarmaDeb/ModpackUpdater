package ml.karmaconfigs.ModPackUpdater.Utils.ModPack;

import ml.karmaconfigs.ModPackUpdater.Utils.Files.CustomFile;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
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
