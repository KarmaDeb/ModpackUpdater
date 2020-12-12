package ml.karmaconfigs.modpackupdater.utils.downloader;

import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.utils.*;
import ml.karmaconfigs.modpackupdater.utils.datatype.*;

import java.io.*;
import java.net.URL;
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
                File mc_dir = Utils.getPackMc(modpack);
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

                        File dest_file = new File(Utils.getPackDir(modpack.getName()) + File.separator + "download" + File.separator + "mods", mod.getHash());
                        if (!dest_file.getParentFile().exists() && dest_file.getParentFile().mkdirs())
                            Debug.util.add(Text.util.create("Created directory " + Utils.findPath(dest_file.getParentFile()), Color.LIGHTGREEN, 12), true);

                        if (hard && dest_file.exists())
                            Files.delete(dest_file.toPath());

                        if (!dest_file.exists()) {
                            if (dest_file.createNewFile()) {
                                Debug.util.add(Text.util.create("Downloading mod " + mod.getName() +
                                        "<br>( " + formatLength(mod.getRealSize()) + " to download )" , Color.WHITE, 12), false);
                            } else {
                                Debug.util.add(Text.util.create("Skipped download of mod " + mod.getName() + " ( Something went wrong )", Color.WHITE, 12), false);
                                continue;
                            }
                        } else {
                            boolean needsUpdate = dest_file.length() - 5 < mod.getSize() - 10 && dest_file.length() + 5 > mod.getSize() + 10;

                            if (needsUpdate) {
                                Debug.util.add(Text.util.create("Updating mod " + mod.getName() +
                                        "<br>( " + formatLength(mod.getRealSize() - dest_file.length()) + " of difference, " + formatLength(mod.getRealSize()) + " to download )", Color.WHITE, 12), false);
                            } else {
                                Debug.util.add(Text.util.create("Skipped " + formatLength(mod.getRealSize()) + " update of mod " + mod.getName() + " ( Already downloaded )", Color.WHITE, 12), false);

                                File mod_file = new File(mc_dir + File.separator + "mods", mod.getName());

                                if (hard && mod_file.exists())
                                    Files.delete(mod_file.toPath());

                                if (!mod_file.getParentFile().exists() && mod_file.getParentFile().mkdirs())
                                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(mod_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                if (!mod_file.exists()) {
                                    FileInputStream compressed = new FileInputStream(dest_file);
                                    FileOutputStream decompressed = new FileOutputStream(mod_file);
                                    InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                    int data;
                                    while ((data = decompressor.read()) != -1) {
                                        decompressed.write(data);
                                    }

                                    compressed.close();
                                    decompressed.close();
                                    decompressor.close();
                                }

                                continue;
                            }
                        }

                        URL url = new URL(mod_url);

                        BufferedInputStream in = new BufferedInputStream(url.openStream());
                        OutputStream out = new FileOutputStream(dest_file);

                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            out.write(dataBuffer, 0, bytesRead);
                        }

                        in.close();
                        out.close();

                        File mod_file = new File(mc_dir + File.separator + "mods", mod.getName());

                        if (hard && mod_file.exists())
                            Files.delete(mod_file.toPath());

                        if (!mod_file.getParentFile().exists() && mod_file.getParentFile().mkdirs())
                            Debug.util.add(Text.util.create("Created directory " + Utils.findPath(mod_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                        if (!mod_file.exists()) {
                            FileInputStream compressed = new FileInputStream(dest_file);
                            FileOutputStream decompressed = new FileOutputStream(mod_file);
                            InflaterInputStream decompressor = new InflaterInputStream(compressed);

                            int data;
                            while ((data = decompressor.read()) != -1) {
                                decompressed.write(data);
                            }

                            compressed.close();
                            decompressed.close();
                            decompressor.close();
                        }
                    } else {
                        if (resource instanceof Version) {
                            if (!sent) {
                                Debug.util.add(Text.util.create("Starting download of modpack versions", Color.LIGHTGREEN, 12), true);
                                sent = true;
                            }

                            Version version = (Version) resource;

                            String ver_main = modpack.getMainURL() + "upload/versions";
                            String ver_url = ver_main + "/" + version.getHash();

                            File dest_file = new File(Utils.getPackDir(modpack.getName()) + File.separator + "download" + File.separator + "versions", version.getHash());
                            if (!dest_file.getParentFile().exists() && dest_file.getParentFile().mkdirs())
                                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(dest_file.getParentFile()), Color.LIGHTGREEN, 12), true);

                            if (hard && dest_file.exists())
                                Files.delete(dest_file.toPath());

                            if (!dest_file.exists()) {
                                if (dest_file.createNewFile()) {
                                    Debug.util.add(Text.util.create("Downloading version " + version.getName() +
                                            "<br>( " + formatLength(version.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                } else {
                                    Debug.util.add(Text.util.create("Skipped download of version " + version.getName() + " ( Something went wrong )", Color.WHITE, 12), false);
                                    continue;
                                }
                            } else {
                                boolean needsUpdate = dest_file.length() - 5 < version.getSize() - 10 && dest_file.length() + 5 > version.getSize() + 10;

                                if (needsUpdate) {
                                    Debug.util.add(Text.util.create("Updating version " + version.getName() +
                                            "<br>( " + formatLength(version.getSize() - dest_file.length()) + " of difference, " + formatLength(version.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                } else {
                                    Debug.util.add(Text.util.create("Skipped " + formatLength(version.getRealSize()) + " update of version " + version.getName() + " ( Already downloaded )", Color.WHITE, 12), false);

                                    File version_file = new File(mc_dir, version.getName());

                                    if (hard && version_file.exists())
                                        Files.delete(version_file.toPath());

                                    if (!version_file.getParentFile().exists() && version_file.getParentFile().mkdirs())
                                        Debug.util.add(Text.util.create("Created directory " + Utils.findPath(version_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                    if (!version_file.exists()) {
                                        FileInputStream compressed = new FileInputStream(dest_file);
                                        FileOutputStream decompressed = new FileOutputStream(version_file);
                                        InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                        int data;
                                        while ((data = decompressor.read()) != -1) {
                                            decompressed.write(data);
                                        }

                                        compressed.close();
                                        decompressed.close();
                                        decompressor.close();
                                    }

                                    continue;
                                }
                            }

                            URL url = new URL(ver_url);

                            BufferedInputStream in = new BufferedInputStream(url.openStream());
                            OutputStream out = new FileOutputStream(dest_file);

                            byte[] dataBuffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                                out.write(dataBuffer, 0, bytesRead);
                            }

                            in.close();
                            out.close();

                            File version_file = new File(mc_dir, version.getName());

                            if (hard && version_file.exists())
                                Files.delete(version_file.toPath());

                            if (!version_file.getParentFile().exists() && version_file.getParentFile().mkdirs())
                                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(version_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                            if (!version_file.exists()) {
                                FileInputStream compressed = new FileInputStream(dest_file);
                                FileOutputStream decompressed = new FileOutputStream(version_file);
                                InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                int data;
                                while ((data = decompressor.read()) != -1) {
                                    decompressed.write(data);
                                }

                                compressed.close();
                                decompressed.close();
                                decompressor.close();
                            }
                        } else {
                            if (resource instanceof Texturepack) {
                                if (!sent) {
                                    Debug.util.add(Text.util.create("Starting download of modpack resourcepacks", Color.LIGHTGREEN, 12), true);
                                    sent = true;
                                }

                                Texturepack texture = (Texturepack) resource;

                                String tex_main = modpack.getMainURL() + "upload/resourcepacks";
                                String tex_url = tex_main + "/" + texture.getHash();

                                File dest_file = new File(Utils.getPackDir(modpack.getName()) + File.separator + "download", texture.getHash());
                                if (!dest_file.getParentFile().exists() && dest_file.getParentFile().mkdirs())
                                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(dest_file.getParentFile()), Color.LIGHTGREEN, 12), true);

                                if (hard && dest_file.exists())
                                    Files.delete(dest_file.toPath());

                                if (!dest_file.exists()) {
                                    if (dest_file.createNewFile()) {
                                        Debug.util.add(Text.util.create("Downloading resourcepack " + texture.getName() +
                                                "<br>( " + formatLength(texture.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                    } else {
                                        Debug.util.add(Text.util.create("Skipped download of resourcepack " + texture.getName() + " ( Something went wrong )", Color.WHITE, 12), false);
                                        continue;
                                    }
                                } else {
                                    boolean needsUpdate = dest_file.length() - 5 < texture.getSize() - 10 && dest_file.length() + 5 > texture.getSize() + 10;

                                    if (needsUpdate) {
                                        Debug.util.add(Text.util.create("Updating resourcepack " + texture.getName() +
                                                "<br>( " + formatLength(texture.getSize() - dest_file.length()) + " of difference, " + formatLength(texture.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                    } else {
                                        Debug.util.add(Text.util.create("Skipped " + formatLength(texture.getRealSize()) + " update of resourcepack " + texture.getName() + " ( Already downloaded )", Color.WHITE, 12), false);

                                        File texture_file = new File(mc_dir, texture.getName());

                                        if (hard && texture_file.exists())
                                            Files.delete(texture_file.toPath());

                                        if (!texture_file.getParentFile().exists() && texture_file.getParentFile().mkdirs())
                                            Debug.util.add(Text.util.create("Created directory " + Utils.findPath(texture_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                        if (!texture_file.exists()) {
                                            FileInputStream compressed = new FileInputStream(dest_file);
                                            FileOutputStream decompressed = new FileOutputStream(texture_file);
                                            InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                            int data;
                                            while ((data = decompressor.read()) != -1) {
                                                decompressed.write(data);
                                            }

                                            compressed.close();
                                            decompressed.close();
                                            decompressor.close();
                                        }

                                        continue;
                                    }
                                }

                                URL url = new URL(tex_url);

                                BufferedInputStream in = new BufferedInputStream(url.openStream());
                                OutputStream out = new FileOutputStream(dest_file);

                                byte[] dataBuffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                                    out.write(dataBuffer, 0, bytesRead);
                                }

                                in.close();
                                out.close();

                                File texture_file = new File(mc_dir, texture.getName());

                                if (hard && texture_file.exists())
                                    Files.delete(texture_file.toPath());

                                if (!texture_file.getParentFile().exists() && texture_file.getParentFile().mkdirs())
                                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(texture_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                if (!texture_file.exists()) {
                                    FileInputStream compressed = new FileInputStream(dest_file);
                                    FileOutputStream decompressed = new FileOutputStream(texture_file);
                                    InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                    int data;
                                    while ((data = decompressor.read()) != -1) {
                                        decompressed.write(data);
                                    }

                                    compressed.close();
                                    decompressed.close();
                                    decompressor.close();
                                }
                            } else {
                                if (resource instanceof Shaderpack) {
                                    if (!sent) {
                                        Debug.util.add(Text.util.create("Starting download of modpack shaderpacks", Color.LIGHTGREEN, 12), true);
                                        sent = true;
                                    }

                                    Shaderpack shader = (Shaderpack) resource;

                                    String sha_main = modpack.getMainURL() + "upload/shaderpacks";
                                    String sha_url = sha_main + "/" + shader.getHash();

                                    File dest_file = new File(Utils.getPackDir(modpack.getName()) + File.separator + "download", shader.getHash());
                                    if (hard && dest_file.exists())
                                        Files.delete(dest_file.toPath());

                                    if (!dest_file.getParentFile().exists() && dest_file.getParentFile().mkdirs())
                                        Debug.util.add(Text.util.create("Created directory " + Utils.findPath(dest_file.getParentFile()), Color.LIGHTGREEN, 12), true);

                                    if (!dest_file.exists()) {
                                        if (dest_file.createNewFile()) {
                                            Debug.util.add(Text.util.create("Downloading shaderpack " + shader.getName() +
                                                    "<br>( " + formatLength(shader.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                        } else {
                                            Debug.util.add(Text.util.create("Skipped download of shaderpack " + shader.getName() + " ( Something went wrong )", Color.WHITE, 12), false);
                                            continue;
                                        }
                                    } else {
                                        boolean needsUpdate = dest_file.length() - 5 < shader.getSize() - 10 && dest_file.length() + 5 > shader.getSize() + 10;

                                        if (needsUpdate) {
                                            Debug.util.add(Text.util.create("Updating shaderpack " + shader.getName() +
                                                    "<br>( " + formatLength(shader.getSize() - dest_file.length()) + " of difference, " + formatLength(shader.getRealSize()) + " to download )", Color.WHITE, 12), false);
                                        } else {
                                            Debug.util.add(Text.util.create("Skipped " + formatLength(shader.getRealSize()) + " update of shaderpack " + shader.getName() + " ( Already downloaded )", Color.WHITE, 12), false);

                                            File shader_file = new File(mc_dir, shader.getName());

                                            if (hard && shader_file.exists())
                                                Files.delete(shader_file.toPath());

                                            if (!shader_file.getParentFile().exists() && shader_file.getParentFile().mkdirs())
                                                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(shader_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                            if (!shader_file.exists()) {
                                                FileInputStream compressed = new FileInputStream(dest_file);
                                                FileOutputStream decompressed = new FileOutputStream(shader_file);
                                                InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                                int data;
                                                while ((data = decompressor.read()) != -1) {
                                                    decompressed.write(data);
                                                }

                                                compressed.close();
                                                decompressed.close();
                                                decompressor.close();
                                            }

                                            continue;
                                        }
                                    }

                                    URL url = new URL(sha_url);

                                    BufferedInputStream in = new BufferedInputStream(url.openStream());
                                    OutputStream out = new FileOutputStream(dest_file);

                                    byte[] dataBuffer = new byte[1024];
                                    int bytesRead;
                                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                                        out.write(dataBuffer, 0, bytesRead);
                                    }

                                    in.close();
                                    out.close();

                                    File shader_file = new File(mc_dir, shader.getName());

                                    if (hard && shader_file.exists())
                                        Files.delete(shader_file.toPath());

                                    if (!shader_file.getParentFile().exists() && shader_file.getParentFile().mkdirs())
                                        Debug.util.add(Text.util.create("Created directory " + Utils.findPath(shader_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                    if (!shader_file.exists()) {
                                        FileInputStream compressed = new FileInputStream(dest_file);
                                        FileOutputStream decompressed = new FileOutputStream(shader_file);
                                        InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                        int data;
                                        while ((data = decompressor.read()) != -1) {
                                            decompressed.write(data);
                                        }

                                        compressed.close();
                                        decompressed.close();
                                        decompressor.close();
                                    }
                                } else {
                                    if (resource instanceof World) {
                                        if (!sent) {
                                            Debug.util.add(Text.util.create("Starting download of modpack worlds", Color.LIGHTGREEN, 12), true);
                                            sent = true;
                                        }

                                        World world = (World) resource;
                                        HashMap<String, String> hash_path = world.getHashPathMap();

                                        File w_folder = new File(mc_dir, "saves");
                                        File wd_folder = new File(w_folder, world.getName());

                                        for (String hash : hash_path.keySet()) {
                                            String path = hash_path.get(hash);

                                            String wor_main = modpack.getMainURL() + "upload/worlds/" + world.getHash();
                                            String wor_url = wor_main + "/" + hash;

                                            File dest_file = new File(Utils.getPackDir(modpack.getName()) + File.separator + "download" + File.separator + "worlds" + File.separator + world.getName(), hash);
                                            if (hard && dest_file.exists())
                                                Files.delete(dest_file.toPath());

                                            if (!dest_file.getParentFile().exists() && dest_file.getParentFile().mkdirs())
                                                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(dest_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                            if (dest_file.createNewFile()) {
                                                Debug.util.add(Text.util.create("Downloading world " + world.getName() + " file " +
                                                        "<br>( " + formatLength(world.getRealSize() - wd_folder.length()) + " to download )", Color.WHITE, 12), false);

                                                URL url = new URL(wor_url);

                                                BufferedInputStream in = new BufferedInputStream(url.openStream());
                                                OutputStream out = new FileOutputStream(dest_file);

                                                byte[] dataBuffer = new byte[1024];
                                                int bytesRead;
                                                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                                                    out.write(dataBuffer, 0, bytesRead);
                                                }

                                                in.close();
                                                out.close();

                                                File world_file = new File(mc_dir + File.separator + "worlds", path);

                                                if (hard && world_file.exists())
                                                    Files.delete(world_file.toPath());

                                                if (!world_file.getParentFile().exists() && world_file.getParentFile().mkdirs())
                                                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(world_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                                if (!world_file.exists()) {
                                                    FileInputStream compressed = new FileInputStream(dest_file);
                                                    FileOutputStream decompressed = new FileOutputStream(world_file);
                                                    InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                                    int data;
                                                    while ((data = decompressor.read()) != -1) {
                                                        decompressed.write(data);
                                                    }

                                                    compressed.close();
                                                    decompressed.close();
                                                    decompressor.close();
                                                }
                                            } else {
                                                Debug.util.add(Text.util.create("Skipped download of world " + world.getName() + " file  ( World data already exists )", Color.WHITE, 12), false);

                                                File world_file = new File(mc_dir + File.separator + "worlds", path);

                                                if (hard && world_file.exists())
                                                    Files.delete(world_file.toPath());

                                                if (!world_file.getParentFile().exists() && world_file.getParentFile().mkdirs())
                                                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(world_file.getParentFile()), Color.LIGHTGREEN, 12), false);

                                                if (!world_file.exists()) {
                                                    FileInputStream compressed = new FileInputStream(dest_file);
                                                    FileOutputStream decompressed = new FileOutputStream(world_file);
                                                    InflaterInputStream decompressor = new InflaterInputStream(compressed);

                                                    int data;
                                                    while ((data = decompressor.read()) != -1) {
                                                        decompressed.write(data);
                                                    }

                                                    compressed.close();
                                                    decompressed.close();
                                                    decompressor.close();
                                                }
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
