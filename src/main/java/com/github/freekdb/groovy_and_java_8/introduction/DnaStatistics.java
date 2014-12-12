package com.github.freekdb.groovy_and_java_8.introduction;

/**
 * Class to determine some DNA statistics.
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class DnaStatistics {
    private long[] counts = new long[4];

    public DnaStatistics() {
    }

    public void accept(final int value) {
        final int baseIndex = "ACGT".indexOf(value);
        if (baseIndex > -1)
            counts[baseIndex]++;
    }

    public void combine(final DnaStatistics other) {
        for (int baseIndex = 0; baseIndex < counts.length; baseIndex++)
            counts[baseIndex] += other.counts[baseIndex];
    }

    public void printStatistics() {
        System.out.println("A: " + counts[0] + ", C: " + counts[1] + ", G: " + counts[2] + ", T: " + counts[3]);
        final long atCount = counts[0] + counts[3];
        final long gcCount = counts[1] + counts[2];
        System.out.println("GC-content: " + (100.0 * gcCount) / (atCount + gcCount) + "%");
    }
}
