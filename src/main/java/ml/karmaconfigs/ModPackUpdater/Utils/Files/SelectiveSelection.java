package ml.karmaconfigs.ModPackUpdater.Utils.Files;

import java.io.File;
import java.util.HashSet;

public final class SelectiveSelection {

    private final HashSet<File> selectedFiles = new HashSet<>();

    public final void addSelectedFile(File file) {
        selectedFiles.add(file);
    }

    public final boolean isSelectiveFile(File file) {
        return selectedFiles.contains(file);
    }
}
