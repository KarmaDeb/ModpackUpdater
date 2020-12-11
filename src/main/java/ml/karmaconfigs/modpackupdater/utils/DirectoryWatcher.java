package ml.karmaconfigs.modpackupdater.utils;


import java.nio.file.*;

import static java.nio.file.LinkOption.*;
import static java.nio.file.StandardWatchEventKinds.*;

import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DirectoryWatcher {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private final boolean trace;

    private BlockingQueue<String> fileProcessingQueue;

    //******* processedFileQueue **** will be used by other threads to retrive unlocked files.. so I have
    // kept as public final
    public final BlockingQueue<String> processedFileQueue;
    private volatile boolean closeProcessingThread;
    private volatile boolean closeWatcherThread;

    private void processFiles(){
        String fileName;
        outerLoop: while(!closeProcessingThread || !fileProcessingQueue.isEmpty()){
            try{
                fileName = fileProcessingQueue.poll(1000, TimeUnit.MILLISECONDS);
            }catch(InterruptedException ie){
                fileName = null;
            }

            if(fileName == null || fileName.equals("")){
                continue;
            }

            long startTime = System.currentTimeMillis();
            innerLoop: while(true){
                FileInputStream fis = null;
                File file = new File(fileName);
                try{
                    fis = new FileInputStream(fileName);
                    break innerLoop;
                }catch(Throwable ex){
                    if(!file.exists() || file.isDirectory()){
                        continue outerLoop;
                    }
                    if((System.currentTimeMillis() - startTime) > 2000){
                        if(fileProcessingQueue.offer(fileName)){
                            continue outerLoop;
                        }else{
                            startTime = System.currentTimeMillis();
                        }
                    }
                }finally{
                    if(fis != null){
                        try{
                            fis.close();
                        }catch(IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                }
            }

            while (true) {
                try {
                    if (processedFileQueue.offer(fileName, 1000, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (Throwable ignored) {}
            }
        }
        closeWatcherThread = true;
        closeProcessingThread = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    private void processEvents(){
        while(!closeWatcherThread) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                continue;
            }catch(ClosedWatchServiceException ex){
                break;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                continue;
            }

            try{
                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);
                    if(kind.equals(ENTRY_CREATE)){
                        System.out.println("Created file: " + name);
                        if (recursive) {
                            try {
                                if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                    registerAll(child);
                                    continue;
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                        while(true){
                            if(fileProcessingQueue.remainingCapacity() < 2){
                                continue;
                            }
                            if(fileProcessingQueue.offer(child.toString())){
                                break;
                            }
                        }
                    } else {
                        if (kind.equals(ENTRY_DELETE)) {
                            System.out.println("Removed file: " + name);
                            if (recursive) {
                                try {
                                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                        registerAll(child);
                                        continue;
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                            while(true){
                                if(fileProcessingQueue.remainingCapacity() < 2){
                                    continue;
                                }
                                if(fileProcessingQueue.offer(child.toString())){
                                    break;
                                }
                            }
                        } else {
                            if (kind.equals(ENTRY_MODIFY)) {
                                System.out.println("Modified file: " + name);
                                if (recursive) {
                                    try {
                                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                            registerAll(child);
                                            continue;
                                        }
                                    } catch (Throwable ignored) {
                                    }
                                }
                                while(true){
                                    if(fileProcessingQueue.remainingCapacity() < 2){
                                        continue;
                                    }
                                    if(fileProcessingQueue.offer(child.toString())){
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }catch(ClosedWatchServiceException cwse){
                break;
            }
        }

        closeProcessingThread = true;
        closeWatcherThread = true;
    }

    public void stopWatching() throws Throwable {
        watcher.close();
        closeProcessingThread = true;
        closeWatcherThread = true;
    }

    public static DirectoryWatcher watchDirectory(final File filePath, final boolean recursive) throws Throwable {
        Path dir = filePath.isDirectory() ? filePath.toPath() : filePath.getParentFile().toPath();
        final DirectoryWatcher watchDir = new DirectoryWatcher(dir, recursive);
        watchDir.closeProcessingThread = false;
        watchDir.closeWatcherThread = false;
        new Thread(watchDir::processFiles, "DirWatchProcessingThread").start();
        new Thread(watchDir::processEvents, "DirWatcherThread").start();
        return watchDir;
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE);
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    private DirectoryWatcher(Path dir, boolean recursive) throws Throwable {
        fileProcessingQueue = new ArrayBlockingQueue<>(500, false);
        processedFileQueue = new ArrayBlockingQueue<>(500, false);
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.recursive = recursive;
        if (recursive) {
            registerAll(dir);
        } else {
            register(dir);
        }

        this.trace = true;
    }
}
