package ml.karmaconfigs.modpackupdater.utils.downloader;

public final class AlreadyDownloadingException extends Exception {

    public AlreadyDownloadingException() {
        super("The tool is already downloading resources, please wait!");
    }
}
