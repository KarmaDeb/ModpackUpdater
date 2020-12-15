package ml.karmaconfigs.launcher.transformerdiscoveryservice;

import cpw.mods.modlauncher.serviceapi.ITransformerDiscoveryService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KLauncherTransformer implements ITransformerDiscoveryService {

    private final static String launch_candidates = System.getProperty("ml.karmaconfigs.launcher.candidates");

    //I don't know how to make this to work :c
    /*
    Until I found the wat to make this work, the tool launcher
    will be strip to +1.13 ( 1.13 not included ) forge versions
     */
    @Override
    public List<Path> candidates(Path gameDir) {
        return Arrays.stream(launch_candidates.split(";"))
                .flatMap(path -> {
                    try {
                        return Stream.of(Paths.get(path));
                    } catch (Throwable e) {
                        return Stream.of();
                    }
                }).collect(Collectors.toList());
    }
}
