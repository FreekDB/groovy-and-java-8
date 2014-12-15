package com.github.freekdb.groovy_and_java_8.sinks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Java version of Rob's approach with source, fork, and sinks.
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class JavaSourceAndSinks {
    public static void main(final String[] arguments) {
        new JavaSourceAndSinks().runSourceAndSinks();
    }

    private void runSourceAndSinks() {
        final Fork fork = new Fork();
        fork.addSink(new MovieCounter());
        fork.addSink(new DistinctTitles());
        fork.addSink(new ClintFinder());
        fork.addSink(new DistinctYears());
        fork.addSink(new YearsWithoutMovies());
        fork.addSink(new ProlificActor());
        fork.addSink(new ProlificActorYear());
        fork.addSink(new MostBusyYear());

        new FileSource(fork).read("data/movies-mpaa.txt");
        fork.report();
    }
}

interface Sink {
    void processLine(final String title, final int year, final String appendix, final List<String> actors);
    void report();
}

class Fork implements Sink {
    private final List<Sink> sinks = new ArrayList<>();

    public void addSink(final Sink sink) {
        sinks.add(sink);
    }

    @Override
    public void processLine(final String title, final int year, final String appendix, final List<String> actors) {
        sinks.forEach(sink -> sink.processLine(title, year, appendix, actors));
    }

    @Override
    public void report() {
        sinks.forEach(Sink::report);
    }
}

class FileSource {
    private Sink sink;

    public FileSource(final Sink sink) {
        this.sink = sink;
    }

    public void read(final String filePath) {
        try {
            Files.lines(new File(filePath).toPath()).forEach(line -> {
                final int startIndexYear = line.indexOf('(') + 1;
                final int endIndexYear = Math.min(line.indexOf(')', startIndexYear), startIndexYear + 4);
                final int startIndexActors = line.indexOf('/');
                final String title = line.substring(0, startIndexYear - 1).trim();
                final int releaseYear = Integer.parseInt(line.substring(startIndexYear, endIndexYear));
                final List<String> actors = Arrays.asList(line.substring(startIndexActors + 1).trim().split("/"));
                sink.processLine(title, releaseYear, "[not used]", actors);
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}

class MovieCounter implements Sink {
    protected String doc = "Total number of movies: %d.";
    protected long movieCount;

    @Override
    public void processLine(final String title, final int year, final String appendix, final List<String> actors) {
        movieCount++;
    }

    @Override
    public void report() {
        System.out.println(String.format(doc, movieCount));
    }
}

class DistinctTitles implements Sink {
    private Set<String> titles = new HashSet<>();

    @Override
    public void processLine(final String title, final int year, final String appendix, final List<String> actors) {
        titles.add(title);
    }

    @Override
    public void report() {
        System.out.println(String.format("Total number of distinct titles: %d.", titles.size()));
    }
}

class ClintFinder extends MovieCounter {
    public ClintFinder() {
        doc = "Clint Eastwood acted in %d movies.";
    }

    @Override
    public void processLine(final String title, final int year, final String appendix, final List<String> actors) {
        if (actors.contains("Eastwood, Clint"))
            movieCount++;
    }
}

class DistinctYears extends DistinctTitles {
    private Set<Integer> years = new HashSet<>();

    @Override
    public void processLine(final String title, final int year, final String appendix, final List<String> actors) {
        years.add(year);
    }

    @Override
    public void report() {
        System.out.println(String.format("Distinct years: %d.", years.size()));
    }
}

class YearsWithoutMovies extends MostBusyYear {
    @Override
    public void report() {
        final IntStream yearStream = yearToMovieCount.keySet().stream().mapToInt(Integer::intValue);
        final IntSummaryStatistics summaryStatistics = yearStream.summaryStatistics();
        final List<Integer> yearWithoutMovies = new ArrayList<>();
        for (int year = summaryStatistics.getMin(); year <= summaryStatistics.getMax(); year++)
            if (!yearToMovieCount.containsKey(year))
                yearWithoutMovies.add(year);
        System.out.println(String.format("Years without movies: %s.", yearWithoutMovies));
    }
}

class ProlificActor extends MostBusyYear {
    private Map<String, Integer> actorToMovieCount = new HashMap<>();

    @Override
    public void processLine(final String title, final int year, final String appendix, final List<String> actors) {
        for (final String actor : actors)
            actorToMovieCount.put(actor, actorToMovieCount.getOrDefault(actor, 0) + 1);
    }

    // todo: combine this with MostBusyYear.report?
    @Override
    public void report() {
        final List<String> busiestActors = new ArrayList<>();
        int highestCount = 0;
        for (final Map.Entry<String, Integer> actorToMovieCountEntry : actorToMovieCount.entrySet())
            if (actorToMovieCountEntry.getValue() > highestCount) {
                busiestActors.clear();
                busiestActors.add(actorToMovieCountEntry.getKey());
                highestCount = actorToMovieCountEntry.getValue();
            } else if (actorToMovieCountEntry.getValue() == highestCount)
                busiestActors.add(actorToMovieCountEntry.getKey());
        System.out.println(String.format("Most prolific actor(s): %s with %d movies.", busiestActors, highestCount));
    }
}

class ProlificActorYear extends MostBusyYear {
    private Map<String, Integer> actorYearToMovieCount = new HashMap<>();

    @Override
    public void processLine(final String title, final int year, final String appendix, final List<String> actors) {
        for (final String actor : actors) {
            final String key = actor + " in " + year;
            actorYearToMovieCount.put(key, actorYearToMovieCount.getOrDefault(key, 0) + 1);
        }
    }

    // todo: combine this with MostBusyYear.report?
    @Override
    public void report() {
        final List<String> busiestActors = new ArrayList<>();
        int highestCount = 0;
        for (final Map.Entry<String, Integer> actorYearToMovieCountEntry : actorYearToMovieCount.entrySet())
            if (actorYearToMovieCountEntry.getValue() > highestCount) {
                busiestActors.clear();
                busiestActors.add(actorYearToMovieCountEntry.getKey());
                highestCount = actorYearToMovieCountEntry.getValue();
            } else if (actorYearToMovieCountEntry.getValue() == highestCount)
                busiestActors.add(actorYearToMovieCountEntry.getKey());
        System.out.println(String.format("Most prolific actor(s) in one year: %s with %d movies.", busiestActors,
                                         highestCount));
    }
}

class MostBusyYear extends MovieCounter {
    protected Map<Integer, Integer> yearToMovieCount = new HashMap<>();

    @Override
    public void processLine(final String title, final int year, final String appendix, final List<String> actors) {
        yearToMovieCount.put(year, yearToMovieCount.getOrDefault(year, 0) + 1);
    }

    @Override
    public void report() {
        final List<Integer> busiestYears = new ArrayList<>();
        int highestCount = 0;
        for (final Map.Entry<Integer, Integer> yearToMovieCountEntry : yearToMovieCount.entrySet())
            if (yearToMovieCountEntry.getValue() > highestCount) {
                busiestYears.clear();
                busiestYears.add(yearToMovieCountEntry.getKey());
                highestCount = yearToMovieCountEntry.getValue();
            } else if (yearToMovieCountEntry.getValue() == highestCount)
                busiestYears.add(yearToMovieCountEntry.getKey());
        System.out.println(String.format("Busiest movie year(s): %s with %d releases.", busiestYears, highestCount));
    }
}
