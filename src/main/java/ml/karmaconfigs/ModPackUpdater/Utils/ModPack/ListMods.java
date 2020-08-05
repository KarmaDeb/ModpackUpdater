package ml.karmaconfigs.ModPackUpdater.Utils.ModPack;

import ml.karmaconfigs.ModPackUpdater.Utils.Files.CustomFile;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class ListMods {

    private static String url;

    private final Utils utils = new Utils();

    public ListMods(String url) {
        ListMods.url = url;
    }

    private final static HashSet<File> staticMods = new HashSet<>();

    private void list() {
        File modFolder = new File(FilesUtilities.getMinecraftDir() + "/mods");
        if (!modFolder.exists()) {
            if (modFolder.mkdirs()) {
                utils.setDebug(utils.rgbColor("Created mods folder", 255, 100, 100), true);
            }
        }

        try {
            URL downloadURL = new URL(url);

            ReadableByteChannel rbc = Channels.newChannel(downloadURL.openStream());
            FileOutputStream fos = new FileOutputStream(FilesUtilities.getFileFromURL(url));
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            CustomFile file = new CustomFile(FilesUtilities.getFileFromURL(url), true);

            List<Object> modNames = file.getList("MODS");
            for (Object modName : modNames) {
                File modFile = new File(FilesUtilities.getMinecraftDir() + "/mods/" + modName);

                staticMods.add(modFile);
            }
        } catch (Throwable e) {
            utils.log(e);
        }
    }

    public ArrayList<File> getMods() {
        list();
        ArrayList<File> mods = new ArrayList<>();
        for (File file : staticMods) {
            if (!mods.contains(file)) {
                mods.add(file);
            }
        }
        return mods;
    }
}
