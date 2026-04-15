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
        if(args == null || args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            printHelp();
            return;
        }

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
            printHelp();
            return;
        }
        try {

            OFFSET = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            printHelp();
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

        if(Integer.parseInt(args[2]) % 16 != 0 || Integer.parseInt(args[3]) % 16 != 0) {
            log.error("The min and max Y must be in multiple of 16");
            return;
        }

        if(MAX_Y_CUBE - MIN_Y_CUBE > 254 || MAX_Y_CUBE - MIN_Y_CUBE == 0) {
            log.error("The target world range must lower or equal to 2064");
            return;
        }

        if(OFFSET % 16 != 0) {
            log.error("The offset must be in multiple of 16");
            return;
        }

        if(MIN_Y_CUBE + (OFFSET / 16) < -127 || MAX_Y_CUBE + (OFFSET / 16) > 127) {
            log.error("The offset world range doesn't fit into the supported vanilla range from -2032 (inclusive) to 2032 (exclusive)");
            return;
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

    public static void printHelp(){
        String help = "Usage: java -jar CC-Converter.jar <path to input> <path to output> <minY> <maxY> <offset> [threads]\n" +
                "\t<path to input> <path to output>\tThe path to the CC world and output folder\n" +
                "\t<minY> <maxY>\tThe min and max world range to target (maxY-minY <= 2064) \n" +
                "\t<offset>\tThe value to offset the world range into vanilla supported range [-2032,2032). Must be in multiple of 16\n" +
                "\t[threads]\tThe number of threads to use (Optional)";
        log.info(help);
    }
}
