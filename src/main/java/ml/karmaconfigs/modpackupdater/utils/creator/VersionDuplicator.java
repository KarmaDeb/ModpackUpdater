package ml.karmaconfigs.modpackupdater.utils.creator;

import ml.karmaconfigs.modpackupdater.files.FileHasher;
import ml.karmaconfigs.modpackupdater.files.data.Data;
import ml.karmaconfigs.modpackupdater.files.data.DataWriter;
import ml.karmaconfigs.modpackupdater.utils.*;

import java.io.File;
import java.util.HashSet;

public final class VersionDuplicator implements Utils {

    private final String ver;

    /**
     * Initialize the mod duplicator
     * tool for the specified mc version
     *
     * @param _version the mc version
     */
    public VersionDuplicator(final String _version) {
        ver = _version;
    }

    /**
     * Copy the mods into the modpack location
     *
     * @param name the modpack name
     */
    public final void copyTo(final String name) {
        new AsyncScheduler(() -> {
            HashSet<Data> version_data = new HashSet<>();

            Cache cache = new Cache();
            File mc = cache.getMcFolder();

            File versions = new File(mc, "versions");
            File v_folder = new File(versions, ver);

            if (v_folder.exists()) {
                Debug.util.add(Text.util.create("Starting to copy version files from " + Utils.findPath(v_folder), Color.LIGHTGRAY, 12), true);

                File destDir = new File(Utils.getPackDir(name), "upload/versions");

                if (!destDir.exists() && destDir.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(destDir), Color.LIGHTGREEN, 12), false);

                for (File version : v_folder.listFiles()) {
                    if (version.getName().endsWith(".jar") || version.getName().endsWith(".json")) {
                        try {
                            FileHasher hasher = new FileHasher(version);
                            File dest = hasher.hashAndCompress(destDir);

                            Data data = new Data(dest.getName().substring(0, 5));
                            data.addData("Hash", dest.getName());
                            data.addData("Size", dest.length());
                            data.addData("Original", version.length());
                            data.addData("Path", "versions/" + ver + "/" + version.getName());

                            version_data.add(data);

                            Debug.util.add(Text.util.create("Copied " + version.getName() + " to " + name + "/upload/versions/" + dest.getName(), Color.LIGHTGREEN, 12), false);
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        }
                    }
                }
            }

            DataWriter writer = new DataWriter(version_data);
            File dest = new File(Utils.getPackDir(name), "upload/data.json");

            writer.write("versions", dest);
        }).run();
    }
}
