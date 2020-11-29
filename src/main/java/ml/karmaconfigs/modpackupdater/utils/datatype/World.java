package ml.karmaconfigs.modpackupdater.utils.datatype;

import java.util.HashMap;

public final class World extends Extended_Resource {

    private final String name;
    private final String hash;
    private final HashMap<String, String> hashes;
    private final long size;
    private final long original;

    public World(final String _name, final String _hash, final HashMap<String, String> _hashes, final long _size, final long _original) {
        name = _name;
        hash = _hash;
        hashes = _hashes;
        size = _size;
        original = _original;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public HashMap<String, String> getHashPathMap() {
        return hashes;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getRealSize() {
        return original;
    }
}
