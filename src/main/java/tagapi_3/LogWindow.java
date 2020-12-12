package tagapi_3;

import ml.karmaconfigs.modpackupdater.utils.Cache;
import ml.karmaconfigs.modpackupdater.utils.Utils;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public final class LogWindow implements ExecuteStreamHandler {

    private final JFrame output_frame = new JFrame("Console output");

    private final Dimension size = new Dimension(600, 400);

    private final JTextArea output = new JTextArea("");
    private final JScrollPane scrollable = new JScrollPane(output);

    private final JCheckBox auto_scroll = new JCheckBox("Auto scroll");

    public final DefaultExecutor initialize(final DefaultExecutor executor) {
        Cache cache = new Cache();
        output_frame.setIconImage(cache.getIco());

        auto_scroll.setSelected(Utils.c_memory.logScroll());

        auto_scroll.addActionListener(e -> Utils.c_memory.saveLogScroll(!Utils.c_memory.logScroll()));

        output_frame.setMinimumSize(size);
        output_frame.setMaximumSize(size);
        output_frame.setPreferredSize(size);
        output_frame.setSize(size);

        JSplitPane first_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, auto_scroll, new JPanel());
        JSplitPane final_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, first_split, scrollable);
        final_split.setEnabled(false);
        final_split.setEnabled(false);

        output_frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        output_frame.add(final_split);

        output_frame.pack();
        output_frame.setVisible(true);

        if (executor == null) {
            output_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }

        return executor;
    }

    public void setProcessInputStream(OutputStream outputStream) {}

    public void setProcessErrorStream(InputStream inputStream) throws IOException {
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while((line = br.readLine()) != null){
            output.append("[ERROR] " + line + "\n");
            if (auto_scroll.isSelected()) {
                scrollable.getVerticalScrollBar().setValue(scrollable.getVerticalScrollBar().getMaximum());
            }
        }
    }

    public void setProcessOutputStream(InputStream inputStream) throws IOException {
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while((line = br.readLine()) != null) {
            output.append(line + "\n");
            if (auto_scroll.isSelected()) {
                scrollable.getVerticalScrollBar().setValue(scrollable.getVerticalScrollBar().getMaximum());
            }
        }
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}
}
