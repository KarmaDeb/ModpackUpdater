package ml.karmaconfigs.modpackupdater.utils.creator;

import ml.karmaconfigs.modpackupdater.Creator;
import ml.karmaconfigs.modpackupdater.files.FileHasher;
import ml.karmaconfigs.modpackupdater.files.data.Data;
import ml.karmaconfigs.modpackupdater.files.data.DataWriter;
import ml.karmaconfigs.modpackupdater.utils.*;

import java.io.File;
import java.util.HashSet;

public final class TextureDuplicator implements Utils {

    /**
     * Copy the mods into the modpack location
     *
     * @param name the modpack name
     */
    public final void copyTo(final String name) {
        new AsyncScheduler(() -> {
            HashSet<Data> textures_data = new HashSet<>();

            Cache cache = new Cache();
            File mc = cache.getMcFolder();

            File textures = new File(mc, "resourcepacks");

            if (textures.exists()) {
                Debug.util.add(Text.util.create("Starting to copy resourcepack files from " + Utils.findPath(textures), Color.LIGHTGRAY, 12), true);

                File destDir = new File(Utils.getPackDir(name), "upload/resourcepacks");

                if (!destDir.exists() && destDir.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(destDir), Color.LIGHTGREEN, 12), false);

                for (File texture : textures.listFiles()) {
                    if (texture.getName().endsWith(".zip") || texture.getName().endsWith(".rar")) {
                        if (Creator.manager.isTextureIncluded(texture.getName())) {
                            try {
                                FileHasher hasher = new FileHasher(texture);
                                File dest = hasher.hashAndCompress(destDir);

                                Data data = new Data(dest.getName().substring(0, 5));
                                data.addData("Hash", dest.getName());
                                data.addData("Size", dest.length());
                                data.addData("Original", texture.length());
                                data.addData("Path", "resourcepacks/" + texture.getName());

                                textures_data.add(data);

                                Debug.util.add(Text.util.create("Copied " + texture.getName() + " to " + name + "/upload/resourcepacks/" + dest.getName(), Color.LIGHTGREEN, 12), false);
                            } catch (Throwable ex) {
                                Text text = new Text(ex);
                                text.format(Color.INDIANRED, 14);

                                Debug.util.add(text, true);
                            }
                        }
                    }
                }
            }

            DataWriter writer = new DataWriter(textures_data);
            File dest = new File(Utils.getPackDir(name), "upload/data.json");

            writer.write("textures", dest);
        }).run();
    }
}
