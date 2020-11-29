package ml.karmaconfigs.modpackupdater.utils;

import ml.karmaconfigs.modpackupdater.Updater;
import ml.karmaconfigs.modpackupdater.files.memory.ClientMemory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

public final class Cache {

    private static BufferedImage cache_ico;
    private static File mc_folder = new ClientMemory().getMc();
    private static File mpu_file = null;
    private static String last_debug;

    /**
     * Initialize the cache storage
     */
    public Cache() {
        try {
            if (cache_ico == null)
                cache_ico = ImageIO.read(new URL("https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/img/logo.png"));
        } catch (Throwable e) {
            try {
                if (cache_ico == null) {
                    InputStream internal_logo = (Updater.class).getResourceAsStream("/logo.png");
                    cache_ico = ImageIO.read(internal_logo);
                }
            } catch (Throwable ex) {
                Text text = new Text(ex);
                text.format(Color.INDIANRED, 14);

                Debug.util.add(text, true);
            }
        }
    }

    /**
     * Save the latest debug line
     *
     * @param _new the new line
     */
    public final void saveDebug(@NotNull final String _new) {
        last_debug = _new;
    }

    /**
     * Save the latest minecraft
     * folder
     *
     * @param file the minecraft folder
     */
    public final void saveMcFolder(@NotNull final File file) {
        mc_folder = file;
    }

    /**
     * Save the latest mpu file
     *
     * @param file the mpu file
     */
    public final void saveMpuFile(@Nullable final File file) {
        if (file != null) {
            if (file.getName().endsWith(".mpu")) {
                mpu_file = file;
            } else {
                mpu_file = null;
            }
        } else {
            mpu_file = null;
        }

        if (file != null && file.getName().endsWith(".mpu"))
            new ClientMemory().saveModpack(file.getParentFile());
    }

    /**
     * Get the ico stored in cache
     * to avoid over-requesting to
     * github
     *
     * @return the stored logo in cache
     */
    @Nullable
    public final BufferedImage getIco() {
        return cache_ico;
    }

    /**
     * Get the last debug message
     *
     * @return the stored last debug message
     */
    @NotNull
    public final String getDebug() {
        return last_debug != null ? last_debug : "";
    }

    /**
     * Get the latest minecraft folder
     *
     * @return the stored minecraft folder
     */
    @NotNull
    public final File getMcFolder() {
        return mc_folder;
    }

    /**
     * Get the latest mpu file
     *
     * @return the stored mpu file
     */
    @Nullable
    public final File getMpuFile() {
        return mpu_file;
    }
}
