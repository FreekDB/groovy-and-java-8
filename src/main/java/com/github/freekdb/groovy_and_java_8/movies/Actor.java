package com.github.freekdb.groovy_and_java_8.movies;

/**
 * Class representing actors.
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class Actor {
    private final String firstName;
    private final String lastName;

    public Actor(final String firstName, final String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
