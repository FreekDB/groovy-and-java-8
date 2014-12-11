package com.github.freekdb.groovy_and_java_8.introduction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Class with a short introduction to streams and pipelines in Java 8.
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class Introduction {
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void main(final String[] arguments) {
        new Introduction().introduceStreams();
    }

    /**
     * Introduce streams with two examples: a simple string stream and a random number stream.
     */
    private void introduceStreams() {
        final long start = start();

        simpleStringStream();
        randomNumberStream();

        end(start);
    }

    /**
     * Simple stream with some strings.
     */
    private void simpleStringStream() {
        //noinspection SpellCheckingInspection
        final List<String> strings = Arrays.asList("one", "zwei", "trois", "vier", "one");
        final String joinedStrings = strings.stream()
            .distinct()
            .sorted()
            .collect(Collectors.joining(", ", "{", "}"));
        System.out.println();
        System.out.println("Joined strings: " + joinedStrings);
        System.out.println("======");
    }

    /**
     * Stream of many random numbers that are all added up.
     */
    private void randomNumberStream() {
        final int randomSeed = 1234567890;
        final int numberCount = 100000;
        final int maximumNumber = 1000000;
        final Random randomGenerator = new Random(randomSeed);
        final long startPipeline = System.currentTimeMillis();
        final BigObject manyAddedNumbers = randomGenerator.longs(numberCount, 0, maximumNumber)
            .boxed()
            .map(BigObject::new)
            //.sorted()
            //.distinct()
            .reduce(BigObject.ZERO, BigObject::add);
        final long durationPipeline = System.currentTimeMillis() - startPipeline;
        System.out.println();
        System.out.println("Many added numbers: " + manyAddedNumbers.getBigNumber());
        System.out.println("Runtime number pipeline: " + (durationPipeline / 1000.0) + " seconds.");
        System.out.println("======");
    }

    private long start() {
        System.out.println("Start: " + TIME_FORMAT.format(new Date()));
        return System.currentTimeMillis();
    }

    private void end(final long start) {
        final long end = System.currentTimeMillis();
        System.out.println();
        System.out.println("End: " + TIME_FORMAT.format(new Date()));
        System.out.println("Total runtime: " + ((end - start) / 1000.0) + " seconds.");
    }
}
