package ml.karmaconfigs.modpackupdater.utils.datatype;

public abstract class Resource {

    public abstract String getName();
    public abstract String getHash();
    public abstract long getSize();
    public abstract long getRealSize();
}
