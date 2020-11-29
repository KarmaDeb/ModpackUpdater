package ml.karmaconfigs.modpackupdater.files.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.utils.Color;
import ml.karmaconfigs.modpackupdater.utils.Debug;
import ml.karmaconfigs.modpackupdater.utils.Text;
import ml.karmaconfigs.modpackupdater.utils.Utils;
import ml.karmaconfigs.modpackupdater.utils.datatype.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;

public final class DataReader implements Utils {

    private final MPUExt modpack;

    private static boolean downloading = false;

    /**
     * Initialize the data reader
     *
     * @param _modpack the modpack file
     */
    public DataReader(final MPUExt _modpack) {
        modpack = _modpack;
    }

    /**
     * Download the modpack data
     */
    public final void downloadData() {
        if (modpack != null) {
            if (!downloading) {
                downloading = true;

                String data_json = modpack.getMainURL() + "upload/data.json";

                File modpack_folder = new File(getPacksDir, modpack.getName());
                File download = new File(modpack_folder, "download");
                File data = new File(download, "data.json");

                if (data.exists() && data.delete())
                    Debug.util.add(Text.util.create("Removed old modpack " + modpack.getName() + " data file", Color.LIGHTGREEN, 12), false);

                if (!modpack_folder.exists() && modpack_folder.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(modpack_folder), Color.LIGHTGREEN, 12), false);
                if (!download.exists() && download.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(download), Color.LIGHTGREEN, 12), false);

                Debug.util.add(Text.util.create("Trying to update modpack " + modpack.getName() + " data from<br>" + data_json, Color.LIGHTGREEN, 12), true);

                try {
                    URL url = new URL(data_json);
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        if (connection.getContentLengthLong() != data.length()) {
                            Debug.util.add(Text.util.create("Downloading modpack " + modpack.getName() + " data", Color.LIGHTGREEN, 12), false);
                            BufferedInputStream in = new BufferedInputStream(url.openStream());
                            OutputStream out = new FileOutputStream(data);

                            byte[] dataBuffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                                out.write(dataBuffer, 0, bytesRead);
                            }

                            in.close();
                            out.close();
                        } else {
                            Debug.util.add(Text.util.create("Skipped modpack " + modpack.getName() + " data update because already updated", Color.LIGHTGREEN, 12), false);
                        }
                    } else {
                        Debug.util.add(Text.util.create("Skipped modpack " + modpack.getName() + " data update | Bad connection request", Color.LIGHTGREEN, 12), false);
                    }
                } catch (Throwable ex) {
                    Text text = new Text(ex);
                    text.format(Color.INDIANRED, 14);

                    Debug.util.add(text, true);
                } finally {
                    Debug.util.add(Text.util.create("Downloaded modpack " + modpack.getName() + " data", Color.LIGHTGREEN, 12), false);
                    downloading = false;
                }
            }
        }
    }

    /**
     * Check if the tool is downloading the modpack data
     *
     * @return if the modpack is downloading the data
     */
    public final boolean isDownloading() {
        return downloading;
    }

    public final HashSet<Resource> getMods() {
        HashSet<Resource> detected_mods = new HashSet<>();

        if (modpack != null) {
            File modpack_folder = new File(getPacksDir, modpack.getName());
            File download = new File(modpack_folder, "download");
            File data = new File(download, "data.json");

            if (data.exists()) {
                if (!modpack_folder.exists() && modpack_folder.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(modpack_folder), Color.LIGHTGREEN, 12), false);
                if (!download.exists() && download.mkdirs())
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(download), Color.LIGHTGREEN, 12), false);

                try {
                    Debug.util.add(Text.util.create("Reading " + modpack.getName() + " mods data", Color.LIGHTGREEN, 12), true);
                    Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                    Reader reader = Files.newBufferedReader(data.toPath());

                    JsonReader json_reader = new JsonReader(reader);
                    json_reader.setLenient(true);

                    JsonObject object = gson.fromJson(json_reader, JsonObject.class);
                    if (object.has("mods")) {
                        JsonObject mods = object.get("mods").getAsJsonObject();

                        for (String key : mods.keySet()) {
                            JsonObject mod = mods.get(key).getAsJsonObject();
                            String name = mod.get("Path").getAsString().replace("mods/", "");
                            String hash = mod.get("Hash").getAsString();
                            long size = mod.get("Size").getAsLong();
                            long original = mod.get("Original").getAsLong();

                            Mod mod_instance = new Mod(name, hash, size, original);
                            detected_mods.add(mod_instance);
                        }
                    }
                } catch (Throwable ex) {
                    Text text = new Text(ex);
                    text.format(Color.INDIANRED, 14);

                    Debug.util.add(text, true);
                }
            }
        }

        return detected_mods;
    }

    public final HashSet<Resource> getVersions() {
        HashSet<Resource> detected_versions = new HashSet<>();

        if (modpack != null) {
            File modpack_folder = new File(getPacksDir, modpack.getName());
            File download = new File(modpack_folder, "download");
            File data = new File(download, "data.json");

            if (!modpack_folder.exists() && modpack_folder.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(modpack_folder), Color.LIGHTGREEN, 12), false);
            if (!download.exists() && download.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(download), Color.LIGHTGREEN, 12), false);

            Debug.util.add(Text.util.create("Reading " + modpack.getName() + " versions data", Color.LIGHTGREEN, 12), true);
            try {
                Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                Reader reader = Files.newBufferedReader(data.toPath());

                JsonReader json_reader = new JsonReader(reader);
                json_reader.setLenient(true);

                JsonObject object = gson.fromJson(json_reader, JsonObject.class);
                if (object.has("versions")) {
                    JsonObject versions = object.get("versions").getAsJsonObject();

                    for (String key : versions.keySet()) {
                        JsonObject version = versions.get(key).getAsJsonObject();
                        String name = version.get("Path").getAsString();
                        String hash = version.get("Hash").getAsString();
                        long size = version.get("Size").getAsLong();
                        long original = version.get("Original").getAsLong();

                        Version version_instance = new Version(name, hash, size, original);
                        detected_versions.add(version_instance);
                    }
                }
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }

        return detected_versions;
    }

    public final HashSet<Resource> getResourcepacks() {
        HashSet<Resource> detected_resources = new HashSet<>();

        if (modpack != null) {
            File modpack_folder = new File(getPacksDir, modpack.getName());
            File download = new File(modpack_folder, "download");
            File data = new File(download, "data.json");

            if (!modpack_folder.exists() && modpack_folder.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(modpack_folder), Color.LIGHTGREEN, 12), false);
            if (!download.exists() && download.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(download), Color.LIGHTGREEN, 12), false);

            try {
                Debug.util.add(Text.util.create("Reading " + modpack.getName() + " resourcepack data", Color.LIGHTGREEN, 12), true);
                Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                Reader reader = Files.newBufferedReader(data.toPath());

                JsonReader json_reader = new JsonReader(reader);
                json_reader.setLenient(true);

                JsonObject object = gson.fromJson(json_reader, JsonObject.class);
                if (object.has("textures")) {
                    JsonObject textures = object.get("textures").getAsJsonObject();

                    for (String key : textures.keySet()) {
                        JsonObject texture = textures.get(key).getAsJsonObject();
                        String name = texture.get("Path").getAsString();
                        String hash = texture.get("Hash").getAsString();
                        long size = texture.get("Size").getAsLong();
                        long original = texture.get("Original").getAsLong();

                        Texturepack texture_instance = new Texturepack(name, hash, size, original);
                        detected_resources.add(texture_instance);
                    }
                }
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }

        return detected_resources;
    }

    public final HashSet<Resource> getShaderpacks() {
        HashSet<Resource> detected_shaders = new HashSet<>();

        if (modpack != null) {
            File modpack_folder = new File(getPacksDir, modpack.getName());
            File download = new File(modpack_folder, "download");
            File data = new File(download, "data.json");

            if (!modpack_folder.exists() && modpack_folder.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(modpack_folder), Color.LIGHTGREEN, 12), false);
            if (!download.exists() && download.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(download), Color.LIGHTGREEN, 12), false);

            try {
                Debug.util.add(Text.util.create("Reading " + modpack.getName() + " shaderpack data", Color.LIGHTGREEN, 12), true);
                Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                Reader reader = Files.newBufferedReader(data.toPath());

                JsonReader json_reader = new JsonReader(reader);
                json_reader.setLenient(true);

                JsonObject object = gson.fromJson(json_reader, JsonObject.class);
                if (object.has("shaders")) {
                    JsonObject shaders = object.get("shaders").getAsJsonObject();

                    for (String key : shaders.keySet()) {
                        JsonObject shader = shaders.get(key).getAsJsonObject();
                        String name = shader.get("Path").getAsString();
                        String hash = shader.get("Hash").getAsString();
                        long size = shader.get("Size").getAsLong();
                        long original = shader.get("Original").getAsLong();

                        Shaderpack shader_instance = new Shaderpack(name, hash, size, original);
                        detected_shaders.add(shader_instance);
                    }
                }
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }

        return detected_shaders;
    }

    public final HashSet<Resource> getWorlds() {
        HashSet<Resource> detected_worlds = new HashSet<>();

        if (modpack != null) {
            File modpack_folder = new File(getPacksDir, modpack.getName());
            File download = new File(modpack_folder, "download");
            File data = new File(download, "data.json");

            if (!modpack_folder.exists() && modpack_folder.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(modpack_folder), Color.LIGHTGREEN, 12), false);
            if (!download.exists() && download.mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(download), Color.LIGHTGREEN, 12), false);

            try {
                Debug.util.add(Text.util.create("Reading " + modpack.getName() + " world data", Color.LIGHTGREEN, 12), true);
                Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                Reader reader = Files.newBufferedReader(data.toPath());

                JsonReader json_reader = new JsonReader(reader);
                json_reader.setLenient(true);

                JsonObject object = gson.fromJson(json_reader, JsonObject.class);
                if (object.has("worlds")) {
                    JsonObject worlds = object.get("worlds").getAsJsonObject();

                    for (String key : worlds.keySet()) {
                        if (!key.endsWith("_info")) {
                            JsonObject world = worlds.get(key).getAsJsonObject();
                            JsonObject world_info = worlds.get(key + "_info").getAsJsonObject();

                            String name = world_info.get("Name").getAsString();
                            String hash = world_info.get("Hash").getAsString();
                            long size = world_info.get("Size").getAsLong();
                            long original = world_info.get("Original").getAsLong();

                            HashMap<String, String> hash_path = new HashMap<>();

                            for (String w_h : world.keySet()) {
                                String path = world.get(w_h).getAsString();

                                hash_path.put(w_h, path);
                            }

                            World world_instance = new World(name, hash, hash_path, size, original);
                            detected_worlds.add(world_instance);
                        }
                    }
                }
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }

        return detected_worlds;
    }
}
