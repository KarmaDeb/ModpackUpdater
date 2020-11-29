package ml.karmaconfigs.modpackupdater.files;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class MPUExt implements Serializable {

    private final CustomFile cf;

    /**
     * Initialize the .mpu file
     *
     * @param file the file
     */
    public MPUExt(final File file) throws NoMPUException {
        if (!file.getName().endsWith(".mpu"))
            throw new NoMPUException(file);

        cf = new CustomFile(file, true);
    }

    /**
     * Set the new modpack name
     *
     * @param _name the new name
     */
    public final void setName(final String _name) {
        cf.set(this.getClass().getName(), _name);
    }

    /**
     * Set the new modpack version
     *
     * @param _version the new version
     */
    public final void setVersion(final String _version) {
        cf.set(this.getClass().getName() + "_V", _version);
    }

    /**
     * Set the new modpack description
     *
     * @param _desc the new description
     */
    public final void setDescription(final List<String> _desc) {
        cf.set(this.getClass().getName() + "_DESC", _desc);
    }

    /**
     * Set the new modpack authors
     *
     * @param _authors the new authors
     */
    public final void setAuthors(final String... _authors) {
        StringBuilder builder = new StringBuilder();
        for (String author : _authors) {
            builder.append(author).append(",");
        }

        String final_authors = builder.substring(0, builder.length() - 1);
        cf.set(this.getClass().getName() + "_AUTHORS", final_authors);
    }

    /**
     * Set the new modpack json data
     * download url
     *
     * @param url the url
     */
    public final void setMainURL(final String url) {
        cf.set(this.getClass().getName() + "_DATA", url.endsWith("/") ? url : url + "/");
    }

    /**
     * Get the modpack name
     *
     * @return the modpack name
     */
    public final String getName() {
        return cf.getString(this.getClass().getName(), "NO_DEF");
    }

    /**
     * Get the MPU version
     *
     * @return the mpu version
     */
    public final String getVersion() {
        return cf.getString(this.getClass().getName() + "_V", "1.0.0");
    }

    /**
     * Get the description of the modpack
     *
     * @return the mpu description
     */
    public final String getDescription() {
        List<String> desc = cf.getStringList(this.getClass().getName() + "_DESC");

        if (desc == null)
            desc = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        for (String str : desc)
            builder.append(str.replace("<br>", "")).append("<br>");

        return builder.toString();
    }

    /**
     * Get the authors of the modpack
     *
     * @return the authors of the modpack
     */
    public final String[] getAuthors() {
        return cf.getString(this.getClass().getName() + "_AUTHORS", "UNKNOWN,AUTHOR").split(",");
    }

    /**
     * Get the json url/file path that contains
     * the downloads data
     *
     * @return a file/url instance pointing to a json
     * with the download data
     */
    public final String getMainURL() {
        return cf.getString(this.getClass().getName() + "_DATA", "NO_URL_FOUND");
    }
}
