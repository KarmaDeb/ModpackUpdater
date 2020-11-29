package ml.karmaconfigs.modpackupdater;

import ml.karmaconfigs.modpackupdater.files.MPUExt;
import ml.karmaconfigs.modpackupdater.files.memory.ClientMemory;
import ml.karmaconfigs.modpackupdater.utils.Cache;
import ml.karmaconfigs.modpackupdater.utils.Debug;
import ml.karmaconfigs.modpackupdater.utils.Text;
import ml.karmaconfigs.modpackupdater.utils.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public final class ModpackOpener {

    private static boolean initialized = false;

    private static final JFrame main_frame = new JFrame("Local modpack loader");
    private final static JFileChooser chooser = new JFileChooser();

    private final static Cache cache = new Cache();

    public final void initialize() {
        FileFilter mpu_ext = new FileNameExtensionFilter("Mod Pack Updater data file ( *.mpu )", "mpu", "MPU", "Mpu", "MPu", "mPu", "mPU", "mpU");

        if (!initialized) {
            main_frame.setIconImage(cache.getIco());

            chooser.setCurrentDirectory(new ClientMemory().getModpackDir());
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

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
                    File selected = chooser.getSelectedFile();
                    if (!selected.getName().endsWith(".mpu")) {
                        Debug.util.add(Text.util.create("Please choose an .mpu file to open", ml.karmaconfigs.modpackupdater.utils.Color.LIGHTGREEN, 12), true);
                    } else {
                        cache.saveMpuFile(selected);
                        if (cache.getMpuFile() != null) {
                            try {
                                main_frame.setVisible(false);
                                MPUExt ext = new MPUExt(cache.getMpuFile());
                                Debug.util.add(Text.util.create("Modpack " + ext.getName() + " detected, to unselect click \"Open\" again and click \"Cancel\"", ml.karmaconfigs.modpackupdater.utils.Color.LIGHTGREEN, 12), true);
                            } catch (Throwable ex) {
                                Text text = new Text(ex);
                                text.format(ml.karmaconfigs.modpackupdater.utils.Color.INDIANRED, 14);

                                Debug.util.add(text, true);
                            }
                        }
                    }
                } else {
                    if (et.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
                        if (cache.getMpuFile() != null) {
                            cache.saveMpuFile(null);
                            Debug.util.add(Text.util.create("Unloaded local modpack", ml.karmaconfigs.modpackupdater.utils.Color.LIGHTGREEN, 12), true);
                        }

                        main_frame.setVisible(false);
                    }
                }
            });

            initialized = true;
        } else {
            main_frame.setVisible(true);
            main_frame.toFront();
        }

        chooser.setFileFilter(mpu_ext);
    }
}
