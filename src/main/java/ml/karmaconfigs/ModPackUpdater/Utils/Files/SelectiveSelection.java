package ml.karmaconfigs.modpackupdater.utils.files;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public final class SelectiveSelection {

    private final HashSet<File> selectedFiles = new HashSet<>();

    public final void addSelectedFile(File file) {
        selectedFiles.add(file);
    }

    public final void addSelectedFile(Collection<File> files) {
        selectedFiles.addAll(files);
    }

    public final boolean isSelectiveFile(File file) {
        return selectedFiles.contains(file);
    }

    public final boolean isEmpty() {
        return selectedFiles.isEmpty();
    }
}
