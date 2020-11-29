package ml.karmaconfigs.modpackupdater;

import ml.karmaconfigs.modpackupdater.utils.Cache;
import ml.karmaconfigs.modpackupdater.utils.Color;
import ml.karmaconfigs.modpackupdater.utils.Debug;
import ml.karmaconfigs.modpackupdater.utils.Text;
import ml.karmaconfigs.modpackupdater.utils.Utils;

import javax.swing.*;
import java.awt.*;

public final class MinecraftChooser {

    private static boolean initialized = false;

    private static final JFrame main_frame = new JFrame("Minecraft directory chooser");
    private final static JFileChooser chooser = new JFileChooser();

    private final static Cache cache = new Cache();

    public final void initialize() {
        if (!initialized) {
            main_frame.setIconImage(cache.getIco());

            chooser.setCurrentDirectory(cache.getMcFolder());
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            main_frame.setPreferredSize(new Dimension(750, 600));
            main_frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            main_frame.pack();
            main_frame.add(chooser);
            main_frame.setResizable(false);

            SwingUtilities.invokeLater(() -> {
                SwingUtilities.updateComponentTreeUI(main_frame);
                for (Component component : main_frame.getComponents()) {
                    if (component != null) {
                        SwingUtilities.updateComponentTreeUI(component);
                    }
                }
            });

            main_frame.pack();
            main_frame.setVisible(true);

            Utils.toCenter(main_frame);

            chooser.addActionListener(et -> {
                if (et.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                    if (!chooser.getSelectedFile().equals(cache.getMcFolder())) {
                        cache.saveMcFolder(chooser.getSelectedFile());
                        Debug.util.add(Text.util.create("Changed minecraft directory to " + Utils.findPath(chooser.getSelectedFile()), Color.LIGHTGREEN, 12), true);
                        main_frame.setVisible(false);
                    }

                    chooser.setCurrentDirectory(chooser.getSelectedFile().getParentFile());
                } else {
                    if (et.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
                        main_frame.setVisible(false);
                    }
                }
            });

            initialized = true;
        } else {
            main_frame.setVisible(true);
            main_frame.toFront();
        }
    }
}
