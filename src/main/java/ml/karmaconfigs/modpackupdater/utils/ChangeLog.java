package ml.karmaconfigs.modpackupdater.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class ChangeLog {

    private static final ArrayList<String> changelog = new ArrayList<>();
    private static String latest = "0";
    private static int back = 10;
    private static boolean available = true;

    /**
     * Tries to request the changelog
     *
     * @throws Throwable if something goes wrong
     */
    public final void requestChangelog() throws Throwable {
        if (available) {
            changelog.clear();
            Debug.util.add(Text.util.create("Retrieving changelog...", Color.WHITE, 12), true);
            URL url = new URL("https://karmaconfigs.github.io/updates/ModpackUpdater/latest.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String word;
            List<String> lines = new ArrayList<>();
            while ((word = reader.readLine()) != null) {
                lines.add(word);
            }

            reader.close();

            latest = lines.get(0);

            for (int i = 3; i < lines.size(); i++) {
                String current = lines.get(i);
                String next = "";
                if (i + 1 < lines.size()) {
                    next = lines.get(i + 1);
                }
                if (next.startsWith("-")) {
                    changelog.add(current + "<br>");
                } else {
                    changelog.add(current);
                }
            }

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    back--;
                    if (back == 0) {
                        available = true;
                        back = 10;
                        timer.cancel();
                    }
                }
            }, 0, TimeUnit.SECONDS.toMillis(1));
            available = false;
        } else {
            Debug.util.add(Text.util.create("Wait " + back + " seconds before requesting changelog...", Color.INDIANRED, 12), true);
        }
    }

    /**
     * Get the latest version of the tool
     *
     * @return the latest version of the tool
     */
    public final String getVersion() {
        return latest;
    }

    /**
     * Get the changelog
     *
     * @return a String
     */
    @Override
    public final String toString() {
        StringBuilder info = new StringBuilder();
        info.append("<html><div><h1>").append("Version: ").append(latest).append("</h1><br><br><br>");
        for (int i = 0; i < changelog.size(); i++) {
            if (i != changelog.size() - 1) {
                info.append(changelog.get(i)).append("<br>");
            } else {
                info.append(changelog.get(i)).append("</div></html>");
            }
        }

        return info.toString();
    }
}
