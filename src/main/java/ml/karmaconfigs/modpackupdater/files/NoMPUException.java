package ml.karmaconfigs.modpackupdater.files;

import ml.karmaconfigs.modpackupdater.utils.Utils;

import java.io.File;

public final class NoMPUException extends Exception {

    public NoMPUException(final File file) {
        super("The file ( " + Utils.findPath(file) + " ) is not an .mpu file");
    }
}
