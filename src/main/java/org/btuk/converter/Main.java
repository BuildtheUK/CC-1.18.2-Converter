package org.btuk.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    //Y / 16 is the cube height
    public static int MIN_Y_CUBE = -8;
    public static int MAX_Y_CUBE = 32;
    public static int OFFSET = 0;

    //Default biome namespace
    public static final String DEFAULT_BIOME = "minecraft:forest";

    //This is where the program will start.
    //This is cli application and thus the arguments will be provided through the args variable.

    /*
    The format of args is:
        args[0]: path of input world file folder
        args[1]: path of output folder
        args[2]: y min
        args[3]: y max
        args[4]: offset.
        args[5]: number of threads to use.
     */

    public static void main(String[] args) {

        //Start time.
        Date date = new Date();
        Long start_time = date.getTime();

        if (args.length < 5) {
            log.error("You must provide arguments for the input and output folders as well as the min and max height and offset.");
            log.error("Additionally the number of threads can be specified.");
            log.error("java -jar CC-Converter.jar <path to input> <path to output> <minY> <maxY> <offset> [threads]");
            return;
        }

        int max_threads = 1;
        int available_processors = Runtime.getRuntime().availableProcessors();

        try {

            MIN_Y_CUBE = Integer.parseInt(args[2]) / 16;
            MAX_Y_CUBE = Integer.parseInt(args[3]) / 16;

        } catch (NumberFormatException e) {
            log.error("java -jar CC-Converter.jar <path to input> <path to output> <minY> <maxY> <offset> [threads]");
            return;
        }
        try {

            OFFSET = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            log.error("java -jar CC-Converter.jar <path to input> <path to output> <minY> <maxY> <offset> [threads]");
            return;
        }

        if (args.length == 6) {
            try {
                max_threads = Integer.parseInt(args[5]);
                if (max_threads > available_processors) {
                    log.error("Error: The number of threads specified (" + max_threads + ") exceeds the number of available processors (" + available_processors + ").");
                    return;
                }
            } catch (NumberFormatException e) {
                max_threads = 1;
            }
        }
        log.info("Starting converter with Min-Cube: " + MIN_Y_CUBE + " and Max-Cube: " + MAX_Y_CUBE);
        log.info("Offset is set to " + OFFSET + " blocks / meters");
        log.info("Number of threads: " + max_threads);

        new WorldIterator(args[0], args[1], max_threads);

        date = new Date();
        Long end_time = date.getTime();

        long durationInMillis = end_time-start_time;

        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        log.info("Done!");
        log.info("Conversion completed in: " + time);

    }
}
