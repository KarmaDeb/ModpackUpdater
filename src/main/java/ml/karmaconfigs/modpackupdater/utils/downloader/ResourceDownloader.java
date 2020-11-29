package ml.karmaconfigs.modpackupdater.utils.downloader;

import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.utils.*;
import ml.karmaconfigs.modpackupdater.utils.datatype.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.InflaterInputStream;

public final class ResourceDownloader implements Utils {

    private final HashSet<Resource> resources;
    private final MPUExt modpack;
    private static boolean terminated = true;
    private static boolean hard = false;

    /**
     * Initialize the resources downloader
     *
     * @param _modpack the owning modpack
     * @param _resources the resources
     * @param _hard hard install (re-download everything)
     */
    public ResourceDownloader(final MPUExt _modpack, final HashSet<Resource> _resources, final boolean _hard) throws AlreadyDownloadingException {
        if (terminated) {
            modpack = _modpack;
            resources = _resources;
            hard = _hard;
        } else {
            throw new AlreadyDownloadingException();
        }
    }

    /**
     * Start the download process
     */
    public final void download() {
        new AsyncScheduler(() -> {
            terminated = false;
            try {
                Cache cache = new Cache();

                boolean sent = false;

                for (Resource resource : resources) {
                    if (resource instanceof Mod) {
                        Mod mod = (Mod) resource;

                        if (!sent) {
                            Debug.util.add(Text.util.create("Starting download of modpack mods", Color.LIGHTGREEN, 12), true);
                            sent = true;
                        }

                        String mods_main = modpack.getMainURL() + "upload/mods";
                        String mod_url = mods_main + "/" + mod.getHash();

                        File mods_folder = new File(cache.getMcFolder(), "mods");
                        File mod_file = new File(mods_folder, mod.getName());
                        if (!mod_file.getParentFile().exists() && mod_file.getParentFile().mkdirs())
                            Debug.util.add(Text.util.create("Created directory " + Utils.findPath(mod_file.getParentFile()), Color.LIGHTGREEN, 12), true);

                        if (hard && mod_file.exists())
                            Files.delete(mod_file.toPath());

                        if (!mod_file.exists()) {
                            if (mod_file.createNewFile()) {
                                Debug.util.add(Text.util.create("Downloading mod " + mod_file.getName() +
                                        "<br>( " + formatLength(mod.getRealSize()) + " to download )" , Color.WHITE, 12), false);
                            } else {
                                Debug.util.add(Text.util.create("Skipped download of mod " + mod_file.getName() + " ( Something went wrong )", Color.WHITE, 12), false);
                                continue;
                            }
                        } else {
                            boolean needsUpdate = mod_file.length() - 5 < mod.getRealSize() - 10 && mod_file.length() + 5 > mod.getRealSize() + 10;

                            if (needsUpdate) {
                                Debug.util.add(Text.util.create("Updating mod " + mod_file.getName() +
                                        "<br>( " + formatLength(mod.getRealSize() - mod_file.length()) + " of difference, " + formatLength(mod.getRealSize()) + " to download )", Color.WHITE, 12), false);
                            } else {
                                Debug.util.add(Text.util.create("Skipped " + formatLength(mod.getRealSize()) + " update of mod " + mod_file.getName() + " ( Already downloaded )", Color.WHITE, 12), false);
                                continue;
                            }
                        }

                        URL url = new URL(mod_url);
                        URLConnection connection = url.openConnection();

                        InputStream in = connection.getInputStream();
                        InflaterInputStream iis = new InflaterInputStream(in);
                        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(mod_file), StandardCharsets.UTF_8);

                        while (iis.available() != 0)
                            out.write(iis.read());

                        in.close();
                        iis.close();
                        out.close();
                    } else {
                        if (resource instanceof Version) {
                            if (!sent) {
                                Debug.util.add(Text.util.create("Starting download of modpack versions", Color.LIGHTGREEN, 12), true);
                                sent = true;
                            }

                            Version version = (Version) resource;

                            String ver_main = modpack.getMainURL() + "upload/versions";
                            String ver_url = ver_main + "/" + version.getHash();

                            File v_file = new File(cache.getMcFolder(), version.getName());
                            if (!v_file.getParentFile().exists() && v_file.getParentFile().mkdirs())
                                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(v_file.getParentFile()), Color.LIGHTGREEN, 12), true);

                            if (hard && v_file.exists())
                                Files.delete(v_file.toPath());

                            if (!v_file.exists()) {
                                if (v_file.createNewFile()) {
                                    Debug.util.add(Text.util.create("Downloading version " + v_file.getName() +
                                            "<br>( " + formatLength(version.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                } else {
                                    Debug.util.add(Text.util.create("Skipped download of version " + v_file.getName() + " ( Something went wrong )", Color.WHITE, 12), false);
                                    continue;
                                }
                            } else {
                                boolean needsUpdate = v_file.length() - 5 < version.getRealSize() - 10 && v_file.length() + 5 > version.getRealSize() + 10;

                                if (needsUpdate) {
                                    Debug.util.add(Text.util.create("Updating version " + v_file.getName() +
                                            "<br>( " + formatLength(version.getRealSize() - v_file.length()) + " of difference, " + formatLength(version.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                } else {
                                    Debug.util.add(Text.util.create("Skipped " + formatLength(version.getRealSize()) + " update of version " + v_file.getName() + " ( Already downloaded )", Color.WHITE, 12), false);
                                    continue;
                                }
                            }

                            URL url = new URL(ver_url);
                            URLConnection connection = url.openConnection();

                            InputStream in = connection.getInputStream();
                            InflaterInputStream iis = new InflaterInputStream(in);
                            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(v_file), StandardCharsets.UTF_8);

                            while (iis.available() != 0)
                                out.write(iis.read());

                            in.close();
                            iis.close();
                            out.close();
                        } else {
                            if (resource instanceof Texturepack) {
                                if (!sent) {
                                    Debug.util.add(Text.util.create("Starting download of modpack resourcepacks", Color.LIGHTGREEN, 12), true);
                                    sent = true;
                                }

                                Texturepack texture = (Texturepack) resource;

                                String tex_main = modpack.getMainURL() + "upload/resourcepacks";
                                String tex_url = tex_main + "/" + texture.getHash();

                                File t_folder = new File(cache.getMcFolder(), "resourcepacks");
                                File t_file = new File(t_folder, texture.getName());
                                if (!t_folder.exists() && t_folder.mkdirs())
                                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(t_file.getParentFile()), Color.LIGHTGREEN, 12), true);

                                if (hard && t_file.exists())
                                    Files.delete(t_file.toPath());

                                if (!t_file.exists()) {
                                    if (t_file.createNewFile()) {
                                        Debug.util.add(Text.util.create("Downloading shaderpack " + t_file.getName() +
                                                "<br>( " + formatLength(texture.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                    } else {
                                        Debug.util.add(Text.util.create("Skipped download of resourcepack " + t_file.getName() + " ( Something went wrong )", Color.WHITE, 12), false);
                                        continue;
                                    }
                                } else {
                                    boolean needsUpdate = t_file.length() - 5 < texture.getRealSize() - 10 && t_file.length() + 5 > texture.getRealSize() + 10;

                                    if (needsUpdate) {
                                        Debug.util.add(Text.util.create("Updating resourcepack " + t_file.getName() +
                                                "<br>( " + formatLength(texture.getRealSize() - t_file.length()) + " of difference, " + formatLength(texture.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                    } else {
                                        Debug.util.add(Text.util.create("Skipped " + formatLength(texture.getRealSize()) + " update of resourcepack " + t_file.getName() + " ( Already downloaded )", Color.WHITE, 12), false);
                                        continue;
                                    }
                                }

                                URL url = new URL(tex_url);
                                URLConnection connection = url.openConnection();

                                InputStream in = connection.getInputStream();
                                InflaterInputStream iis = new InflaterInputStream(in);
                                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(t_file), StandardCharsets.UTF_8);

                                while (iis.available() != 0)
                                    out.write(iis.read());

                                in.close();
                                iis.close();
                                out.close();
                            } else {
                                if (resource instanceof Shaderpack) {
                                    if (!sent) {
                                        Debug.util.add(Text.util.create("Starting download of modpack shaderpacks", Color.LIGHTGREEN, 12), true);
                                        sent = true;
                                    }

                                    Shaderpack shader = (Shaderpack) resource;

                                    String sha_main = modpack.getMainURL() + "upload/shaderpacks";
                                    String sha_url = sha_main + "/" + shader.getHash();

                                    File s_folder = new File(cache.getMcFolder(), "shaderpacks");
                                    File s_file = new File(s_folder, shader.getName());
                                    if (!s_folder.exists() && s_folder.mkdirs())
                                        Debug.util.add(Text.util.create("Created directory " + Utils.findPath(s_file.getParentFile()), Color.LIGHTGREEN, 12), true);

                                    if (hard && s_file.exists())
                                        Files.delete(s_file.toPath());

                                    if (!s_file.exists()) {
                                        if (s_file.createNewFile()) {
                                            Debug.util.add(Text.util.create("Downloading shaderpack " + s_file.getName() +
                                                    "<br>( " + formatLength(shader.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                        } else {
                                            Debug.util.add(Text.util.create("Skipped download of shaderpack " + s_file.getName() + " ( Something went wrong )", Color.WHITE, 12), false);
                                            continue;
                                        }
                                    } else {
                                        boolean needsUpdate = s_file.length() - 5 < shader.getRealSize() - 10 && s_file.length() + 5 > shader.getRealSize() + 10;

                                        if (needsUpdate) {
                                            Debug.util.add(Text.util.create("Updating shaderpack " + s_file.getName() +
                                                    "<br>( " + formatLength(shader.getRealSize() - s_file.length()) + " of difference, " + formatLength(shader.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                        } else {
                                            Debug.util.add(Text.util.create("Skipped " + formatLength(shader.getRealSize()) + " update of shaderpack " + s_file.getName() + " ( Already downloaded )", Color.WHITE, 12), false);
                                            continue;
                                        }
                                    }

                                    URL url = new URL(sha_url);
                                    URLConnection connection = url.openConnection();

                                    InputStream in = connection.getInputStream();
                                    InflaterInputStream iis = new InflaterInputStream(in);
                                    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(s_file), StandardCharsets.UTF_8);

                                    while (iis.available() != 0)
                                        out.write(iis.read());

                                    in.close();
                                    iis.close();
                                    out.close();
                                } else {
                                    if (resource instanceof World) {
                                        if (!sent) {
                                            Debug.util.add(Text.util.create("Starting download of modpack worlds", Color.LIGHTGREEN, 12), true);
                                            sent = true;
                                        }

                                        World world = (World) resource;
                                        HashMap<String, String> hash_path = world.getHashPathMap();

                                        File w_folder = new File(cache.getMcFolder(), "saves");
                                        File wd_folder = new File(w_folder, world.getName());

                                        for (String hash : hash_path.keySet()) {
                                            String path = hash_path.get(hash);

                                            String wor_main = modpack.getMainURL() + "upload/worlds/" + world.getHash();
                                            String wor_url = wor_main + "/" + hash;

                                            File w_file = new File(w_folder, path);

                                            if (hard && w_file.exists())
                                                Files.delete(w_file.toPath());

                                            if (!w_folder.exists() && w_folder.mkdirs())
                                                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(w_folder.getParentFile()), Color.LIGHTGREEN, 12), true);

                                            if (!w_file.getParentFile().exists() && w_file.getParentFile().mkdirs())
                                                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(w_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                            if (w_file.createNewFile()) {
                                                Debug.util.add(Text.util.create("Downloading world " + world.getName() + " file " + w_file.getName() +
                                                        "<br>( " + formatLength(world.getRealSize() - wd_folder.length()) + " to download )", Color.WHITE, 12), false);

                                                URL url = new URL(wor_url);
                                                URLConnection connection = url.openConnection();

                                                InputStream in = connection.getInputStream();
                                                InflaterInputStream iis = new InflaterInputStream(in);
                                                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(w_file), StandardCharsets.UTF_8);

                                                while (iis.available() != 0)
                                                    out.write(iis.read());

                                                in.close();
                                                iis.close();
                                                out.close();
                                            } else {
                                                Debug.util.add(Text.util.create("Skipped download of world " + world.getName() + " file " + w_file.getName() + " ( World data already exists )", Color.WHITE, 12), false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            } finally {
                terminated = true;
            }
        }).run();
    }

    /**
     * Check if the process is terminated
     *
     * @return if the downloader has finished
     */
    public final boolean isDownloaded() {
        return terminated;
    }

    /**
     * Format the length into bit, bytes, kb, mg, and gb
     *
     * @param length the length in bytes
     * @return a formatted length with bit, bytes, kb, mg or gb
     */
    private String formatLength(final long length) {
        /*
        Make sure the long is not negative.
        It doesn't really matters if it's negative since
        the negative value is just the opposite of the positive
        value, but displaying "-5 KB to download" doesn't looks
        really good
        */
        String toStr = String.valueOf(length);
        long final_length = Long.parseLong(toStr.replace("-", ""));

        long bits = final_length * 8;
        long bytes = bits / 8;
        long kilobytes = bytes / 1024;
        long megabytes = kilobytes / 1024;
        long gigabytes = megabytes / 1024;

        if (bits < 8) {
            return bits + " bits";
        } else {
            if (bytes < 1024) {
                return bytes + " bytes";
            } else {
                if (kilobytes < 2048) {
                    return kilobytes + " KB";
                } else {
                    if (megabytes < 4056) {
                        return megabytes + " MB";
                    } else {
                        return gigabytes + " GB";
                    }
                }
            }
        }
    }
}
