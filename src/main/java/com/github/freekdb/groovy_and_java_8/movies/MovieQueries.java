package com.github.freekdb.groovy_and_java_8.movies;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class with movie queries.
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class MovieQueries {
    public static void main(final String[] args) {
        new MovieQueries().runQueries();
    }

    private void runQueries() {
        final List<Movie> movies = readData("data/movies-mpaa.txt");
        System.out.println("movies.size(): " + movies.size());

        // In which year were the most movies released?
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

        // Most seen actor? Frank Welker
        // Most seen actor in one year? Phil Hawn: 24 movies in 1999
    }

    private List<Movie> readData(final String filePath) {
        final List<Movie> movies = new ArrayList<>();
        try {
            final BufferedReader dataReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = dataReader.readLine()) != null) {
                movies.add(new Movie(line));
            }
            dataReader.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return movies;
    }
}
