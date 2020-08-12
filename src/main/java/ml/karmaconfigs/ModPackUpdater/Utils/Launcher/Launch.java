package ml.karmaconfigs.ModPackUpdater.Utils.Launcher;

import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class Launch {

    /*
    Placeholders:
    {min} - Min memory to use
    {natives} - Natives locations
    {libraries} - Libraries location
    {mainClass} - The .jar main class
    {name} - The client name
    {version} - The mc version
    {game} - The game directory
    {assetsDir} - The game assets dir
    {assetsIndex} - The assets index version
    {uuid} - The uuid used
    {tweakLoader} - Tweak class loader
    {type} - The version type
    {f_version} - Forge version
    {f_mcVersion} - Forge mc version
    {f_mcpVersion} - Forge mc portable version
     */
    private final Modpack modpack;

    private final static Utils utils = new Utils();

    public Launch() {
        modpack = new Modpack(utils.getCurrentModpack());

        MCDownloader downloader = new MCDownloader(modpack);
        downloader.run();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (downloader.isFinished()) {
                    timer.cancel();
                    String arg = "no args";
                    try {
                        String clientToken = UUID.nameUUIDFromBytes(("OfflinePlayer:" + FilesUtilities.getConfig.getClientName()).getBytes(StandardCharsets.UTF_8)).toString();
                        String natives = FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/versions/" + modpack.getVersionName() + "/natives"));
                        String tweak = getTweakClass(modpack.getVersionName());
                        String libs = getLibraries(modpack.getVersionName(), modpack.getRealVersion()) + ";" + FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/versions/" + modpack.getVersionName(), modpack.getVersionName() + ".jar"));

                        if (!tweak.isEmpty()) {
                            String arg_legacy = "java -Xms512M -Xmx{min}M -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump -Djava.library.path={natives} -cp {libraries} {mainClass} --width 854 --height 480 --username {name} --version {version} --gameDir {game} --assetsDir {assetsDir} --assetIndex {assetsIndex} --uuid {uuid} --accessToken null --tweakClass {tweakLoader} --versionType {type}";
                            arg = arg_legacy
                                    .replace("{min}", FilesUtilities.getConfig.getClientMemory())
                                    .replace("{natives}", natives)
                                    .replace("{libraries}", libs)
                                    .replace("{mainClass}", getMainClass(modpack.getVersionName()))
                                    .replace("{name}", FilesUtilities.getConfig.getClientName())
                                    .replace("{version}", modpack.getVersionName())
                                    .replace("{game}", FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir())))
                                    .replace("{assetsDir}", FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/assets")))
                                    .replace("{assetsIndex}", modpack.getRealVersion())
                                    .replace("{uuid}", clientToken)
                                    .replace("{tweakLoader}", tweak)
                                    .replace("{type}", getType(modpack.getVersionName()).toLowerCase());
                        } else {
                            String args_actuality = "java -Xms512M -Xmx{min}M -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump -Djava.library.path={natives} -cp {libraries} {mainClass} --width 854 --height 480 --username {name} --version {version} --gameDir {game} --assetsDir {assetsDir} --assetIndex {assetsIndex} --uuid {uuid} --accessToken null --launchTarget fmlclient --fml.forgeVersion {f_version} --fml.mcVersion {f_mcVersion} --fml.mcpVersion {f_mcpVersion} --fml.forgeGroup net.minecraftforge --versionType {type}";
                            arg = args_actuality
                                    .replace("{min}", FilesUtilities.getConfig.getClientMemory())
                                    .replace("{natives}", natives)
                                    .replace("{libraries}", libs)
                                    .replace("{mainClass}", getMainClass(modpack.getVersionName()))
                                    .replace("{name}", FilesUtilities.getConfig.getClientName())
                                    .replace("{version}", modpack.getVersionName())
                                    .replace("{game}", FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir())))
                                    .replace("{assetsDir}", FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/assets")))
                                    .replace("{assetsIndex}", modpack.getRealVersion())
                                    .replace("{uuid}", UUID.randomUUID().toString())
                                    .replace("{f_version}", getJVMArg(modpack.getVersionName(), "--fml.forgeVersion"))
                                    .replace("{f_mcVersion}", getJVMArg(modpack.getVersionName(), "--fml.mcVersion"))
                                    .replace("{f_mcpVersion}", getJVMArg(modpack.getVersionName(), "--fml.mcpVersion"))
                                    .replace("{type}", "release");
                        }
                    } catch (Throwable e) {
                        utils.log(e);
                    }

                    utils.setDebug(utils.rgbColor("Trying to launch minecraft using args: " + arg, 155, 240, 175), true);
                    System.out.println(arg);
                    try {
                        ProcessBuilder builder = new ProcessBuilder(arg.split(" "));
                        builder.directory(new File(FilesUtilities.getMinecraftDir() + "/bin"));

                        Process process = builder.start();
                        process.waitFor(10, TimeUnit.SECONDS);

                        if (!process.isAlive()) {
                            if (process.exitValue() != 0) {
                                utils.setDebug(utils.rgbColor("[ERROR] Process ended with code " + process.exitValue(), 220, 100, 100), true);
                                utils.display(process);
                                process.destroy();
                            }
                        }
                    } catch (Throwable e) {
                        utils.log(e);
                    }
                }
            }
        }, 0, 1);
    }

    /**
     * Get the main class used by the version
     *
     * @param version the version
     * @return a String
     * @throws Throwable any exception
     */
    private String getMainClass(String version) throws Throwable {
        File json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version + "/" + version + ".json");
        if (json.exists()) {
            FileReader reader = new FileReader(json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("mainClass")) {
                return info.get("mainClass").toString();
            }
        }
        return "";
    }

    private String getTweakClass(String version) throws Throwable {
        File json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version + "/" + version + ".json");
        if (json.exists()) {
            FileReader reader = new FileReader(json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("minecraftArguments")) {
                String[] data = info.get("minecraftArguments").toString().split(" ");

                for (int i = 0; i < data.length; i++) {
                    String current = data[i];
                    String next = "";
                    if (i + 1 != data.length) {
                        next = data[i + 1];
                    }
                    if (current.equals("--tweakClass")) {
                        return next;
                    }
                }
            }
        }
        return "";
    }

    /**
     * Get the .jar version type
     *
     * @param version the version
     * @return a String
     * @throws Throwable any exception
     */
    private String getType(String version) throws Throwable {
        File json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version + "/" + version + ".json");
        if (json.exists()) {
            FileReader reader = new FileReader(json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("minecraftArguments")) {
                String[] data = info.get("minecraftArguments").toString().split(" ");

                for (int i = 0; i < data.length; i++) {
                    String current = data[i];
                    String next = "";
                    if (i + 1 != data.length) {
                        next = data[i + 1];
                    }
                    if (current.equals("--versionType")) {
                        return next;
                    }
                }
            }
        }
        return "";
    }

    /**
     * Build the libraries used by the .jar
     *
     * @param version the version
     * @return a String
     */
    private String getLibraries(String version, String realVersion) throws Throwable {
        File json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version + "/" + version + ".json");
        File vanilla_json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + realVersion + "/" + realVersion + ".json");
        StringBuilder lib = new StringBuilder();

        if (json.exists()) {
            FileReader reader = new FileReader(json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("libraries")) {
                JSONArray list = (JSONArray) info.get("libraries");
                for (Object obj : list) {
                    JSONObject object = (JSONObject) obj;
                    JSONObject libInfo = (JSONObject) object.get("downloads");

                    if (libInfo.containsKey("artifact")) {
                        JSONObject artifacts = (JSONObject) libInfo.get("artifact");

                        if (artifacts.containsKey("path") && artifacts.get("path") != null && !artifacts.get("path").toString().isEmpty()) {
                            String path = FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/libraries/" + artifacts.get("path").toString()));
                            if (!lib.toString().contains(path)) {
                                lib.append(path).append(";");
                            }
                        }
                    }
                }
            }
        }

        if (vanilla_json.exists()) {
            FileReader reader = new FileReader(vanilla_json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("libraries")) {
                JSONArray list = (JSONArray) info.get("libraries");
                for (Object obj : list) {
                    JSONObject object = (JSONObject) obj;
                    JSONObject libInfo = (JSONObject) object.get("downloads");

                    if (libInfo.containsKey("artifact")) {
                        JSONObject artifacts = (JSONObject) libInfo.get("artifact");

                        if (artifacts.containsKey("path") && artifacts.get("path") != null && !artifacts.get("path").toString().isEmpty()) {
                            String path = FilesUtilities.getPath(new File(FilesUtilities.getMinecraftDir() + "/libraries/" + artifacts.get("path").toString()));

                            if (!lib.toString().contains(path)) {
                                lib.append(path).append(";");
                            }
                        }
                    }
                }
            }
        }

        return lib.substring(0, lib.length() - 1);
    }

    private String getJVMArg(String version, String arg) throws Throwable {
        File json = new File(FilesUtilities.getMinecraftDir() + "/versions/" + version + "/" + version + ".json");

        if (json.exists()) {
            FileReader reader = new FileReader(json);
            JSONParser jsonParser = new JSONParser();
            JSONObject info = (JSONObject) jsonParser.parse(reader);

            if (info.containsKey("arguments")) {
                JSONObject args = (JSONObject) info.get("arguments");
                if (args.containsKey("game")) {
                    JSONArray game = (JSONArray) args.get("game");
                    for (int i = 0; i < game.size(); i++) {
                        String current = game.get(i).toString();
                        String next = "";
                        if (i + 1 != game.size()) {
                            next = game.get(i + 1).toString();
                        }

                        if (current.equals(arg)) {
                            return next;
                        }
                    }
                }
            }
        }

        return "";
    }
}
