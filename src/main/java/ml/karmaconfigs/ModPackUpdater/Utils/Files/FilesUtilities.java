package ml.karmaconfigs.ModPackUpdater.Utils.Files;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;

import java.io.File;

public interface FilesUtilities {

    Config getConfig = new Config();

    static String getPath(File file) {
        return file.getAbsolutePath().replaceAll("\\\\", "/");
    }

    static String getMinecraftDir() {
        if (Utils.os.getOS().equals("Windows")) {
            return (System.getenv("APPDATA") + "/.minecraft");
        }
        if (Utils.os.getOS().equals("Linux")) {
            return (System.getProperty("user.home") + "/.minecraft");
        }
        if (Utils.os.getOS().equals("Mac")) {
            return (System.getProperty("user.home") + "/Library/Application Support/minecraft");
        }
        return "N/A";
    }

    static File getUpdaterDir() {
        if (Utils.os.getOS().equals("Windows")) {
            return new File(System.getenv("APPDATA") + "/ModPackUpdater");
        }
        if (Utils.os.getOS().equals("Linux")) {
            return new File(System.getProperty("user.home") + "/ModPackUpdater");
        }
        if (Utils.os.getOS().equals("Mac")) {
            return new File(System.getProperty("user.home") + "/Library/Application Support/ModPackUpdater");
        }
        return new File("");
    }

    @SneakyThrows
    static File getFileFromURL(String url) throws Throwable {
        Utils utils = new Utils();

        return new File(getUpdaterDir() + "/modpacks/", utils.getModpackName(url) + ".txt");
    }

    static File getModpackDownloadDir(Modpack modpack) {
        File downloadDir = new File(getUpdaterDir() + "/downloads/" + modpack.getName() + "/");

        if (!downloadDir.exists()) {
            if (downloadDir.mkdirs()) {
                System.out.println("Executed");
            }
        }
        return downloadDir;
    }

    static File getModpackMods(Modpack modpack) {
        File modsDir = new File(getModpackDownloadDir(modpack), "mods/");

        if (!modsDir.exists()) {
            if (modsDir.mkdirs()) {
                System.out.println("Executed");
            }
        }

        return modsDir;
    }

    static File getModpackTextures(Modpack modpack) {
        File modsDir = new File(getModpackDownloadDir(modpack), "resourcepacks/");

        if (!modsDir.exists()) {
            if (modsDir.mkdirs()) {
                System.out.println("Executed");
            }
        }

        return modsDir;
    }

    static File getModpackShaders(Modpack modpack) {
        File modsDir = new File(getModpackDownloadDir(modpack), "shaderpacks/");

        if (!modsDir.exists()) {
            if (modsDir.mkdirs()) {
                System.out.println("Executed");
            }
        }

        return modsDir;
    }

    static File getModpackUploadDir(Modpack modpack) {
        File uploadDir = new File(getUpdaterDir() + "/uploads/" + modpack.getName() + "/");
        if (!uploadDir.exists()) {
            if (uploadDir.mkdirs()) {
                System.out.println("Executed");
            }
        }

        return uploadDir;
    }

    static File getModpackLaunchDir(Modpack modpack) {
        File launchDir = new File(getUpdaterDir() + "/launcher/" + modpack.getName() + "/");
        if (!launchDir.exists() && launchDir.mkdirs()) {
            System.out.println("Executed");
        }

        return launchDir;
    }
}
