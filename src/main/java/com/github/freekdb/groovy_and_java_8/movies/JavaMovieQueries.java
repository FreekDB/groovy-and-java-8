package com.github.freekdb.groovy_and_java_8.movies;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class with movie queries, comparing Java 7 versus Java 8 style.
 * <p>
 * Thanks to:
 * - José Paumard (http://blog.paumard.org/en/) from Université Paris 13 for all the great Java 8 examples with movies
 * and actors. His presentations at Devoxx 2014 were the inspiration for this project.
 * - Robert Sedgewick (http://www.cs.princeton.edu/~rs/) and Kevin Wayne (http://www.cs.princeton.edu/~wayne/contact/)
 * from Princeton University for the movie data that the examples use
 * (http://introcs.cs.princeton.edu/java/data/movies-mpaa.txt).
 *
 * Useful links:
 * - http://blog.paumard.org/2014/11/12/java-8-and-streams-at-devoxx/
 * - http://blog.paumard.org/2014/11/15/a-week-in-devoxx/
 * - https://github.com/JosePaumard/jdk8-lambda-tour/blob/master/src/org/paumard/jdk8/Movies.java
 * - https://speakerdeck.com/glaforge/groovy-in-the-light-of-java-8-devoxx-2014
 * - https://speakerdeck.com/glaforge/groovy-in-2014-and-beyond-devoxx-2014
 * - https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html
 * - https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html
 * - https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html
 * - https://docs.oracle.com/javase/tutorial/collections/streams/index.html
 * - http://www.coreservlets.com/java-8-tutorial/
 * - https://leanpub.com/whatsnewinjava8/read
 * - http://blog.takipi.com/compiling-lambda-expressions-scala-vs-java-8/
 * - https://parleys.com/home
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class JavaMovieQueries {
    /**
     * Main method.
     *
     * @param arguments unused command-line arguments.
     */
    public static void main(final String[] arguments) {
        new JavaMovieQueries().runQueries();
    }

    /**
     * Compare several queries on a movies list using Java 7 versus Java 8 style.
     */
    private void runQueries() {
        final String filePath = "data/movies-mpaa.txt";
        //final String filePath = "data/movies-mpaa-test.txt";
        final List<Movie> movies = readMoviesAndActors(filePath);
        final Set<Actor> actors = movies.stream().flatMap(movie -> movie.getActors().stream()).collect(Collectors.toSet());
        System.out.println("Total number of actors: " + actors.size() + ".");

        printYearWithMostMovies(movies);
        printMostActiveActor(movies);
        printMostActiveActorInSingleYear(movies, actors);
    }

    /**
     * Print the year in which the most movies were released (and the movie count). Expected answers: 1997, when 700
     * movies were released.
     *
     * @param movies the movies.
     */
    private void printYearWithMostMovies(final List<Movie> movies) {
        // Java 7 style.
        final Map<Integer, Integer> yearToMovieCount = new HashMap<>();
        for (final Movie movie : movies) {
            final int year = movie.getReleaseYear();
            yearToMovieCount.put(year, 1 + (yearToMovieCount.containsKey(year) ? yearToMovieCount.get(year) : 0));
        }
        int maximumMovieCount = 0;
        int maximumMovieYear = 0;
        for (int year : yearToMovieCount.keySet()) {
            final int movieCount = yearToMovieCount.get(year);
            if (movieCount > maximumMovieCount) {
                maximumMovieCount = movieCount;
                maximumMovieYear = year;
            }
        }
        System.out.println();
        System.out.println("Java 7 - maximum movie year: " + maximumMovieYear + ", when " + maximumMovieCount
                           + " movies were released.");

        // Java 8 style.
        final Map.Entry<Integer, Long> yearAndCountEntry = movies.stream()
            .collect(Collectors.groupingBy(Movie::getReleaseYear, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .get();
        System.out.println("Java 8 - maximum movie year: " + yearAndCountEntry.getKey()
                           + ", when " + yearAndCountEntry.getValue() + " movies were released.");
    }

    /**
     * Print the actor that has worked on the most movies. Expected answer: Frank Welker, who worked on 92 movies.
     *
     * @param movies the movies.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void printMostActiveActor(final List<Movie> movies) {
        // Java 7 style.
        final Map<Actor, Long> actorToMovieCount = new HashMap<>();
        for (final Movie movie : movies)
            for (final Actor actor : movie.getActors())
                actorToMovieCount.put(actor, 1 + (actorToMovieCount.containsKey(actor) ? actorToMovieCount.get(actor) : 0));
        long maximumMovieCount = 0;
        Actor mostProductiveActor = null;
        for (final Actor actor : actorToMovieCount.keySet())
            if (actorToMovieCount.get(actor) > maximumMovieCount) {
                maximumMovieCount = actorToMovieCount.get(actor);
                mostProductiveActor = actor;
            }
        System.out.println();
        if (mostProductiveActor != null)
            System.out.println("Java 7 - most productive actor: " + mostProductiveActor.getFullName()
                               + ", who worked on " + actorToMovieCount.get(mostProductiveActor) + " movies.");
        else
            System.out.println("Java 7 - most productive actor not found?!?");

        // Java 8 style.
        final Map.Entry<Actor, Long> actorAndCountEntry = movies.stream()
            .flatMap(movie -> movie.getActors().stream())
            .collect(Collectors.toMap(Function.identity(), actor -> 1L, (count1, count2) -> count1 + count2))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue()).get();
        System.out.println("Java 8 - most productive actor: " + actorAndCountEntry.getKey().getFullName()
                           + ", who worked on " + actorAndCountEntry.getValue() + " movies.");
    }

    /**
     * Print the actor that has worked on the most movies in a single year. Expected answer: Phil Hawn, who worked on 24
     * movies in 1999.
     *
     * @param movies the movies.
     * @param actors the actors.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void printMostActiveActorInSingleYear(final List<Movie> movies, final Set<Actor> actors) {
        // Java 7 style.
        final Map<Actor, Map<Long, Long>> actorToMoviePerYearCount = new HashMap<>();
        for (final Movie movie : movies)
            for (final Actor actor : movie.getActors()) {
                final Map<Long, Long> yearToMovieCount = actorToMoviePerYearCount.containsKey(actor)
                                                         ? actorToMoviePerYearCount.get(actor) : new HashMap<>();
                final long year = movie.getReleaseYear();
                yearToMovieCount.put(year, 1 + (yearToMovieCount.containsKey(year) ? yearToMovieCount.get(year) : 0));
                actorToMoviePerYearCount.put(actor, yearToMovieCount);
            }
        long maximumMovieCount = 0;
        Actor mostProductiveActor = null;
        long mostProductiveYear = 0;
        for (final Actor actor : actorToMoviePerYearCount.keySet())
            for (final long year : actorToMoviePerYearCount.get(actor).keySet()) {
                if (actorToMoviePerYearCount.get(actor).get(year) > maximumMovieCount) {
                    maximumMovieCount = actorToMoviePerYearCount.get(actor).get(year);
                    mostProductiveActor = actor;
                    mostProductiveYear = year;
                }
            }
        System.out.println();
        if (mostProductiveActor != null)
            System.out.println("Java 7 - most productive actor in a single year: " + mostProductiveActor.getFullName()
                               + ", who worked on " + maximumMovieCount + " movies in " + mostProductiveYear + ".");
        else
            System.out.println("Java 7 - most productive actor in a single year not found?!?");

        // Java 8 style.
        // todo: rewrite to improve performance, for example by having the movies as the "outer loop".
        // Disabled the code below for the moment.
        System.out.println("Java 8 - most productive actor in a single year: to be optimized...");
        if (movies.size() == -1) {
            final Map.Entry<Actor, Map.Entry<Integer, Long>> actorYearCountEntry = actors.stream().parallel()
                .collect(Collectors.toMap(
                    Function.identity(),
                    actor -> movies.stream().filter(movie -> movie.getActors().contains(actor))
                        .collect(Collectors.groupingBy(Movie::getReleaseYear, Collectors.counting()))
                        .entrySet().stream().max(Map.Entry.comparingByValue()).get()))
                .entrySet().stream().max(Comparator.comparing(entry -> entry.getValue().getValue())).get();
            System.out.println("Most productive actor in a single year: " + actorYearCountEntry.getKey().getFullName());
            System.out.println("Year: " + actorYearCountEntry.getValue().getKey());
            System.out.println("Movie count: " + actorYearCountEntry.getValue().getValue());
            System.out.println();
        }
    }

    /**
     * Read the movies list from the input file.
     *
     * @param filePath the input file path.
     * @return the movies.
     */
    private List<Movie> readMoviesAndActors(final String filePath) {
        System.out.println("Reading movies and actors from file " + filePath + "...");
        final List<Movie> movies = new ArrayList<>();
        try {
            final BufferedReader dataReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = dataReader.readLine()) != null)
                movies.add(new Movie(line));
            dataReader.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total number of movies: " + movies.size() + ".");

        return movies;
    }
}
