package ml.karmaconfigs.modpackupdater.files;

import ml.karmaconfigs.modpackupdater.utils.Color;
import ml.karmaconfigs.modpackupdater.utils.Debug;
import ml.karmaconfigs.modpackupdater.utils.Text;
import ml.karmaconfigs.modpackupdater.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@SuppressWarnings("unused")
public final class CustomFile implements Serializable {

    private final File file;

    public CustomFile(File file, boolean create) {
        this.file = file;

        if (create) {
            create();
        }
    }

    private boolean isOpenList(String line, String path) {
        return line.equals("[LIST=" + path + "]");
    }

    private boolean isCloseList(String line, String path) {
        return line.equals("[/LIST=" + path + "]");
    }

    /**
     * Create the file and
     * directories
     */
    public final void create() {
        if (!file.getParentFile().exists()) {
            if (file.getParentFile().mkdirs()) {
                Debug.util.add(Text.util.create("Created directory " + Utils.findPath(file.getParentFile()), Color.LIGHTGREEN, 12), false);
            } else {
                try {
                    Files.createDirectory(file.toPath());
                    Debug.util.add(Text.util.create("Created directory " + Utils.findPath(file.getParentFile()), Color.LIGHTGREEN, 12), false);
                } catch (Throwable ex) {
                    Text text = new Text(ex);
                    text.format(Color.INDIANRED, 14);

                    Debug.util.add(text, true);
                }
            }
        }
        if (!file.exists()) {
            try {
                try {
                    if (file.createNewFile()) {
                        Debug.util.add(Text.util.create("Created file " + Utils.findPath(file), Color.LIGHTGREEN, 12), false);
                    } else {
                        Files.createFile(file.toPath());
                    }
                } catch (Throwable ex) {
                    Text text = new Text(ex);
                    text.format(Color.INDIANRED, 14);

                    Debug.util.add(text, true);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write a value, with no path
     *
     * @param value the value
     * @since 2.0 - SNAPSHOT this have been un-deprecated
     * since it has an utility, like creating comments or
     * values that doesn't need a path
     */
    public final void set(Object value) {
        if (!exists()) {
            create();
        }

        byte[] toByte = value.toString().getBytes(StandardCharsets.UTF_8);
        String val = new String(toByte, StandardCharsets.UTF_8);

        InputStream flInput = null;
        InputStreamReader flReader = null;
        BufferedReader reader = null;
        try {
            flInput = new FileInputStream(file);
            flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
            reader = new BufferedReader(flReader);

            List<Object> sets = new ArrayList<>();

            boolean alreadySet = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals(value.toString())) {
                    sets.add(line);
                } else {
                    alreadySet = true;
                    sets.add(val);
                }
            }

            if (!alreadySet) {
                sets.add(val);
            }

            FileOutputStream out = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            for (Object str : sets) {
                writer.write(str + "\n");
            }

            writer.flush();
            writer.close();
            out.flush();
            out.close();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            closeStreams(flInput, flReader, reader);
        }
    }

    /**
     * Write a value into the file
     *
     * @param path  the path
     * @param value the value
     */
    public final void set(String path, Object value) {
        if (!exists()) {
            create();
        }

        byte[] toByte = value.toString().getBytes(StandardCharsets.UTF_8);
        String val = new String(toByte, StandardCharsets.UTF_8);

        InputStream flInput = null;
        InputStreamReader flReader = null;
        BufferedReader reader = null;
        try {
            flInput = new FileInputStream(file);
            flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
            reader = new BufferedReader(flReader);

            List<Object> sets = new ArrayList<>();

            boolean alreadySet = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String currentPath = line.split(":")[0];

                    if (!currentPath.equals(path)) {
                        sets.add(line);
                    } else {
                        alreadySet = true;
                        sets.add(path + ": " + val);
                    }
                }
            }

            if (!alreadySet) {
                sets.add(path + ": " + val);
            }

            FileOutputStream out = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            for (Object str : sets) {
                writer.write(str + "\n");
            }

            writer.flush();
            writer.close();
            out.flush();
            out.close();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            closeStreams(flInput, flReader, reader);
        }
    }

    /**
     * Write a list into the file
     *
     * @param path   the path
     * @param list the values
     */
    public final void set(String path, List<?> list) {
        if (!exists()) {
            create();
        }

        InputStream flInput = null;
        InputStreamReader flReader = null;
        BufferedReader reader = null;
        try {
            flInput = new FileInputStream(file);
            flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
            reader = new BufferedReader(flReader);

            List<String> sets = new ArrayList<>();

            boolean adding = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("[LIST=" + path + "]")) {
                    adding = false;
                }
                if (!adding) {
                    if (line.equals("[/LIST=" + path + "]")) {
                        adding = true;
                    }
                }
                if (adding) {
                    if (!line.equals("[LIST=" + path + "]") && !line.equals("[/LIST=" + path + "]")) {
                        sets.add(line);
                    }
                }
            }

            sets.add("[LIST=" + path + "]");
            for (Object val : list) {
                sets.add(val.toString());
            }
            sets.add("[/LIST=" + path + "]");

            FileOutputStream out = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            for (Object str : sets) {
                writer.write(str + "\n");
            }

            writer.flush();
            writer.close();
            out.flush();
            out.close();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            closeStreams(flInput, flReader, reader);
        }
    }

    /**
     * Save the current modpack instance
     *
     * @param path the path to the instance value
     * @param classInstance the instance to save
     */
    public final void saveInstance(final String path, final Object classInstance) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(classInstance);
            oos.close();
            String inst = Base64.getEncoder().encodeToString(out.toByteArray());

            set(path, inst);
        } catch (Throwable ex) {
            Text text = new Text(ex);
            text.format(Color.INDIANRED, 14);

            Debug.util.add(text, true);
        }
    }

    /**
     * Unset the value and the path
     *
     * @param path the path
     */
    public final void unset(String path) {
        if (!exists()) {
            create();
        }

        boolean isList = getList(path) != null;

        InputStream flInput = null;
        InputStreamReader flReader = null;
        BufferedReader reader = null;
        try {
            flInput = new FileInputStream(file);
            flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
            reader = new BufferedReader(flReader);

            List<Object> sets = new ArrayList<>();

            String line;
            if (!isList) {
                while ((line = reader.readLine()) != null) {
                    if (!line.replace(":", "").startsWith(path)) {
                        sets.add(line);
                    }
                }
            } else {
                boolean removing = false;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("[LIST=" + path + "]")) {
                        removing = true;
                    }
                    if (removing) {
                        if (line.equals("[/LIST=" + path + "]")) {
                            removing = false;
                        }
                    }
                    if (!removing) {
                        sets.add(line);
                    }
                }
            }

            FileWriter writer = new FileWriter(file);
            for (Object str : sets) {
                writer.write(str + "\n");
            }

            writer.flush();
            writer.close();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            closeStreams(flInput, flReader, reader);
        }
    }

    /**
     * Get a value from a path
     *
     * @param path the path
     * @param def  the default value
     * @return an object
     */
    public final Object get(String path, Object def) {
        Object val = def;

        if (exists()) {
            InputStream flInput = null;
            InputStreamReader flReader = null;
            BufferedReader reader = null;
            try {
                flInput = new FileInputStream(file);
                flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
                reader = new BufferedReader(flReader);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.split(":")[0] != null) {
                        String actualPath = line.split(":")[0];
                        if (actualPath.equals(path)) {
                            val = line.replace(actualPath + ": ", "");
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                closeStreams(flInput, flReader, reader);
            }
        }

        return val;
    }

    /**
     * Get a String from the path
     *
     * @param path the path
     * @param def  the default value
     * @return a String
     */
    public final String getString(String path, String def) {
        String val = def;

        if (exists()) {
            InputStream flInput = null;
            InputStreamReader flReader = null;
            BufferedReader reader = null;
            try {
                flInput = new FileInputStream(file);
                flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
                reader = new BufferedReader(flReader);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.split(":")[0] != null) {
                        String actualPath = line.split(":")[0];
                        if (actualPath.equals(path)) {
                            val = line.replace(actualPath + ": ", "");
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                closeStreams(flInput, flReader, reader);
            }
        }

        return val;
    }

    /**
     * Get a list from the path
     *
     * @param path the path
     * @return a list
     */
    public final List<?> getList(String path) {
        if (exists()) {
            InputStream flInput = null;
            InputStreamReader flReader = null;
            BufferedReader reader = null;
            try {
                flInput = new FileInputStream(file);
                flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
                reader = new BufferedReader(flReader);

                List<Object> values = new ArrayList<>();

                boolean adding = false;
                Object line;
                while ((line = reader.readLine()) != null) {
                    if (isOpenList(line.toString(), path)) {
                        adding = true;
                    }
                    if (isCloseList(line.toString(), path)) {
                        adding = false;
                    }
                    if (adding) {
                        if (!isOpenList(line.toString(), path)) {
                            values.add(line);
                        }
                    }
                }

                return values;
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                closeStreams(flInput, flReader, reader);
            }
        }

        return null;
    }

    /**
     * Get a list of strings
     *
     * @param path the path
     * @return a list of strings
     */
    @Nullable
    public final List<String> getStringList(String path) {
        List<?> originalList = getList(path);
        if (originalList != null) {
            List<String> val = new ArrayList<>();

            for (Object value : originalList) {
                val.add(value.toString());
            }

            return val;
        }

        return null;
    }

    /**
     * Get a Boolean from the path
     *
     * @param path the path
     * @param def  the default value
     * @return a boolean
     */
    public final boolean getBoolean(String path, boolean def) {
        boolean val = def;

        if (exists()) {
            InputStream flInput = null;
            InputStreamReader flReader = null;
            BufferedReader reader = null;
            try {
                flInput = new FileInputStream(file);
                flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
                reader = new BufferedReader(flReader);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.split(":")[0] != null) {
                        String actualPath = line.split(":")[0];
                        if (actualPath.equals(path)) {
                            val = Boolean.parseBoolean(line.replace(actualPath + ": ", ""));
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                closeStreams(flInput, flReader, reader);
            }
        }

        return val;
    }

    /**
     * Get an integer from the path
     *
     * @param path the path
     * @param def  the default value
     * @return an integer
     */
    public final int getInt(String path, int def) {
        int val = def;

        if (exists()) {
            InputStream flInput = null;
            InputStreamReader flReader = null;
            BufferedReader reader = null;
            try {
                flInput = new FileInputStream(file);
                flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
                reader = new BufferedReader(flReader);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.split(":")[0] != null) {
                        String actualPath = line.split(":")[0];
                        if (actualPath.equals(path)) {
                            val = Integer.parseInt(line.replace(actualPath + ": ", ""));
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                closeStreams(flInput, flReader, reader);
            }
        }

        return val;
    }

    /**
     * Get a String from the path
     *
     * @param path the path
     * @param def  the default value
     * @return a String
     */
    public final double getDouble(String path, double def) {
        double val = def;

        if (exists()) {
            InputStream flInput = null;
            InputStreamReader flReader = null;
            BufferedReader reader = null;
            try {
                flInput = new FileInputStream(file);
                flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
                reader = new BufferedReader(flReader);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.split(":")[0] != null) {
                        String actualPath = line.split(":")[0];
                        if (actualPath.equals(path)) {
                            val = Double.parseDouble(line.replace(actualPath + ": ", ""));
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                closeStreams(flInput, flReader, reader);
            }
        }

        return val;
    }

    /**
     * Get a String from the path
     *
     * @param path the path
     * @param def  the default value
     * @return a String
     */
    public final long getLong(String path, long def) {
        long val = def;

        if (exists()) {
            InputStream flInput = null;
            InputStreamReader flReader = null;
            BufferedReader reader = null;
            try {
                flInput = new FileInputStream(file);
                flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
                reader = new BufferedReader(flReader);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.split(":")[0] != null) {
                        String actualPath = line.split(":")[0];
                        if (actualPath.equals(path)) {
                            val = Long.parseLong(line.replace(actualPath + ": ", ""));
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                closeStreams(flInput, flReader, reader);
            }
        }

        return val;
    }

    /**
     * Get the last modpack extension instance
     *
     * @param path the saved path of the instance
     * @param def the default instance
     * @return a modpack extension instance
     */
    public final Object getInstance(String path, Object def) {
        Object val = def;
        try (FileInputStream flIn = new FileInputStream(file); InputStreamReader flReader = new InputStreamReader(flIn, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(flReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String actualPath = line.split(":")[0];
                    if (actualPath.equals(path)) {
                        byte [] data = Base64.getDecoder().decode(line.replace(actualPath + ": ", ""));
                        ObjectInputStream ois = new ObjectInputStream(
                                new ByteArrayInputStream(  data ) );
                        Object o  = ois.readObject();
                        ois.close();
                        val = o;
                        break;
                    }
                }
            }
        } catch (Throwable ex) {
            Text text = new Text(ex);
            text.format(Color.INDIANRED, 14);

            Debug.util.add(text, true);
        }
        return val;
    }

    /**
     * Get the custom file
     *
     * @return a file
     */
    public final File getFile() {
        return file;
    }

    /**
     * Check if the file exists
     *
     * @return a boolean
     */
    public final boolean exists() {
        return file.exists();
    }

    /**
     * Read the file completely
     *
     * @return the complete file as string
     */
    @Override
    public final String toString() {
        String val = "";

        if (exists()) {
            InputStream flInput = null;
            InputStreamReader flReader = null;
            BufferedReader reader = null;
            try {
                flInput = new FileInputStream(file);
                flReader = new InputStreamReader(flInput, StandardCharsets.UTF_8);
                reader = new BufferedReader(flReader);

                StringBuilder val_builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    val_builder.append(line);
                }

                val = val_builder.toString();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                closeStreams(flInput, flReader, reader);
            }
        }

        return val;
    }

    /**
     * Close all the streams to allow file-managing
     * out of plugin
     *
     * @param in the input stream file
     * @param inReader the input stream reader of the in file
     * @param reader the inReader file reader
     */
    private void closeStreams(InputStream in, InputStreamReader inReader, BufferedReader reader) {
        try {
            if (in != null)
                in.close();
            if (inReader != null)
                inReader.close();
            if (reader != null)
                reader.close();
        } catch (Throwable ignored) {}
    }
}