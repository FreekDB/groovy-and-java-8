package com.github.freekdb.groovy_and_java_8.movies;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing movies.
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class Movie {
    private final String title;
    private final int releaseYear;
    private final List<Actor> actors;

    public Movie(final String title, final int releaseYear) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.actors = new ArrayList<>();
    }

    public Movie(final String dataLine) {
        final int startIndexYear = dataLine.indexOf('(') + 1;
        final int endIndexYear = Math.min(dataLine.indexOf(')', startIndexYear), startIndexYear + 4);
        final int startIndexActors = dataLine.indexOf('/');
        this.title = dataLine.substring(0, startIndexYear - 1).trim();
        this.releaseYear = Integer.parseInt(dataLine.substring(startIndexYear, endIndexYear));
        this.actors = new ArrayList<>();
        for (final String actorName : dataLine.substring(startIndexActors + 1).split("/")) {
            final int commaIndex = actorName.indexOf(',');
            if (commaIndex != -1)
                this.actors.add(new Actor(actorName.substring(commaIndex + 1).trim(),
                                          actorName.substring(0, commaIndex).trim()));
            else {
                this.actors.add(new Actor(actorName, ""));
                //System.err.println("Movie.Movie: actor name without a comma: " + actorName);
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public List<Actor> getActors() {
        return actors;
    }
}
