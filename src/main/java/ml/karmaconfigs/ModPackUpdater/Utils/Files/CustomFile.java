package ml.karmaconfigs.ModPackUpdater.Utils.Files;

import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class CustomFile {

    private final Utils utils = new Utils();
    private final File file;

    public CustomFile(File file, boolean create) {
        this.file = file;

        if (create) {
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        utils.setDebug(utils.rgbColor("Created file " + file.getName(), 255, 255, 255), true);
                    }
                } catch (Throwable e) {
                    utils.log(e);
                }
            }
        }
    }

    private boolean isOpenList(String line, String path) {
        return line.equals("[LIST=" + path + "]");
    }

    private boolean isCloseList(String line, String path) {
        return line.equals("[/LIST=" + path + "]");
    }

    public final void write(Object value) {
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            List<String> sets = new ArrayList<>();
            boolean alreadySet = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals(value.toString())) {
                    sets.add(line);
                    continue;
                }
                sets.add(value.toString());
                alreadySet = true;
            }
            if (!alreadySet)
                sets.add(value.toString());
            FileWriter writer = new FileWriter(file);
            for (String str : sets)
                writer.write(str + "\n");
            writer.flush();
            writer.close();
        } catch (Throwable e) {
             utils.log(e);
        }
    }

    public final void write(String path, Object value) {
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            ArrayList<String> sets = new ArrayList<>();
            boolean alreadySet = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String currentPath = line.split(":")[0];
                    if (!currentPath.equals(path)) {
                        sets.add(line);
                        continue;
                    }
                    alreadySet = true;
                    sets.add(path + ": " + value);
                }
            }
            if (!alreadySet)
                sets.add(path + ": " + value);
            FileWriter writer = new FileWriter(file);
            for (Object str : sets)
                writer.write(str + "\n");
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            utils.log(e);
        }
    }

    public final void write(String path, List<?> values) {
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            List<String> sets = new ArrayList<>();
            boolean adding = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("[LIST=" + path + "]"))
                    adding = false;
                if (!adding &&
                        line.equals("[/LIST=" + path + "]"))
                    adding = true;
                if (adding &&
                        !line.equals("[LIST=" + path + "]") && !line.equals("[/LIST=" + path + "]"))
                    sets.add(line);
            }
            sets.add("[LIST=" + path + "]");
            for (Object val : values)
                sets.add(val.toString());
            sets.add("[/LIST=" + path + "]");
            FileWriter writer = new FileWriter(file);
            for (String str : sets)
                writer.write(str + "\n");
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            utils.log(e);
        }
    }

    public final Object get(String path, Object def) {
        Object val = def;
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String actualPath = line.split(":")[0];
                    if (actualPath.equals(path))
                        val = line.replace(actualPath + ": ", "");
                }
            }
        } catch (Throwable e) {
            utils.log(e);
        }
        return val;
    }

    public final String getString(String path, String def) {
        String val = def;
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String actualPath = line.split(":")[0];
                    if (actualPath.equals(path))
                        val = line.replace(actualPath + ": ", "");
                }
            }
        } catch (Throwable e) {
            utils.log(e);
        }
        return val;
    }

    public final List<Object> getList(String path) {
        List<Object> values = new ArrayList<>(Collections.emptyList());
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            boolean adding = false;
            Object line;
            while ((line = reader.readLine()) != null) {
                if (isOpenList(line.toString(), path))
                    adding = true;
                if (isCloseList(line.toString(), path))
                    adding = false;
                if (adding &&
                        !isOpenList(line.toString(), path))
                    values.add(line);
            }
        } catch (Throwable e) {
            utils.log(e);
        }
        return values;
    }

    public final List<String> getStringList(String path) {
        List<String> val = new ArrayList<>();
        for (Object value : getList(path))
            val.add(value.toString());
        return val;
    }

    public final boolean getBoolean(String path, boolean def) {
        boolean val = def;
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String actualPath = line.split(":")[0];
                    if (actualPath.equals(path))
                        val = Boolean.parseBoolean(line.replace(actualPath + ": ", ""));
                }
            }
        } catch (Throwable e) {
            utils.log(e);
        }
        return val;
    }

    public final boolean isSet(String path) {
        if (get(path, null) == null) {
            return getList(path).isEmpty();
        } else {
            return true;
        }
    }

    public final int getInt(String path, int def) {
        int val = def;
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String actualPath = line.split(":")[0];
                    if (actualPath.equals(path))
                        val = Integer.parseInt(line.replace(actualPath + ": ", ""));
                }
            }
        } catch (Throwable e) {
            utils.log(e);
        }
        return val;
    }

    public final double getDouble(String path, double def) {
        double val = def;
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String actualPath = line.split(":")[0];
                    if (actualPath.equals(path))
                        val = Double.parseDouble(line.replace(actualPath + ": ", ""));
                }
            }
        } catch (Throwable e) {
            utils.log(e);
        }
        return val;
    }

    public final long getLong(String path, long def) {
        long val = def;
        try {
            InputStreamReader flReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(flReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0] != null) {
                    String actualPath = line.split(":")[0];
                    if (actualPath.equals(path))
                        val = Long.parseLong(line.replace(actualPath + ": ", ""));
                }
            }
        } catch (Throwable e) {
            utils.log(e);
        }
        return val;
    }
}