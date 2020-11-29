package ml.karmaconfigs.modpackupdater.utils.creator;

import ml.karmaconfigs.modpackupdater.Options;
import ml.karmaconfigs.modpackupdater.files.FileHasher;
import ml.karmaconfigs.modpackupdater.files.data.Data;
import ml.karmaconfigs.modpackupdater.files.data.DataWriter;
import ml.karmaconfigs.modpackupdater.utils.*;

import java.io.File;
import java.util.HashSet;

public final class ShaderDuplicator implements Utils {

    /**
     * Copy the mods into the modpack location
     *
     * @param name the modpack name
     */
    public final void copyTo(final String name) {
        new AsyncScheduler(() -> {
            HashSet<Data> shaders_data = new HashSet<>();

            Cache cache = new Cache();
            File mc = cache.getMcFolder();

            File shaders = new File(mc, "shaderpacks");

            if (shaders.exists()) {
                Debug.util.add(Text.util.create("Starting to copy shaderpack files from " + Utils.findPath(shaders), Color.LIGHTGRAY, 12), true);

                File destDir = new File(Utils.getPackDir(name), "upload/shaderpack");

                if (!destDir.exists() && destDir.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(destDir), Color.LIGHTGREEN, 12), false);

                for (File shader : shaders.listFiles()) {
                    if (shader.getName().endsWith(".zip") || shader.getName().endsWith(".rar")) {
                        if (Options.manager.isShaderIncluded(shader.getName())) {
                            try {
                                FileHasher hasher = new FileHasher(shader);
                                File dest = hasher.hashAndCompress(destDir);

                                Data data = new Data(dest.getName().substring(0, 2));
                                data.addData("Hash", dest.getName());
                                data.addData("Size", dest.length());
                                data.addData("Original", shader.length());
                                data.addData("Path", "shaderpacks/" + shader.getName());

                                shaders_data.add(data);

                                Debug.util.add(Text.util.create("Copied " + shader.getName() + " to " + name + "/upload/shaderpacks/" + dest.getName(), Color.LIGHTGREEN, 12), false);
                            } catch (Throwable ex) {
                                Text text = new Text(ex);
                                text.format(Color.INDIANRED, 14);

                                Debug.util.add(text, true);
                            }
                        }
                    }
                }
            }

            DataWriter writer = new DataWriter(shaders_data);
            File dest = new File(Utils.getPackDir(name), "upload/data.json");

            writer.write("shaders", dest);
        }).run();
    }
}
