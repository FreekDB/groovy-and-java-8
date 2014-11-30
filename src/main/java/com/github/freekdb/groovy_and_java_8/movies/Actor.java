package com.github.freekdb.groovy_and_java_8.movies;

import java.util.Objects;

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

//    public String getFirstName() {
//        return firstName;
//    }

//    public String getLastName() {
//        return lastName;
//    }

    public String getFullName() {
        return firstName + (lastName != null ? " " + lastName : "");
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public boolean equals(final Object that) {
        return that instanceof Actor
               && Objects.equals(firstName, ((Actor) that).firstName)
               && Objects.equals(lastName, ((Actor) that).lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }
}
