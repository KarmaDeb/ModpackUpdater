package ml.karmaconfigs.modpackupdater.utils.launcher;

import ml.karmaconfigs.modpackupdater.utils.Utils;
import ml.karmaconfigs.modpackupdater.utils.files.FilesUtilities;
import ml.karmaconfigs.modpackupdater.utils.files.Unzip;
import ml.karmaconfigs.modpackupdater.utils.modpack.Modpack;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class MCDownloader {

    private static Modpack modpack;

    private static boolean finished = false;

    /**
     * Initialize the MCDownloader
     *
     * @param modpack the modpack natives to download
     */
    public MCDownloader(Modpack modpack) {
        MCDownloader.modpack = modpack;
        finished = false;
    }

    public void run() {
        DownloadManifest manifest = new DownloadManifest();
        Thread thread_1 = new Thread(manifest, "DownloadManifest");
        thread_1.start();

        Timer timer_1 = new Timer();
        timer_1.schedule(new TimerTask() {
            @Override
            public void run() {
                if (manifest.downloaded) {
                    DownloadJSon version = new DownloadJSon(modpack);
                    Thread thread_2 = new Thread(version, "DownloadVersion");
                    thread_2.start();

                    Timer timer_2 = new Timer();
                    timer_2.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (version.downloaded) {
                                DownloadAssetsIndex assets = new DownloadAssetsIndex(modpack);
                                Thread thread_3 = new Thread(assets, "DownloadAssets");
                                thread_3.start();

                                Timer timer_3 = new Timer();
                                timer_3.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (assets.downloaded) {
                                            DownloadLibraries libraries = new DownloadLibraries(modpack);
                                            Thread thread_4 = new Thread(libraries, "DownloadLibs");
                                            thread_4.start();

                                            Timer timer_4 = new Timer();
                                            timer_4.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    if (libraries.downloaded) {
                                                        DownloadNatives natives = new DownloadNatives(modpack);
                                                        Thread thread_4 = new Thread(natives, "DownloadNatives");

                                                        thread_4.start();

                                                        Timer timer_5 = new Timer();
                                                        timer_5.schedule(new TimerTask() {
                                                            @Override
                                                            public void run() {
                                                                finished = natives.downloaded;
                                                                if (finished) {
                                                                    timer_5.cancel();
                                                                }
                                                            }
                                                        }, 0, 1);
                                                        timer_4.cancel();
                                                    }
                                                }
                                            }, 0, 1);
                                            timer_3.cancel();
                                        }
                                    }
                                }, 0, 1);
                                timer_2.cancel();
                            }
                        }
                    }, 0, 1);
                    timer_1.cancel();
                }
            }
        }, 0, 1);
    }

    /**
     * Check if the natives have been completely downloaded
     *
     * @return a boolean
     */
    public final boolean isFinished() {
        return finished;
    }
}

/**
 * Download the versions manifest
 */
class DownloadManifest implements Runnable {

    private final static Utils utils = new Utils();
    public boolean downloaded = false;

    @Override
    public void run() {
        try {
            File destination = new File(FilesUtilities.getMinecraftDir(), "version_manifest.json");

            if (!destination.exists()) {
                utils.setDebug(utils.rgbColor("Downloading versions manifest", 155, 240, 175), true);

                int count;
                URL url = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");

                URLConnection connection = url.openConnection();
                connection.connect();

                int lengthOfFile = connection.getContentLength();

                File destFolder = new File(FilesUtilities.getPath(destination).replace("/" + destination.getName(), ""));

                if (!destFolder.exists() && destFolder.mkdirs()) {
                    System.out.println("Executed");
                }

                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(destination);

                byte[] data = new byte[1024];

                long percentage = 0;
                while ((count = input.read(data)) != -1) {
                    long total = destination.length();
                    if (percentage != total * 100 / lengthOfFile) {
                        percentage = total * 100 / lengthOfFile;
                        if (percentage < 98) {
                            utils.setProgress("Downloading json " + destination.getName(), (int) percentage);
                        }
                    }

                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            }
        } catch (Exception e) {
            utils.log(e);
        } finally {
            utils.setProgress("Download status bar", 1);
            downloaded = true;
        }
    }
}

/**
 * Download version .json to get libraries and other...
 */
class DownloadJSon implements Runnable {

    private final static Utils utils = new Utils();
    private static String version;
    public boolean downloaded = false;

    public DownloadJSon(Modpack modpack) {
        version = modpack.getRealVersion();
    }

    /**
     * Get the main class used by the version
     *
     * @param version the version
     * @return a String
     * @throws Throwable any exception
     */
    private String getVersionJson(String version) throws Throwable {
        File json = new File(FilesUtilities.getMinecraftDir(), "version_manifest.json");
        if (json.exists()) {
            FileReader reader = new FileReader(json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("versions")) {
                JSONArray versions = (JSONArray) info.get("versions");

                for (Object o : versions) {
                    JSONObject versionInfo = (JSONObject) o;
                    if (versionInfo.get("id").equals(version)) {
                        return versionInfo.get("url").toString();
                    }
                }
            }
        }
        return "";
    }

    @Override
    public void run() {
        try {
            utils.setDebug(utils.rgbColor("Downloading " + version + ".json", 155, 240, 175), true);

            int count;
            URL url = new URL(getVersionJson(version));

            URLConnection connection = url.openConnection();
            connection.connect();

            int lengthOfFile = connection.getContentLength();

            File destFolder = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version);
            File destination = new File(destFolder, version + ".json");

            if (!destination.exists()) {
                if (!destFolder.exists() && destFolder.mkdirs()) {
                    System.out.println("Executed");
                }

                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(destination);

                byte[] data = new byte[1024];

                long percentage = 0;
                while ((count = input.read(data)) != -1) {
                    long total = destination.length();
                    if (percentage != total * 100 / lengthOfFile) {
                        percentage = total * 100 / lengthOfFile;
                        if (percentage < 98) {
                            utils.setProgress("Downloading json " + destination.getName(), (int) percentage);
                        }
                    }

                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            }
        } catch (Throwable e) {
            utils.log(e);
        } finally {
            utils.setProgress("Download status bar", 1);
            downloaded = true;
        }
    }
}

class DownloadAssetsIndex implements Runnable {

    private final static Utils utils = new Utils();
    private static String version;
    public boolean downloaded = false;

    public DownloadAssetsIndex(Modpack modpack) {
        version = modpack.getRealVersion();
    }

    /**
     * Get the main class used by the version
     *
     * @param version the version
     * @return a String
     * @throws Throwable any exception
     */
    private String getAssetsUrl(String version) throws Throwable {
        File json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version, version + ".json");
        if (json.exists()) {
            FileReader reader = new FileReader(json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("assetIndex")) {
                JSONObject assets = (JSONObject) info.get("assetIndex");

                return assets.get("url").toString();
            }
        }
        return "";
    }

    @Override
    public void run() {
        try {
            utils.setDebug(utils.rgbColor("Downloading assets for " + version + ".json", 155, 240, 175), true);

            int count;
            URL url = new URL(getAssetsUrl(version));

            URLConnection connection = url.openConnection();
            connection.connect();

            int lengthOfFile = connection.getContentLength();

            File destFolder = new File(FilesUtilities.getMinecraftDir() + "/assets/indexes/");
            File destination = new File(destFolder, version + ".json");

            if (!destination.exists()) {
                if (!destFolder.exists() && destFolder.mkdirs()) {
                    System.out.println("Executed");
                }

                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(destination);

                byte[] data = new byte[1024];

                long percentage = 0;
                while ((count = input.read(data)) != -1) {
                    long total = destination.length();
                    if (percentage != total * 100 / lengthOfFile) {
                        percentage = total * 100 / lengthOfFile;
                        if (percentage < 98) {
                            utils.setProgress("Downloading json " + destination.getName(), (int) percentage);
                        }
                    }

                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            }
        } catch (Throwable e) {
            utils.log(e);
        } finally {
            utils.setProgress("Download status bar", 1);
            downloaded = true;
        }
    }
}

class DownloadLibraries implements Runnable {

    private final static Utils utils = new Utils();
    private static String version;
    private static String f_version;
    public boolean downloaded = false;

    public DownloadLibraries(Modpack modpack) {
        f_version = modpack.getVersionName();
        version = modpack.getRealVersion();
    }

    /**
     * Get the main class used by the version
     *
     * @return a HashMap
     */
    private HashMap<URL, String> getVanillaLibs() throws Throwable {
        HashMap<URL, String> libraries = new HashMap<>();

        File vanilla_json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version, version + ".json");

        if (vanilla_json.exists()) {
            FileReader reader = new FileReader(vanilla_json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("libraries")) {
                JSONArray libs = (JSONArray) info.get("libraries");

                for (Object lib : libs) {
                    JSONObject libInfo = (JSONObject) lib;
                    JSONObject downloadInfo = (JSONObject) libInfo.get("downloads");

                    if (downloadInfo.containsKey("artifact")) {
                        JSONObject artifact = (JSONObject) downloadInfo.get("artifact");

                        if (!artifact.get("url").toString().isEmpty()) {
                            String path = FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/libraries/" + artifact.get("path")));
                            URL url = new URL(URLDecoder.decode(artifact.get("url").toString(), "UTF-8"));

                            libraries.put(url, path);
                        } else {
                            String path = FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/libraries/" + artifact.get("path")));
                            URL url = new URL("http://files.minecraftforge.net/maven/" + artifact.get("path"));

                            libraries.put(url, path);
                        }
                    }
                }
            }
        }

        return libraries;
    }

    private HashMap<URL, String> getLoaderLibraries() throws Throwable {
        HashMap<URL, String> libraries = new HashMap<>();

        File loader_json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + f_version, f_version + ".json");

        if (loader_json.exists()) {
            FileReader reader = new FileReader(loader_json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("libraries")) {
                JSONArray libs = (JSONArray) info.get("libraries");

                for (Object lib : libs) {
                    JSONObject libInfo = (JSONObject) lib;
                    JSONObject downloadInfo = (JSONObject) libInfo.get("downloads");

                    if (downloadInfo.containsKey("artifact")) {
                        JSONObject artifact = (JSONObject) downloadInfo.get("artifact");

                        if (!artifact.get("url").toString().isEmpty()) {
                            String path = FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/libraries/" + artifact.get("path")));
                            URL url = new URL(URLDecoder.decode(artifact.get("url").toString(), "UTF-8"));

                            libraries.put(url, path);
                        } else {
                            String path = FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/libraries/" + artifact.get("path")));
                            URL url = new URL("http://files.minecraftforge.net/maven/" + artifact.get("path").toString().replace(".jar", "-universal.jar"));

                            libraries.put(url, path);
                        }
                    }
                }
            }
        }

        return libraries;
    }

    private HashMap<URL, String> getLibraries() throws Throwable {
        HashMap<URL, String> libraries = new HashMap<>();

        libraries.putAll(getVanillaLibs());
        libraries.putAll(getLoaderLibraries());

        return libraries;
    }

    @Override
    public void run() {
        int libs;
        int downloadedAmount = 0;
        int already = 0;
        int error = 0;

        try {
            HashMap<URL, String> libraries = getLibraries();

            libs = libraries.size();

            utils.setDebug(utils.rgbColor("Trying to download " + libs + " libraries", 155, 240, 175), true);

            for (URL url : libraries.keySet()) {
                File destination = new File(libraries.get(url));
                if (!destination.exists()) {
                    File path = new File(destination.getPath().replace(destination.getName(), ""));

                    if (!path.exists() && path.mkdirs()) {
                        System.out.println("Executed");
                    }

                    if (url != null) {
                        downloaded = false;
                        utils.setDebug(utils.rgbColor("Downloading library " + destination.getName() + " to " + FilesUtilities.getPath(destination) + " from " + url, 155, 240, 175), downloadedAmount == 0);

                        int count;

                        URLConnection connection = url.openConnection();
                        connection.connect();

                        int lengthOfFile = connection.getContentLength();

                        InputStream input = new BufferedInputStream(url.openStream(), 8192);
                        OutputStream output = new FileOutputStream(destination);

                        byte[] data = new byte[1024];

                        long percentage = 0;
                        while ((count = input.read(data)) != -1) {
                            long total = destination.length();
                            if (percentage != total * 100 / lengthOfFile) {
                                percentage = total * 100 / lengthOfFile;
                                if (percentage < 98) {
                                    utils.setProgress("Downloading library " + destination.getName(), (int) percentage);
                                }
                            }

                            output.write(data, 0, count);
                        }

                        output.flush();
                        output.close();
                        input.close();
                    } else {
                        utils.setDebug(utils.rgbColor("Download url for " + destination.getName() + " is null", 220, 100, 100), false);
                        error++;
                    }
                } else {
                    utils.setDebug(utils.rgbColor("Library " + destination.getName() + " is already downloaded", 155, 240, 175), downloadedAmount == 0);
                    already++;
                }
                downloadedAmount++;
            }
        } catch (Throwable e) {
            utils.log(e);
        } finally {
            downloaded = true;

            utils.setDebug(utils.rgbColor("Downloaded a total of " + downloadedAmount + " libraries ( " + already + " where already downloaded and " + error + " failed )", 155, 240, 175), true);

            utils.setProgress("Download status bar", 1);
        }
    }
}

class DownloadNatives implements Runnable {

    private final static Utils utils = new Utils();

    private static String version;
    private static String f_version;

    public boolean downloaded = false;

    public DownloadNatives(Modpack modpack) {
        version = modpack.getRealVersion();
        f_version = modpack.getVersionName();
    }

    private ArrayList<String> getNativesURL(File json) {
        ArrayList<String> version_url_list_natives = new ArrayList<>();
        try {
            String natives_OS = Utils.os.getOS();
            switch (natives_OS) {
                case "Linux":
                    natives_OS = natives_OS.replace("Linux", "natives-linux");
                    break;
                case "Windows":
                    natives_OS = natives_OS.replace("Windows", "natives-windows");
                    break;
                case "Mac":
                    natives_OS = natives_OS.replace("Mac", "natives-osx");
                    break;
            }
            String content = new Scanner(json).useDelimiter("\\Z").next();
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
            try {

                String script_js = "var getJsonLibrariesDownloadsClassifiersNativesX=function(r,s){var a=r,e=JSON.parse(a),n=\"\",t=0;for(i=0;i<500;i++)try{n=n+e.libraries[t].downloads.classifiers[s].url+\"\\n\",t+=1}catch(o){t+=1}return n},getJsonLibrariesDownloadsClassifiersNativesY=function(r,s){var a=r,e=JSON.parse(a),n=\"\",t=0;for(i=0;i<500;i++)try{n=n+e.libraries[t].downloads.classifiers[s].path+\"\\n\",t+=1}catch(o){t+=1}return n},getJsonLibrariesDownloadsClassifiersNativesZ=function(r){var s=r,a=JSON.parse(s),e=\"\",n=0;for(i=0;i<500;i++)try{a.libraries[n].natives?(e=e+a.libraries[n].name+\"\\n\",n+=1):n+=1}catch(t){n+=1}return e};";

                File file = new File("./.script.js");
                if (file.createNewFile()) {
                    System.out.println("Executed");
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(script_js);
                bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            engine.eval(new FileReader("./.script.js"));

            Invocable invocable = (Invocable) engine;

            Object result = invocable.invokeFunction("getJsonLibrariesDownloadsClassifiersNativesX", content, natives_OS);

            version_url_list_natives.addAll(Arrays.asList(result.toString().split("\n")));
        } catch (Throwable e) {
            utils.log(e);
        }

        return version_url_list_natives;
    }

    private ArrayList<String> getNativesPath(File json) {
        ArrayList<String> version_path_list_natives = new ArrayList<>();
        try {
            String natives_OS = Utils.os.getOS();
            switch (natives_OS) {
                case "Linux":
                    natives_OS = natives_OS.replace("Linux", "natives-linux");
                    break;
                case "Windows":
                    natives_OS = natives_OS.replace("Windows", "natives-windows");
                    break;
                case "Mac":
                    natives_OS = natives_OS.replace("Mac", "natives-osx");
                    break;
            }

            String content = new Scanner(json).useDelimiter("\\Z").next();
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");

            try {
                String script_js = "var getJsonLibrariesDownloadsClassifiersNativesX=function(r,s){var a=r,e=JSON.parse(a),n=\"\",t=0;for(i=0;i<500;i++)try{n=n+e.libraries[t].downloads.classifiers[s].url+\"\\n\",t+=1}catch(o){t+=1}return n},getJsonLibrariesDownloadsClassifiersNativesY=function(r,s){var a=r,e=JSON.parse(a),n=\"\",t=0;for(i=0;i<500;i++)try{n=n+e.libraries[t].downloads.classifiers[s].path+\"\\n\",t+=1}catch(o){t+=1}return n},getJsonLibrariesDownloadsClassifiersNativesZ=function(r){var s=r,a=JSON.parse(s),e=\"\",n=0;for(i=0;i<500;i++)try{a.libraries[n].natives?(e=e+a.libraries[n].name+\"\\n\",n+=1):n+=1}catch(t){n+=1}return e};";

                File file = new File("./.script.js");
                if (file.createNewFile()) {
                    System.out.println("Executed");
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(script_js);
                bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            engine.eval(new FileReader("./.script.js"));

            Invocable invocable = (Invocable) engine;

            Object result = invocable.invokeFunction("getJsonLibrariesDownloadsClassifiersNativesY", content, natives_OS);

            version_path_list_natives.addAll(Arrays.asList(result.toString().split("\n")));
        } catch (Throwable e) {
            utils.log(e);
        }

        return version_path_list_natives;
    }

    private ArrayList<String> removeDuped(ArrayList<String> arraylist_1, ArrayList<String> arrayList_2) {
        ArrayList<String> finalArray = new ArrayList<>();

        for (String str : arraylist_1) {
            if (!finalArray.contains(str)) {
                finalArray.add(str);
            }
        }

        for (String str : arrayList_2) {
            if (!finalArray.contains(str)) {
                finalArray.add(str);
            }
        }

        return finalArray;
    }

    private void copyNativesToLibraries() {
        File[] dirs = new File(FilesUtilities.getMinecraftDir() + "/versions/" + f_version + "/natives/").listFiles();
        if (dirs != null && !Arrays.asList(dirs).isEmpty()) {
            for (File file : dirs) {
                String[] filePath = file.getPath().split("\\\\");
                if (!filePath[filePath.length - 1].equals("META-INF")) {
                    if (file.isDirectory()) {
                        try {
                            FileUtils.copyDirectoryToDirectory(file, new File(FilesUtilities.getMinecraftDir() + "/libraries/"));
                        } catch (Throwable e) {
                            utils.log(e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        int downloadedAmount = 0;
        int already = 0;
        int error = 0;
        ArrayList<File> natives = new ArrayList<>();
        try {
            File versionVanilla = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version, version + ".json");
            File versionLoader = new File(FilesUtilities.getMinecraftDir() + "/versions/" + f_version, f_version + ".json");

            ArrayList<String> vanilla_paths = getNativesPath(versionVanilla);
            ArrayList<String> vanilla_urls = getNativesURL(versionVanilla);

            ArrayList<String> ml_paths = getNativesPath(versionLoader);
            ArrayList<String> ml_urls = getNativesURL(versionLoader);

            ArrayList<String> final_paths = removeDuped(vanilla_paths, ml_paths);
            ArrayList<String> final_urls = removeDuped(vanilla_urls, ml_urls);

            for (int i = 0; i < final_urls.size(); i++) {
                if (!final_urls.get(i).isEmpty()) {
                    File destination = new File(FilesUtilities.getMinecraftDir() + "/versions/" + f_version + "/natives/" + final_paths.get(i));

                    File destFolder = new File(destination.getPath().replace(destination.getName(), ""));
                    if (!destFolder.exists() && destFolder.mkdirs()) {
                        System.out.println("Executed");
                    }

                    int count;
                    URL url = new URL(final_urls.get(i));

                    URLConnection connection = url.openConnection();
                    connection.connect();

                    int lengthOfFile = connection.getContentLength();

                    if (!destination.exists()) {
                        utils.setDebug(utils.rgbColor("Downloading " + destination.getName() + " to " + FilesUtilities.getPath(destFolder) + " from " + final_urls.get(i), 155, 240, 175), downloadedAmount == 0);

                        if (!destFolder.exists() && destFolder.mkdirs()) {
                            System.out.println("Executed");
                        }

                        InputStream input = new BufferedInputStream(url.openStream(), 8192);
                        OutputStream output = new FileOutputStream(destination);

                        byte[] data = new byte[1024];

                        long percentage = 0;
                        while ((count = input.read(data)) != -1) {
                            long total = destination.length();
                            if (percentage != total * 100 / lengthOfFile) {
                                percentage = total * 100 / lengthOfFile;
                                if (percentage < 98) {
                                    utils.setProgress("Downloading native " + destination.getName(), (int) percentage);
                                }
                            }

                            output.write(data, 0, count);
                        }

                        output.flush();
                        output.close();
                        input.close();
                    } else {
                        utils.setDebug(utils.rgbColor("Native " + destination.getName() + " is already downloaded", 155, 240, 175), downloadedAmount == 0);
                        already++;
                    }
                    natives.add(destination);
                    downloadedAmount++;
                }
            }
        } catch (Throwable e) {
            utils.log(e);
            error++;
        } finally {
            utils.setDebug(utils.rgbColor("Downloaded a total of " + downloadedAmount + " natives ( " + already + " where already downloaded and " + error + " failed )", 155, 240, 175), true);

            if (downloadedAmount != already) {
                copyNativesToLibraries();

                Unzip unzip = new Unzip(natives, new File(FilesUtilities.getMinecraftDir() + "/versions/" + f_version + "/natives"), false);
                Thread thread = new Thread(unzip, "UnzipNatives");
                thread.start();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        downloaded = unzip.isEnded();
                        if (downloaded) {
                            timer.cancel();
                        }
                    }
                }, 0, TimeUnit.SECONDS.toMillis(1));
            } else {
                downloaded = true;
            }

            utils.setProgress("Download status bar", 1);
        }
    }
}