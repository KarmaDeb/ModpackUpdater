package ml.karmaconfigs.modpackupdater.files.memory;

import ml.karmaconfigs.modpackupdater.files.CustomFile;
import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class ClientMemory implements Utils {

    private final static CustomFile cfg = new CustomFile(new File(getUpdaterDir, "client.config"), true);

    /**
     * Save the last modpack download url
     *
     * @param url the url address
     */
    public final void saveURL(final String url) {
        cfg.set("Modpack_Url", url);
    }

    /**
     * Save the last selected minecraft
     * directory
     *
     * @param dir the minecraft directory
     */
    public final void saveMc(final File dir) {
        cfg.set("Minecraft_Dir", dir.getAbsolutePath());
    }

    /**
     * Save the last selected minecraft
     * directory
     *
     * @param dir the minecraft directory
     */
    public final void saveModpack(final File dir) {
        cfg.set("Modpack_Dir", dir.getAbsolutePath());
    }

    /**
     * Save the auto scroll status of the tool
     *
     * @param auto auto scroll?
     */
    public final void saveAutoScroll(final boolean auto) {
        cfg.set("UI_AutoScroll", auto);
    }

    /**
     * Save the auto scroll status of the log
     * output
     *
     * @param auto auto scroll?
     */
    public final void saveLogScroll(final boolean auto) {
        cfg.set("UI_LogScroll", auto);
    }

    /**
     * Save the update check status of the tool
     *
     * @param check check updates?
     */
    public final void saveUpdateCheck(final boolean check) {
        cfg.set("UI_CheckUpdates", check);
    }

    /**
     * Get the last modpack download url
     *
     * @return the latest modpack download url
     */
    public final String loadURL() {
        return cfg.getString("Modpack_Url", "");
    }

    /**
     * Get the last selected minecraft
     * directory
     *
     * @return the latest selected
     * minecraft directory
     */
    public final File getMc() {
        String path = cfg.getString("Minecraft_Dir", defaultMC.getAbsolutePath());

        return new File(path);
    }

    /**
     * Get the last selected minecraft
     * directory
     *
     * @return the latest selected
     * minecraft directory
     */
    public final File getModpackDir() {
        String path = cfg.getString("Modpack_Dir", System.getProperty("user.home"));

        return new File(path);
    }

    /**
     * Get the tool auto scroll status
     */
    public final boolean autoScroll() {
        return cfg.getBoolean("UI_AutoScroll", true);
    }

    /**
     * Get the logs auto scroll status
     */
    public final boolean logScroll() {
        return cfg.getBoolean("UI_LogScroll", true);
    }

    /**
     * Get the tool update checker status
     */
    public final boolean updateChecks() {
        return cfg.getBoolean("UI_CheckUpdates", true);
    }
}
