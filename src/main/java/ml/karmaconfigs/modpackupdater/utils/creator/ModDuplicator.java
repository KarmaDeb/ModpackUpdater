package ml.karmaconfigs.modpackupdater.utils.creator;

import ml.karmaconfigs.modpackupdater.files.FileHasher;
import ml.karmaconfigs.modpackupdater.files.data.Data;
import ml.karmaconfigs.modpackupdater.files.data.DataWriter;
import ml.karmaconfigs.modpackupdater.utils.*;

import java.io.File;
import java.util.HashSet;

public final class ModDuplicator implements Utils {

    private final String version;

    /**
     * Initialize the mod duplicator
     * tool for the specified mc version
     *
     * @param _version the mc version
     */
    public ModDuplicator(final String _version) {
        version = _version;
    }

    /**
     * Copy the mods into the modpack location
     *
     * @param name the modpack name
     */
    public final void copyTo(final String name) {
        new AsyncScheduler(() -> {
            HashSet<Data> mod_data = new HashSet<>();

            Cache cache = new Cache();
            File mc = cache.getMcFolder();

            File mods = new File(mc, "mods");
            File v_mods = new File(mods, version);

            if (mods.exists()) {
                Debug.util.add(Text.util.create("Starting to copy mods from " + Utils.findPath(mods), Color.LIGHTGRAY, 12), true);

                File destDir = new File(Utils.getPackDir(name), "upload/mods");

                if (!destDir.exists() && destDir.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(destDir), Color.LIGHTGREEN, 12), false);

                for (File mod : mods.listFiles()) {
                    if (mod.getName().endsWith(".jar")) {
                        try {
                            FileHasher hasher = new FileHasher(mod);
                            File dest = hasher.hashAndCompress(destDir);

                            Data data = new Data(dest.getName().substring(0, 5));
                            data.addData("Hash", dest.getName());
                            data.addData("Size", dest.length());
                            data.addData("Original", mod.length());
                            data.addData("Path", "mods/" + mod.getName());

                            mod_data.add(data);

                            Debug.util.add(Text.util.create("Copied " + mod.getName() + " to " + name + "/upload/mods/" + dest.getName(), Color.LIGHTGREEN, 12), false);
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }
                }
            }

            if (v_mods.exists()) {
                Debug.util.add(Text.util.create("Starting to copy mods from " + Utils.findPath(v_mods), Color.LIGHTGRAY, 12), true);

                File destDir = new File(Utils.getPackDir(name), "upload/mods");

                if (!destDir.exists() && destDir.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(destDir), Color.LIGHTGREEN, 12), false);

                for (File mod : v_mods.listFiles()) {
                    if (mod.getName().endsWith(".jar")) {
                        try {
                            FileHasher hasher = new FileHasher(mod);
                            File dest = hasher.hashAndCompress(destDir);

                            Data data = new Data(dest.getName().substring(0, 5));
                            data.addData("Hash", dest.getName());
                            data.addData("Size", dest.length());
                            data.addData("Original", mod.length());
                            data.addData("Path", "mods/" + version + "/" + mod.getName());

                            mod_data.add(data);

                            Debug.util.add(Text.util.create("Copied " + mod.getName() + " to " + name + "/upload/mods/" + dest.getName(), Color.LIGHTGREEN, 12), false);
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }
                }
            }

            DataWriter writer = new DataWriter(mod_data);
            File dest = new File(Utils.getPackDir(name), "upload/data.json");

            writer.write("mods", dest);
        }).run();
    }
}
