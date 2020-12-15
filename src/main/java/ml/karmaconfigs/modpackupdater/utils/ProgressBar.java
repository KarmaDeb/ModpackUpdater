package ml.karmaconfigs.modpackupdater.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public final class ProgressBar {

    private static boolean initialized = false;

    private final static JDialog bar_frame = new JDialog();

    private final static Dimension size = new Dimension(200, 40);

    private final static JLabel bar_info = new JLabel();
    private final static JProgressBar bar = new JProgressBar();

    private static int last_percentage = 0;

    static {
        if (!initialized) {
            bar_info.setText("Progress info");

            try {
                bar_frame.setIconImage(ImageIO.read(new URL("https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/img/logo.png")));
            } catch (Throwable ex) {
                bar_frame.setIconImage(null);
            }

            bar_frame.setMinimumSize(size);
            bar_frame.setMaximumSize(size);
            bar_frame.setPreferredSize(size);
            bar_frame.setSize(size);

            bar_frame.setUndecorated(true);

            bar_frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bar_info, bar);
            splitter.setDividerLocation(30);
            splitter.setEnabled(false);

            bar_frame.pack();
            bar_frame.setVisible(false);

            bar_frame.add(splitter);

            Utils.toCenter(bar_frame);
            bar_frame.setAlwaysOnTop(true);

            initialized = true;
        }
    }

    public final void show(final int progress) {
        if (last_percentage > progress) {
            bar_frame.setVisible(false);
            last_percentage = progress;
            return;
        }

        if (progress >= 99.9) {
            bar_frame.setVisible(false);
            bar.setValue(0);
            last_percentage = 0;
        } else {
            if (!bar_frame.isVisible()) {
                bar_frame.setVisible(true);
                Utils.toCenter(bar_frame);
            }
            bar.setValue(progress);
            last_percentage = progress;
        }
    }

    /**
     * Set the progress bar location
     *
     * @param component the component relative to
     */
    public final void setLocation(Component component) {
        bar_frame.setLocationRelativeTo(component);
    }

    /**
     * Set the progress bar info
     *
     * @param info the new info
     *             to show
     */
    public final void setLabel(final String info) {
        bar_info.setText(info);
    }

    /**
     * Refresh the log window UI
     */
    public final void refreshUI() {
        SwingUtilities.invokeLater(() -> SwingUtilities.updateComponentTreeUI(bar_frame));
    }
}
