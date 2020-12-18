package ml.karmaconfigs.modpackupdater.manager;

import com.therandomlabs.curseapi.file.CurseDependency;
import com.therandomlabs.curseapi.file.CurseDependencyType;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.project.CurseProject;
import ml.karmaconfigs.modpackupdater.utils.*;
import ml.karmaconfigs.modpackupdater.utils.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DownloadVersionDialog {

    private final JFrame dialog = new JFrame();
    private final JComboBox<String> versions_box = new JComboBox<>();

    public final void show(CurseProject project) throws Throwable {
        ArrayList<CurseFile> files = new ArrayList<>(project.files());
        Collections.reverse(files);

        versions_box.removeAllItems();

        List<String> versions = new ArrayList<>();
        for (CurseFile file : project.files()) {
            for (String version : file.gameVersionStrings()) {
                version = version.replaceAll("[aA-zZ]", "").replace("-", "");
                if (!version.isEmpty()) {
                    if (!versions.contains(version)) {
                        versions.add(version);
                    }
                }
            }
        }
        versions.sort(VersionNumberComparator.getInstance());
        Collections.reverse(versions);
        versions.forEach(versions_box::addItem);

        Cache cache = new Cache();

        dialog.setIconImage(cache.getIco());

        JButton accept = new JButton("Download");
        JButton cancel = new JButton("Cancel");

        accept.setToolTipText("Download/Update the mod " + project.name() + "\n" +
                "( Will download dependencies if available )");
        cancel.setToolTipText("Cancel the mod download and\n" +
                "go back to browser");

        JSplitPane acp_can = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, accept, cancel);
        JSplitPane ver_butt = new JSplitPane(JSplitPane.VERTICAL_SPLIT, versions_box, acp_can);

        acp_can.setDividerLocation(250);
        acp_can.setEnabled(false);
        ver_butt.setEnabled(false);

        dialog.add(ver_butt);

        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        accept.addActionListener(e -> {
            String selected_version = versions_box.getItemAt(versions_box.getSelectedIndex());
            if (selected_version != null) {
                Collections.reverse(files);
                for (CurseFile file : files) {
                    if (contains(file, selected_version)) {
                        try {
                            File mod_dir = new File(cache.getMcFolder() + "/mods/" + selected_version);
                            if (!mod_dir.exists())
                                Files.createDirectories(mod_dir.toPath());

                            if (alreadyDownloaded(file, mod_dir)) {
                                deleteSimilar(file, mod_dir);
                            }

                            file.downloadToDirectory(mod_dir.toPath());
                            downloadDependencies(project, file, selected_version, mod_dir);
                            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        } finally {
                            Debug.util.add(Text.util.create("Downloaded mod " + project.name(), Color.LIGHTGREEN, 12), true);
                        }
                        break;
                    }
                }
            }
        });

        cancel.addActionListener(e -> dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING)));

        dialog.setAlwaysOnTop(true);

        Dimension size = new Dimension(500, 100);
        dialog.setMinimumSize(size);
        dialog.setMaximumSize(size);
        dialog.setPreferredSize(size);
        dialog.setSize(size);
        dialog.setResizable(false);

        Utils.toCenter(dialog);

        dialog.setTitle(project.name());

        dialog.pack();
        dialog.setVisible(true);
    }

    private void downloadDependencies(CurseProject main_project, CurseFile file, final String selected_version, final File mod_dir) throws Throwable {
        for (CurseDependency dependency : file.dependencies()) {
            if (dependency.type().equals(CurseDependencyType.REQUIRED)) {
                for (CurseFile dependency_file : dependency.project().files()) {
                    if (contains(dependency_file, selected_version)) {
                        try {
                            dependency_file.downloadToDirectory(mod_dir.toPath());

                            downloadDependencies(main_project, dependency_file, selected_version, mod_dir);
                            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
                        } catch (Throwable ex) {
                            Text text = new Text(ex);
                            text.format(Color.INDIANRED, 14);

                            Debug.util.add(text, true);
                        } finally {
                            Debug.util.add(Text.util.create("Downloaded mod " + dependency.project().name() + " as dependency of " + main_project.name(), Color.LIGHTGREEN, 12), true);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void deleteSimilar(final CurseFile download_file, final File dest_folder) throws Throwable {
        for (File file : dest_folder.isDirectory() ? dest_folder.listFiles() : dest_folder.getParentFile().listFiles()) {
            String name = file.getName();
            String recommended = download_file.displayName();

            if (similarity(name, recommended) >= 0.6) {
                Debug.util.add(Text.util.create("Removed old mod file: " + dest_folder.getName() + "/" + file.getName(), Color.LIGHTGREEN, 12), true);
                Files.delete(file.toPath());
                break;
            }
        }
    }

    private boolean contains(final CurseFile file, final String ver) {
        for (String v : file.gameVersionStrings()) {
            if (v.equals(ver)) {
                return true;
            }
        }

        return false;
    }

    private boolean alreadyDownloaded(final CurseFile download_file, File dest_folder) {
        for (File file : dest_folder.isDirectory() ? dest_folder.listFiles() : dest_folder.getParentFile().listFiles()) {
            String name = file.getName();
            String recommended = download_file.displayName();

            if (similarity(name, recommended) >= 0.6) {
                return true;
            }
        }

        return false;
    }

    public double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0)
            return 1.0;

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}
