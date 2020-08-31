package ml.karmaconfigs.ModPackUpdater.VersionChecker;

import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class Changelog {

    private static String latest = "0";
    private static final ArrayList<String> changelog = new ArrayList<>();

    private static int back = 10;
    private static boolean available = true;

    /**
     * Initialize the version checker
     */
    public Changelog() throws Throwable {
        Utils utils = new Utils();
        if (available) {
            changelog.clear();
            utils.setDebug(utils.rgbColor("Retrieving changelog...", 125, 255, 195), true);
            URL url = new URL("https://karmaconfigs.github.io/updates/ModpackUpdater/latest.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String word;
            List<String> lines = new ArrayList<>();
            while ((word = reader.readLine()) != null) {
                if (!lines.contains(word)) {
                    lines.add(word);
                }
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
                    if (back == 1) {
                        available = true;
                        timer.cancel();
                    }
                    back--;
                }
            }, 0, TimeUnit.SECONDS.toMillis(1));
            available = false;
        } else {
            utils.setDebug(utils.rgbColor("Wait " + back + " seconds before reloading changelog", 220, 100, 100), true);
        }
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
