package ml.karmaconfigs.modpackupdater.files.memory;

import ml.karmaconfigs.modpackupdater.files.CustomFile;
import ml.karmaconfigs.modpackupdater.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreatorMemory implements Utils {

    private final static CustomFile cfg = new CustomFile(new File(getUpdaterDir, "creator.config"), true);

    public final void saveUrl(final String url) {
        cfg.set("Modpack_Url", url);
    }

    public final void saveName(final String name) {
        cfg.set("Modpack_Name", name);
    }

    public final void saveVersion(final String version) {
        cfg.set("Modpack_Version", version);
    }

    public final void saveAuthors(final String... authors) {
        List<String> list = new ArrayList<>(Arrays.asList(authors));

        cfg.set("Modpack_Authors", list);
    }

    public final void saveDescription(final List<String> description) {
        cfg.set("Modpack_Description", description);
    }

    public final String getUrl() {
        return cfg.getString("Modpack_Url", "https://example.org/");
    }

    public final String getName() {
        return cfg.getString("Modpack_Name", "");
    }

    public final String getVersion() {
        return cfg.getString("Modpack_Version", "");
    }

    public final List<String> getDescription() {
        return cfg.getStringList("Modpack_Description");
    }

    public final List<String> getAuthors() {
        return cfg.getStringList("Modpack_Authors");
    }
}
