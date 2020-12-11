package ml.karmaconfigs.modpackupdater.utils.creator;

import ml.karmaconfigs.modpackupdater.Creator;
import ml.karmaconfigs.modpackupdater.files.FileHasher;
import ml.karmaconfigs.modpackupdater.files.data.Data;
import ml.karmaconfigs.modpackupdater.files.data.DataWriter;
import ml.karmaconfigs.modpackupdater.utils.*;

import java.io.File;
import java.util.HashSet;

public final class WorldDuplicator implements Utils {

    /**
     * Copy the mods into the modpack location
     *
     * @param name the modpack name
     */
    public final void copyTo(final String name) {
        new AsyncScheduler(() -> {
            try {
                HashSet<Data> world_data = new HashSet<>();

                Cache cache = new Cache();
                File mc = cache.getMcFolder();

                File worlds = new File(mc, "saves");

                if (worlds.exists()) {
                    for (File world : worlds.listFiles()) {
                        if (world.isDirectory()) {
                            if (Creator.manager.isWorldIncluded(world.getName())) {
                                HashSet<String> contents = getFiles(world.getName(), world);

                                String hash = FileHasher.external.hash(world.getName());

                                Data data = new Data(hash.substring(0, 5));
                                File dest_dir = new File(Utils.getPackDir(name) + "/upload/worlds/" + hash);

                                for (String content : contents) {
                                    File dest = new File(worlds, content);

                                    FileHasher hasher = new FileHasher(dest);
                                    File hashed = hasher.hashAndCompress(dest_dir);

                                    data.addData(hashed.getName(), content);
                                }

                                Data world_data_sub = new Data(hash.substring(0, 2) + "_info");

                                world_data_sub.addData("Name", world.getName());
                                world_data_sub.addData("Hash", hash);
                                world_data_sub.addData("Size", world.length());
                                world_data_sub.addData("Original", dest_dir.length());

                                world_data.add(data);
                                world_data.add(world_data_sub);
                            }
                        }
                    }
                }

                DataWriter writer = new DataWriter(world_data);
                File dest = new File(Utils.getPackDir(name), "upload/data.json");

                writer.write("worlds", dest);
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }).run();
    }

    /**
     * Get the paths of all the files inside
     * that folder
     *
     * @param dir the main directory
     * @return a map with path (starting from world name) -> file
     */
    private HashSet<String> getFiles(final String world_name, final File dir) {
        HashSet<String> files = new HashSet<>();

        for (File file : dir.listFiles()) {
            if (!file.getName().contains(".")) {
                files.addAll(getFiles(world_name, file));
            } else {
                String to_remove = dir.getAbsolutePath().replaceAll("\\\\", "/").split(world_name)[0];
                String path = getPathFrom(to_remove, file);
                files.add(path);
            }
        }

        return files;
    }

    /**
     * Get the path removing the non-wanted
     * path
     *
     * @param remove the path to remove from the original
     *               path
     * @param file the file
     * @return the path starting from the dir name localizer
     */
    private String getPathFrom(final String remove, final File file) {
        String path = file.getAbsolutePath().replaceAll("\\\\", "/");

        return path.replace(remove, "");
    }
}
