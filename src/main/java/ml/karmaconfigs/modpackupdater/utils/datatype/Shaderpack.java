package ml.karmaconfigs.modpackupdater.utils.datatype;

public final class Shaderpack extends Resource {

    private final String name;
    private final String hash;
    private final long size;
    private final long original;

    public Shaderpack(final String _name, final String _hash, final long _size, final long _original) {
        name = _name;
        hash = _hash;
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
    public long getSize() {
        return size;
    }

    @Override
    public long getRealSize() {
        return original;
    }
}
