package com.github.freekdb.groovy_and_java_8.introduction;

import java.math.BigInteger;

/**
 * Class with a BigInteger and an unused block of memory.
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
class BigObject implements Comparable<BigObject> {
    public static final BigObject ZERO = new BigObject(0L);

    @SuppressWarnings("UnusedDeclaration")
    private final int[] buffer = new int[1024];
    private BigInteger bigNumber;

    public BigObject(final BigInteger bigNumber) {
        this.bigNumber = bigNumber;
    }

    public BigObject(final Long number) {
        this.bigNumber = new BigInteger(number.toString());
    }

    public BigInteger getBigNumber() {
        return bigNumber;
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") final BigObject that) {
        return that != null ? bigNumber.compareTo(that.bigNumber) : -1;
    }

    public BigObject add(final BigObject that) {
        return new BigObject(bigNumber.add(that.bigNumber));
    }
}
