package co.com.runtime.kmeans.datatypes;

import co.com.runtime.kmeans.interfaces.KmeansClusterItem;

import java.util.*;

public class Baloto implements KmeansClusterItem<Baloto> {
    private static final int BALOTO_SIZE = 6;

    public final Comparator<Baloto> DISTANCE_ORDER = new DistanceOrder();
    /**
     * Stores the Baloto results
     */
    private Double[] numbers;

    public Baloto(String s) {
        this.numbers = new Double[BALOTO_SIZE];
        StringTokenizer st = new StringTokenizer(s);
        for (int i = 0; i < this.numbers.length && st.hasMoreTokens(); i++) {
            this.numbers[i] = Double.valueOf(st.nextToken());
        }
        Arrays.sort(this.numbers);
    }

    private Baloto(Double[] average) {
        this.numbers = Arrays.copyOf(average, average.length);
        Arrays.sort(this.numbers);
    }

    public Baloto() {
        this.numbers = initializeArray();
    }

    @Override
    public double distance(Baloto other) {
        return euclideanDistance(other);
    }

    @Override
    public Baloto mean(Map<Baloto, Boolean> elements) {
        Set<Baloto> results = elements.keySet();
        return mean(results.toArray(new Baloto[results.size()]));
    }

    @Override
    public Baloto mean(Baloto[] elements) {
        Double[] average = initializeArray();
        for (Baloto baloto : elements) {
            for (int i = 0; i < average.length; i++) {
                average[i] = (average[i] == null) ? baloto.numbers[i]
                        : average[i] + baloto.numbers[i];
            }
        }
        if (elements.length != 0) {
            for (int i = 0; i < average.length; i++) {
                average[i] = average[i] / elements.length;
            }
        }
        return new Baloto(average);
    }

    private Double[] initializeArray() {
        Double[] average = new Double[BALOTO_SIZE];
        for (int i = 0; i < average.length; i++) {
            average[i] = 0D;
        }
        return average;
    }

    /**
     * Returns the Euclidean distance between two Baloto result sets
     *
     * @param other The Baloto instance against the distance is calculated
     * @return The Euclidean distance between the two Baloto instances
     */
    private double euclideanDistance(Baloto other) {
        double d = 0;
        for (int i = 0; i < numbers.length; i++) {
            d += Math.pow(numbers[i] - other.numbers[i], 2);
        }
        return Math.sqrt(d);
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Double number : this.numbers) {
            h += number.hashCode();
        }
        return h;
    }

    @Override
    public boolean equals(Object other) {
        boolean eq = true;
        if (other instanceof Baloto) {
            if (this.numbers.length == ((Baloto) other).numbers.length) {
                for (int i = 0; i < this.numbers.length && eq; i++) {
                    if (!this.numbers[i].equals(((Baloto) other).numbers[i])) {
                        eq = false;
                    }
                }
            } else {
                eq = false;
            }
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.numbers.length; i++) {
            sb.append(String.format("%02.0f", this.numbers[i]));
            if (i != this.numbers.length - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private class DistanceOrder implements Comparator<Baloto> {
        @Override
        public int compare(Baloto b1, Baloto b2) {
            Double difference = distance(b2) - distance(b1);
            if (difference > 0D) {
                return -1;
            } else if (difference < 0D) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
