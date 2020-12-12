package ml.karmaconfigs.modpackupdater.files.memory;

import ml.karmaconfigs.modpackupdater.files.CustomFile;
import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class LauncherMemory implements Utils {

    private final static CustomFile cfg = new CustomFile(new File(getUpdaterDir, "launcher.config"), true);

    /**
     * Save the current modpack into the client memory
     *
     * @param modpack the modpack
     */
    public final void saveModpack(final MPUExt modpack) {
        cfg.saveInstance("Modpack", modpack);
    }

    /**
     * Save the launcher client name
     *
     * @param name the mc client name
     */
    public final void saveName(final String name) {
        cfg.set("Launch_Name", name);
    }

    /**
     * Save the launcher min memory to use
     *
     * @param min the minimum amount of memory
     */
    public final void saveMinMemory(final int min) {
        cfg.set("Launch_Min", min);
    }

    /**
     * Save the launcher max memory to use
     *
     * @param max the minimum amount of memory
     */
    public final void saveMaxMemory(final int max) {
        cfg.set("Launch_Max", max);
    }

    /**
     * Load the last modpack from client memory
     *
     * @return the latest modpack
     */
    @Nullable
    public final MPUExt loadModpack() {
        return (MPUExt) cfg.getInstance("Modpack", null);
    }

    /**
     * Get the launcher launch name
     *
     * @return the launcher client name
     */
    public final String getName() {
        return cfg.getString("Launch_Name", "Steve");
    }

    /**
     * Get the launcher min memory
     *
     * @return the launcher min memory
     */
    public final int getMinMemory() {
        return cfg.getInt("Launch_Min", 1024);
    }

    /**
     * Get the launcher max memory
     *
     * @return the launcher max memory
     */
    public final int getMaxMemory() {
        return cfg.getInt("Launch_Max", 2048);
    }
}
