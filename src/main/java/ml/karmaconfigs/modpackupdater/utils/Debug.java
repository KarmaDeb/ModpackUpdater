package ml.karmaconfigs.modpackupdater.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public final class Debug implements Utils {

    private final static JEditorPane debug_label = new JEditorPane();
    private final static JScrollPane debug_scroll = new JScrollPane(debug_label);

    private final static HashMap<String, String> debug_timestamp = new HashMap<>();
    private final static HashMap<String, String> debug_unformatted = new HashMap<>();

    static {
        debug_label.setContentType("text/html");
        debug_label.setEditable(false);
    }

    /**
     * Get the timestamp of the specified text line
     *
     * @param line the line
     * @return a timestamp
     */
    @Nullable
    public final String getTimeStamp(final String line) {
        return debug_timestamp.getOrDefault(line, null);
    }

    /**
     * Get the scrollable panel of the debug
     * so it can be added to any frame
     *
     * @return the debug label scrollable pane
     */
    @NotNull
    public final JScrollPane getDebugScrollable() {
        return debug_scroll;
    }

    /**
     * Get the editor panel of the debug
     * so it can be modified
     *
     * @return the debug editor label
     */
    @NotNull
    public final JEditorPane getEditor() {
        return debug_label;
    }

    public interface util {

        /**
         * Add a new line to the debug
         * label
         *
         * @param text the new line text
         * @param space insert a double space
         */
        static void add(@NotNull final Text text, final boolean space) {
            //debug_label.setText("<html>" + text.getText(true) + "</html>");
            try {
                Cache cache = new Cache();

                if (!cache.getDebug().equals(text.getText(false))) {
                    cache.saveDebug(text.getText(false));
                    Calendar calendar = GregorianCalendar.getInstance();

                    String timestamp = "[" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "] ";
                    String old = (debug_label.getText() != null ? debug_label.getText() : "")
                            .replace("<html>", "").replace("</html>", "")
                            .replace("<head>", "").replace("</head>", "")
                            .replace("<body>", "").replace("</body>", "");
                    String spacer = space && !old.endsWith("<br>") ? "<br><br>" : "<br>";
                    String str = old + spacer + text.getText(true);
                    debug_label.setText("<html>" + str + "</html>");

                    debug_timestamp.put(text.getText(true), timestamp);
                    debug_unformatted.put(text.getText(true), text.getText(false));

                    if (c_memory.autoScroll()) {
                        SwingUtilities.invokeLater(() -> debug_scroll.getVerticalScrollBar().setValue(debug_scroll.getVerticalScrollBar().getMaximum()));
                    }
                }
            } catch (Throwable e) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        add(text, space);
                    }
                }, TimeUnit.SECONDS.toMillis(3));
            }
        }

        static void add(@NotNull final String text, final boolean space) {
            Cache cache = new Cache();

            if (!cache.getDebug().equals(text)) {
                cache.saveDebug(text);
                Calendar calendar = GregorianCalendar.getInstance();

                String timestamp = "[" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "] ";
                String old = (debug_label.getText() != null ? debug_label.getText() : "")
                        .replace("<html>", "").replace("</html>", "")
                        .replace("<head>", "").replace("</head>", "")
                        .replace("<body>", "").replace("</body>", "");
                String spacer = space && !old.endsWith("<br>") ? "<br><br>" : "<br>";
                String str = old + spacer + text;
                debug_label.setText("<html>" + str + "</html>");

                debug_timestamp.put(text, timestamp);
                debug_unformatted.put(text, text);

                if (c_memory.autoScroll()) {
                    SwingUtilities.invokeLater(() -> debug_scroll.getVerticalScrollBar().setValue(debug_scroll.getVerticalScrollBar().getMaximum()));
                }
            }
        }

        /**
         * Export the debug data
         */
        static void export() throws Throwable {
            Calendar calendar = GregorianCalendar.getInstance();

            String file_format = calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);
            String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);

            if (!logsDir.exists())
                if (logsDir.mkdirs()) {
                    Text text = new Text("Created directory " + Utils.findPath(logsDir));
                    text.format(Color.LIGHTGREEN, 12);
                }

            File debug_file = new File(logsDir, file_format + "_" + time.replace(":", "-") + ".md");

            if (!debug_file.exists() && debug_file.createNewFile()) {
                Text text = new Text("Created file " + Utils.findPath(debug_file));
                text.format(Color.LIGHTGREEN, 12);

                add(text, true);
            } else {
                Text text = new Text("Couldn't create file " + Utils.findPath(debug_file));
                text.format(Color.INDIANRED, 12);

                add(text, true);
            }

            FileWriter writer = new FileWriter(debug_file);

            String os = System.getProperty("os.name");
            os = os.substring(0, 1).toUpperCase() + os.substring(1).toLowerCase();

            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

            String realArch = arch != null && arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            String jvm_arch = System.getProperty("os.arch");

            String version = System.getProperty("os.version");

            String model = System.getProperty("sun.arch.data.model");

            writer.write("# ModpackUpdater debug file generated at " + time + "<br>\n");
            writer.write("### System info<br>\n");
            writer.write("Operative system: " + os + "<br>\n");
            writer.write("OS version: " + version.replace(jvm_arch, "") + "<br>\n");
            writer.write("JVM architecture: " + jvm_arch + "<br>\n");
            writer.write("Architecture: " + realArch + "<br>\n");
            writer.write("Model: " + model + "<br><br>\n\n");

            for (String debug_line : debug_label.getText().split("<br>")) {
                if (!debug_line.replaceAll("\\s", "").isEmpty()) {
                    Debug debug = new Debug();
                    String timestamp = debug.getTimeStamp(debug_line);

                    String unformatted = debug_unformatted.getOrDefault(debug_line, null);

                    if (timestamp != null && !timestamp.isEmpty() && unformatted != null && !unformatted.isEmpty())
                        writer.write(timestamp + unformatted + "<br>\n");
                } else {
                    writer.write("<br>\n");
                }
            }

            writer.write("### End of debug");

            writer.flush();
            writer.close();
        }
    }
}
