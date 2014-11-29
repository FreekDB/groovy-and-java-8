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
 * - https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class MovieQueries {
    /**
     * Main method.
     *
     * @param arguments unused command-line arguments.
     */
    public static void main(final String[] arguments) {
        new MovieQueries().runQueries();
    }

    /**
     * Compare several queries on a movies list using Java 7 versus Java 8 style.
     */
    private void runQueries() {
        final List<Movie> movies = readMoviesAndActors("data/movies-mpaa.txt");
        //final List<Movie> movies = readMoviesAndActors("data/movies-mpaa-test.txt");
        final Set<Actor> actors = movies.stream().flatMap(movie -> movie.getActors().stream()).collect(Collectors.toSet());

        System.out.println("Number of movies: " + movies.size());
        System.out.println("Number of actors: " + actors.size());
        System.out.println();

        if (movies.size() == -1) {
            printYearWithMostMovies(movies);
            printMostActiveActor(movies, actors);
        }
        printMostActiveActorInSingleYear(movies, actors);
    }

    /**
     * Read the movies list from the input file.
     *
     * @param filePath the input file path.
     * @return the movies.
     */
    private List<Movie> readMoviesAndActors(final String filePath) {
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
        return movies;
    }

    /**
     * Print the year in which the most movies were released (and the movie count). Expected answers: 1997 and 700.
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
        System.out.println("maximumMovieYear: " + maximumMovieYear);
        System.out.println("yearToMovieCount.get(maximumMovieYear): " + yearToMovieCount.get(maximumMovieYear));
        System.out.println();

        // Java 8 style.
        final Map.Entry<Integer, Long> yearAndCountEntry = movies.stream()
            .collect(Collectors.groupingBy(Movie::getReleaseYear, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .get();
        System.out.println("maximumMovieYear: " + yearAndCountEntry.getKey());
        System.out.println("yearToMovieCount.get(maximumMovieYear): " + yearAndCountEntry.getValue());
        System.out.println();
    }

    /**
     * Print the actor that has worked on the most movies. Expected answer: Frank Welker who worked on 92 movies.
     *
     * @param movies the movies.
     * @param actors the actors.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void printMostActiveActor(final List<Movie> movies, final Set<Actor> actors) {
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
        if (mostProductiveActor != null) {
            System.out.println("Most productive actor: " + mostProductiveActor.getFullName());
            System.out.println("Movie count: " + actorToMovieCount.get(mostProductiveActor));
        } else
            System.out.println("Most productive actor: not found");
        System.out.println();

        // Java 8 style.
        // todo: rewrite to improve performance, for example by having the movies as the "outer loop".
        // Disabled the code below for the moment.
        if (movies.size() == -1) {
            System.out.println("MovieQueries.printMostActiveActor - 1");
            final Map<Actor, Long> actorLongMap = actors.stream().parallel()
                //final Map<Actor, Long> actorLongMap = actors.parallelStream()
                .collect(
                    Collectors.toMap(
                        Function.identity(),
                        actor -> movies.stream().filter(movie -> movie.getActors().contains(actor)).count()
                        //Actor::getFullName
                    )
                );
            System.out.println("actorLongMap: " + actorLongMap);

            System.out.println("MovieQueries.printMostActiveActor - 2");
            final Map<Actor, Long> actorToCountMap = actors.stream().parallel()
                .collect(Collectors.toMap(Function.identity(),
                                          actor -> movies.stream().filter(movie -> movie.getActors().contains(actor)).count()));
            System.out.println("actorToCountMap: " + actorToCountMap);
            final Map.Entry<Actor, Long> actorAndCountEntry = actorToCountMap
                .entrySet().stream().max(Map.Entry.comparingByValue()).get();
            System.out.println("Most productive actor: " + actorAndCountEntry.getKey().getFullName());
            System.out.println("Movie count: " + actorAndCountEntry.getValue());
            System.out.println();
        }
    }

    /**
     * Print the actor that has worked on the most movies in a single year. Expected answer: Phil Hawn who worked on 24
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
        if (mostProductiveActor != null) {
            System.out.println("Most productive actor in a single year: " + mostProductiveActor.getFullName());
            System.out.println("Year: " + mostProductiveYear);
            System.out.println("Movie count: " + actorToMoviePerYearCount.get(mostProductiveActor).get(mostProductiveYear));
        } else
            System.out.println("Most productive actor: not found");
        System.out.println();

        // Java 8 style.
        // todo: rewrite to improve performance, for example by having the movies as the "outer loop".
        // Disabled the code below for the moment.
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
}
