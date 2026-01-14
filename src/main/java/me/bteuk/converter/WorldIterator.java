package org.btuk.converter;

import cubicchunks.regionlib.impl.EntryLocation2D;
import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.SaveCubeColumns;
import cubicchunks.regionlib.impl.save.SaveSection2D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cubicchunks.regionlib.lib.ExtRegion;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import org.btuk.converter.cc.MemoryReadRegion;
import org.btuk.converter.cc.RWLockingCachedRegionProvider;
import org.btuk.converter.cc.Utils;
import org.btuk.converter.utils.MinecraftIDConverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorldIterator {

    SaveCubeColumns saveCubeColumns;

    Path input;
    Path output;

    /**

    Iterates through all .2dr (512x512 files)

     @param inputPath
     the path to the directory where the world is stored

     @param outputPath
     the path to the directory where the output files should be stored

     @param maxThreads
     the maximum number of threads the converter can use, it will always use the maximum allowed

     */

    public WorldIterator(String inputPath, String outputPath, int maxThreads) {

        //Get the path from the String.
        input = Paths.get(inputPath);
        output = Paths.get(outputPath);

        //Check if file exists.
        if (!Files.exists(input) && !Files.exists(output)) {
            return;
        }

        //Create output sub-folders.
        try {
            Files.createDirectories(output.resolve("post-processing"));
            Files.createDirectories(output.resolve("region"));
            Files.createDirectories(output.resolve("maps"));
            //Files.createDirectories(output.resolve("entities"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveCubeColumns = createSave(input);

        //Get all the files in the folder.
        String[] files = new File(input.resolve("region2d").toString()).list();

        //Set the data folder of the world and output maps folder for the ID converter
        MinecraftIDConverter.instance.dataPath = input.resolve("data");
        MinecraftIDConverter.instance.mapsPath = output.resolve("maps");


        if (files == null) {
            return;
        }

        //Create the thread manager.
        //The max number of threads is specified in the args, or a default value of 1.
        ThreadManager manager = new ThreadManager(this, maxThreads);

        //Iterate through all the regions.
        for (String sFile : files) {

            //Add all the files to the queue.
            try {
                manager.queue.put(sFile);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Number of regions queued = " + manager.queue.size());

        int max_queue = manager.queue.size();
        //Every 10 seconds print the queue size in the console.
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (manager.queue.size()-maxThreads <= 0) {
                    this.cancel();
                    t.cancel();
                    return;
                }
                int progress = max_queue - (manager.queue.size() - maxThreads);
                int percentComplete = (int) (((double) progress / (double) max_queue) * 100);
                String progressBar = "[";

                for (int i = 0; i < 100; i += 5) {
                    if (i < percentComplete) {
                        progressBar += "=";
                    } else {
                        progressBar += " ";
                    }
                }

                progressBar += "] " + percentComplete + "%";
                System.out.print("\r" + progressBar + ("   Queue: " + (manager.queue.size()-maxThreads) + "/" + max_queue + " "));
            }
        }, 10000, 10000);

        //Start processing the queue.
        manager.process();
        while (manager.activeThreads.get() > 0) {

        }

        if(!MinecraftIDConverter.instance.convertedMapItems.isEmpty())
            MinecraftIDConverter.instance.writeMapsSessionConfig();
    }

    private static SaveCubeColumns createSave(Path path) {
        try {
            Utils.createDirectories(path);

            Path part2d = path.resolve("region2d");
            Utils.createDirectories(part2d);

            Path part3d = path.resolve("region3d");
            Utils.createDirectories(part3d);

            EntryLocation2D.Provider keyProv2d = new EntryLocation2D.Provider();
            EntryLocation3D.Provider keyProv3d = new EntryLocation3D.Provider();

            SaveSection2D section2d = new SaveSection2D(
                    new RWLockingCachedRegionProvider<>(
                            new SimpleRegionProvider<>(keyProv2d, part2d, (keyProv, r) ->
                                    new MemoryReadRegion.Builder<EntryLocation2D>()
                                            .setDirectory(part2d)
                                            .setRegionKey(r)
                                            .setKeyProvider(keyProv2d)
                                            .setSectorSize(512)
                                            .build(),
                                    (file, key) -> Files.exists(file)
                            )
                    ),
                    new RWLockingCachedRegionProvider<>(
                            new SimpleRegionProvider<>(new EntryLocation2D.Provider(), part2d,
                                    (keyProvider, regionKey) -> new ExtRegion<>(part2d, Collections.emptyList(), keyProvider, regionKey),
                                    (file, key) -> Files.exists(file.resolveSibling(key.getRegionKey().getName() + ".ext"))
                            )
                    ));
            SaveSection3D section3d = new SaveSection3D(
                    new RWLockingCachedRegionProvider<>(
                            new SimpleRegionProvider<>(keyProv3d, part3d, (keyProv, r) ->
                                    new MemoryReadRegion.Builder<EntryLocation3D>()
                                            .setDirectory(part3d)
                                            .setRegionKey(r)
                                            .setKeyProvider(keyProv3d)
                                            .setSectorSize(512)
                                            .build(),
                                    (file, key) -> Files.exists(file)
                            )
                    ),
                    new RWLockingCachedRegionProvider<>(
                            new SimpleRegionProvider<>(new EntryLocation3D.Provider(), part3d,
                                    (keyProvider, regionKey) -> new ExtRegion<>(part3d, Collections.emptyList(), keyProvider, regionKey),
                                    (dir, key) -> Files.exists(dir.resolveSibling(key.getRegionKey().getName() + ".ext"))
                            )
                    ));

            return new SaveCubeColumns(section2d, section3d);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
