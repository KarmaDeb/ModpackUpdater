package ml.karmaconfigs.modpackupdater.files;

import ml.karmaconfigs.modpackupdater.utils.Color;
import ml.karmaconfigs.modpackupdater.utils.Debug;
import ml.karmaconfigs.modpackupdater.utils.Text;
import ml.karmaconfigs.modpackupdater.utils.Utils;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.zip.DeflaterOutputStream;

public final class FileHasher {

    private final File file;

    /**
     * Initialize the file hasher
     *
     * @param _file the file to hash
     */
    public FileHasher(final File _file) {
        file = _file;
    }

    /**
     * Hash the file into a string
     *
     * @return the hashed file
     */
    public final File hashAndCompress(final File destDir) throws Throwable {
        if (!destDir.exists() && destDir.mkdirs())
            Debug.util.add(Text.util.create("Created directory " + Utils.findPath(destDir), Color.LIGHTGREEN, 12), false);


        byte[] fileBytes = Files.readAllBytes(file.toPath());
        MessageDigest hash = MessageDigest.getInstance("MD5");
        byte[] hashed = hash.digest(fileBytes);

        String hash_str = DatatypeConverter.printHexBinary(hashed).toLowerCase();
        File compressed = new File(destDir, hash_str);

        if (!compressed.exists())
            Files.write(compressed.toPath(), Collections.emptyList(), StandardCharsets.UTF_8);

        FileOutputStream out = new FileOutputStream(compressed);
        DeflaterOutputStream compressor = new DeflaterOutputStream(out);

        Files.copy(file.toPath(), compressor);

        compressor.close();
        out.close();
        return compressed;
    }

    public interface external {

        /**
         * Hash the string
         *
         * @param original the string
         * @return the hashed string
         * @throws Throwable if something goes wrong
         */
        static String hash(final String original) throws Throwable {
            MessageDigest hash = MessageDigest.getInstance("MD5");
            byte[] hashed = hash.digest(original.getBytes());

            return DatatypeConverter.printHexBinary(hashed).toLowerCase();
        }
    }
}
