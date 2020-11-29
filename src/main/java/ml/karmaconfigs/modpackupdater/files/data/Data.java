package ml.karmaconfigs.modpackupdater.files.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;

public final class Data {

    private final HashMap<String, Object> values = new HashMap<>();
    private final String title;

    /**
     * Initialize the data info
     *
     * @param _title the title of the data
     */
    public Data(@NotNull final String _title) {
        title = _title;
    }

    /**
     * Add a data info to the values table
     *
     * @param path the path to the value
     * @param value the value
     */
    public final void addData(final String path, final Object value) {
        values.put(path, value);
    }

    /**
     * Get the data title
     *
     * @return the data title
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Get the value assigned to the specified path
     *
     * @param path the path
     * @return the value
     */
    @Nullable
    public final Object getData(final String path) {
        return values.getOrDefault(path, null);
    }

    /**
     * Get all the paths of the table
     *
     * @return a set of values
     */
    public final Set<String> getPaths() {
        return values.keySet();
    }
}
