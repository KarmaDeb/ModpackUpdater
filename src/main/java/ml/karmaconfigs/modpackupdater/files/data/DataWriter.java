package ml.karmaconfigs.modpackupdater.files.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import ml.karmaconfigs.modpackupdater.utils.Color;
import ml.karmaconfigs.modpackupdater.utils.Debug;
import ml.karmaconfigs.modpackupdater.utils.Text;
import ml.karmaconfigs.modpackupdater.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;

public final class DataWriter {

    private final HashSet<Data> datas;

    /**
     * Initialize the data writer
     *
     * @param _datas the data to write
     */
    public DataWriter(@NotNull final HashSet<Data> _datas) {
        datas = _datas;
    }

    /**
     * Write the data into the specified file
     *
     * @param prefix the json prefix
     * @param dest the target file
     */
    public final void write(String prefix, final File dest) {
        prefix = toASCII(prefix);

        try {
            if (!dest.getParentFile().exists() && dest.getParentFile().mkdirs())
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(dest.getParentFile()), Color.LIGHTGREEN, 12), false);
            if (!dest.exists() && dest.createNewFile())
                Debug.util.add(Text.util.create("Created file " + Utils.findPath(dest), Color.LIGHTGREEN, 12), false);

            if (Files.lines(dest.toPath()).count() <= 0)
                Files.write(dest.toPath(), "{}".getBytes());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject main = gson.fromJson(new FileReader(dest), JsonObject.class);

            main.add(prefix, new JsonObject());
            JsonObject data_inf = main.get(prefix).getAsJsonObject();

            for (Data data : datas) {
                String title = toASCII(data.getTitle());

                data_inf.add(title, new JsonObject());

                JsonObject inf = data_inf.get(title).getAsJsonObject();
                for (String path : data.getPaths()) {
                    String _path = toASCII(path);
                    String value = toASCII(data.getData(path));
                    inf.addProperty(_path, value);
                }
            }

            String json = gson.toJson(main);

            FileOutputStream out = new FileOutputStream(dest);
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (Throwable ex) {
            Text text = new Text(ex);
            text.format(Color.INDIANRED, 14);

            Debug.util.add(text, true);
        }
    }

    private String toASCII(Object text) {
        // strips off all non-ASCII characters
        text = text.toString().replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters
        text = text.toString().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // removes non-printable characters from Unicode
        text = text.toString().replaceAll("\\p{C}", "");

        return text.toString().trim();
    }
}
