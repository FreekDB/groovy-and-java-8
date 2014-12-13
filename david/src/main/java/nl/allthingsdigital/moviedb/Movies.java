/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.allthingsdigital.moviedb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author David van Enckevort <david@allthingsdigital.nl>
 */
public class Movies {
    private static final String CLINT = "Eastwood, Clint";

    public static void main(String[] args) throws IOException {

        Movies movies = new Movies();
        for (String file : args) {
            movies.run(file);
        }
    }

    private void run(String file) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(file));

        ActorConsumer actor = new ActorConsumer(200_000);
        MovieConsumer titles = new MovieConsumer(20_000);
        YearConsumer year = new YearConsumer(100);

        lines.peek(titles).peek(year).forEach(actor);

        System.out.println(actor.report());

        List<String> moviesForClint = actor.getMoviesForActor(CLINT);
        System.out.println(String.format("There are %d movies featuring %s", moviesForClint.size(), CLINT));
        System.out.println(String.format("Movies featuring %s: %s", CLINT,
                moviesForClint.stream().collect(Collectors.joining("\", \"", "\"", "\""))));

        System.out.println(titles.report());
        System.out.println(year.report());

    }

    private static class Pair<A, B> {
        private A first;
        private B second;

        public Pair(A firstValue, B secondValue) {
            first = firstValue;
            second = secondValue;
        }

        public A getFirst() {
            return first;
        }

        public void setFirst(A first) {
            this.first = first;
        }

        public B getSecond() {
            return second;
        }

        public void setSecond(B second) {
            this.second = second;
        }
    }

    private abstract class AbstractMovieDBConsumer implements Consumer<String> {
        abstract String report();

        protected Pair<Integer,String> getTitle(String line) {
            Integer year = getYear(line);
            String title = stripYear(line.split("/")[0]);
            return new Pair<>(year, title);
        }

        protected String stripYear(String line) {
            int lastIndexOf = line.lastIndexOf('(');
            if (line.lastIndexOf(')') > lastIndexOf) {
                String title = line.substring(0, lastIndexOf).trim();
                return title;
            }
            return line;
        }

        protected Integer getYear(final String line) {
            String title = line.split("/")[0];
            int start = title.lastIndexOf('(');
            int end = title.lastIndexOf(')');
            if (end > start) {
                String year = title.substring(start + 1, end + 1).trim();
                if (year.length() < 4) {
                    System.out.println(title);
                    return -1;
                }
                return Integer.parseInt(year.substring(0, 4));
            }
            return -1;
        }
    }

    private class ActorConsumer extends AbstractMovieDBConsumer {

        Map<String,List<String>> actors;
        Map<String,Map<Integer,Integer>> actorMoviesPerYear;

        private ActorConsumer(int expectedSize) {
            this.actorMoviesPerYear = new ConcurrentHashMap<>(expectedSize);
            this.actors = new ConcurrentHashMap<>(expectedSize);
        }

        @Override
        public void accept(String t) {
            int start = t.indexOf('/');
            List<String> actorList = Arrays.asList(t.substring(start + 1).split("/"));
            Pair<Integer,String> yearAndTitle = getTitle(t);
            String movie = yearAndTitle.getSecond();
            Integer year = yearAndTitle.getFirst();
            actorList.stream().forEach(A -> {
                actors.merge(A, getMovieList(movie),
                    (L1, L2) -> { L1.addAll(L2); return L1; } );
                actorMoviesPerYear.merge(A, getMovieMap(year),
                        (M1, M2) -> {
                            M1.put(year, M1.getOrDefault(year, 0) + M2.getOrDefault(year, 0));
                            return M1;
                        });
            });
        }

        private Map<Integer,Integer> getMovieMap(Integer year) {
            Map<Integer, Integer> map = new HashMap<>();
            map.put(year, 1);
            return map;
        }
        private List<String> getMovieList(String movie) {
            List<String> movies = new ArrayList<>();
            movies.add(movie);
            return movies;
        }

        public List<String> getMoviesForActor(final String actor) {
            return actors.get(actor);
        }

        public Pair<String,Integer> getActorWithMostMovies() {
            Pair<String,Integer> pair = new Pair<>("", 0);
            actors.forEach((actor, movies) -> {
                if (movies.size() > pair.getSecond()) {
                    pair.setFirst(actor);
                    pair.setSecond(movies.size());
                }
                });
            return pair;
        }

        public Pair<String,Integer> getActorWithMostMoviesInYear() {
            Pair<String,Integer> pair = new Pair<>("", 0);
            Pair<String,Integer> mostMovies = new Pair<>("", 0);
            actorMoviesPerYear.forEach((actor, moviesPerYear) -> {
                moviesPerYear.forEach((year, movies) -> {
                    if (movies > mostMovies.getSecond()) {
                        mostMovies.setSecond(movies);
                        pair.setFirst(actor);
                        pair.setSecond(year);
                    }
                });
            });
            return pair;
        }

        @Override
        public String report() {
            StringBuilder report = new StringBuilder();
            report.append(String.format("There are %d unique actors in the database.\n", actors.size()));
            Pair<String, Integer> pair = getActorWithMostMovies();
            report.append(String.format("The actor with most movies is %s with %d movies.\n",
                    pair.getFirst(), pair.getSecond()));
            pair = getActorWithMostMoviesInYear();
            String actor = pair.getFirst();
            Integer year = pair.getSecond();
            Integer movies = actorMoviesPerYear.get(actor).get(year);
            report.append(String.format("The most active actor in a year was %s with %d movies in %d",
                    actor, movies, year));
            return report.toString();
        }

    }

    private class MovieConsumer extends AbstractMovieDBConsumer {

        int count;
        Set<String> unique;

        private MovieConsumer(int expectedSize) {
            this.count = 0;
            this.unique = new HashSet<>(expectedSize);
        }

        @Override
        public void accept(String t) {
            count++;
            unique.add(getTitle(t).getSecond());
        }

        @Override
        public String report() {
            return String.format("There are %d movies in the database, with %d unique titles", count, unique.size());
        }
    }

    private class YearConsumer extends AbstractMovieDBConsumer {
        Map<Integer,Integer> map;

        private YearConsumer(int expectedSize) {
            this.map = new ConcurrentHashMap<>(expectedSize);
        }

        @Override
        public void accept(String t) {
            map.merge(getYear(t), 1, (A, B) -> {return A + B;});
        }

        @Override
        public String report() {
            Map<Integer, Integer> wc = new HashMap<>(map);
            final StringBuilder report = new StringBuilder();
            report.append(String.format("Movies without a year: %d\n", wc.get(-1) == null ? 0 : wc.get(-1)));
            wc.remove(-1);
            IntSummaryStatistics yearSummary = wc.keySet().stream().mapToInt(I -> I).summaryStatistics();
            report.append(String.format("First year in movie database: %d\n", yearSummary.getMin()));
            report.append(String.format("Last year in movie database: %d\n", yearSummary.getMax()));
            List<Integer> years = IntStream.rangeClosed(yearSummary.getMin(), yearSummary.getMax()).boxed().collect(Collectors.toList());
            years.removeAll(wc.keySet());
            String withoutMovies = years.stream().map(I -> { return String.valueOf(I); }).collect(Collectors.joining(", "));
            report.append(String.format("Years without movies: %s\n", withoutMovies));
            IntSummaryStatistics movieSummary = wc.values().stream().collect(Collectors.summarizingInt(I -> I));
            report.append(String.format("Most movies in a year: %d\n", movieSummary.getMax()));
            report.append(String.format("Least movies in a year: %d\n", movieSummary.getMin()));
            report.append(String.format("Average movies in a year: %01.2f\n", movieSummary.getAverage()));
            BestYear bestYear = new BestYear();
            wc.forEach(bestYear);
            report.append(String.format("Best year was %d with %d movies\n",
                    bestYear.getBestYear(), bestYear.getMaxMovies()));
            return report.toString();
        }
        private class BestYear implements BiConsumer<Integer, Integer> {
            private int maxMovies = 0;
            private int bestYear = 0;
            @Override
            public void accept(Integer year, Integer movies) {
                if (movies > maxMovies) {
                    bestYear = year;
                    maxMovies = movies;
                }
            }
            public int getBestYear() {
                return bestYear;
            }
            public int getMaxMovies() {
                return maxMovies;
            }
        }
    }
}
