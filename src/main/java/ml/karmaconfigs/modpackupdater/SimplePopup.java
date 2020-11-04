package ml.karmaconfigs.modpackupdater;

import ml.karmaconfigs.modpackupdater.utils.files.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class SimplePopup {

    private final static Config config = new Config();
    private static JFrame frame = null;

    public final void initialize() {
        if (frame == null) {
            frame = new JFrame("Version selector");
            frame.setIconImage(MainFrame.frame.getIconImage());

            JButton simple_version = new JButton("Simple version");
            JButton complex_version = new JButton("Complex version");

            JCheckBox keep = new JCheckBox("<html>Don't show again this<br>Can be changed by clicking \"Change version\" button</html>");

            JSplitPane buttons_splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, simple_version, complex_version);
            buttons_splitter.setDividerLocation(300);
            buttons_splitter.setLastDividerLocation(300);
            buttons_splitter.setEnabled(false);

            JSplitPane check_splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttons_splitter, keep);
            check_splitter.setEnabled(false);

            frame.add(check_splitter);

            frame.setResizable(false);
            frame.setMinimumSize(new Dimension(600, 125));
            frame.setMaximumSize(new Dimension(600, 125));
            frame.setPreferredSize(new Dimension(600, 125));
            frame.setSize(new Dimension(600, 125));
            frame.pack();

            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (!MainFrame.frame.isVisible() && !SimpleFrame.frame.isVisible()) {
                        System.exit(0);
                    }
                }
            });

            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

            keep.setSelected(config.skipSelector());

            simple_version.addActionListener(e -> {
                if (!SimpleFrame.active) {
                    SimpleFrame.active = true;
                    MainFrame.active = false;
                    MainFrame.frame.setVisible(false);
                }
                SimpleFrame.frame.setVisible(true);

                if (keep.isSelected()) {
                    config.saveLaunchType(true);
                }

                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            });

            complex_version.addActionListener(e -> {
                if (!MainFrame.active) {
                    SimpleFrame.active = false;
                    MainFrame.active = true;
                    SimpleFrame.frame.setVisible(false);
                }
                MainFrame.frame.setVisible(true);

                if (keep.isSelected()) {
                    config.saveLaunchType(false);
                }

                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            });

            keep.addActionListener(e -> config.saveSkipSelector(keep.isSelected()));
        }

        frame.setVisible(true);
    }
}
