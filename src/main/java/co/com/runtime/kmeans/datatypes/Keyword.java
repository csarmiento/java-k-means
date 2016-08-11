package co.com.runtime.kmeans.datatypes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import co.com.runtime.kmeans.interfaces.KmeansClusterItem;

public class Keyword implements KmeansClusterItem<Keyword> {
    private static final boolean ALLOW_ALL_WORDS = false;
    private static final double  LOG2            = Math.log(2);

    public final Comparator<Keyword> DISTANCE_ORDER = new DistanceOrder();

    /**
     * Words that can distort the semantic matching between keywords
     */
    private final static String[] cleanningSet = {"@", "#", "-", "*", "&",
            "%", ".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "_",
            "0f", "a", "in", "of", "for", "and", "the", "to", "at", "on", "or",
            "by"};
    private final static Set<String> removableStrings;

    static {
        removableStrings = new TreeSet<>();
        removableStrings.addAll(Arrays.asList(cleanningSet));
    }

    /**
     * Stores words and occurrences of each word
     */
    private HashMap<String, Double> words;

    /**
     * Stores the original phrase before cleanning, used only in the
     * Keyword(string phrase) constructor
     */
    private String originalPhrase;

    public Keyword() {
        words = new HashMap<>();
    }

    public Keyword(String phrase) {
        originalPhrase = phrase;
        words = new HashMap<>();
        StringTokenizer st = new StringTokenizer(phrase);
        while (st.hasMoreTokens()) {
            addWord(st.nextToken().toLowerCase());
        }
    }

    private Keyword(HashMap<String, Double> words) {
        this.words = words;
    }

    public void addWord(String word) {
        if (ALLOW_ALL_WORDS) {
            add(word);
        } else if (!removableStrings.contains(word)) {
            add(word);
        }
    }

    private void add(String word) {
        Double occurrences = words.get(word);
        if (occurrences != null) {
            words.put(word, occurrences + 1);
        } else {
            words.put(word, 1D);
        }
    }

    private double magnitude() {
        double mag = 0;
        for (String word : words.keySet()) {
            mag += Math.pow(words.get(word), 2);
        }
        return Math.sqrt(mag);
    }

    private double dotProduct(Keyword other) {
        double result = 0;
        for (String word : words.keySet()) {
            Double occursInOther = other.words.get(word);
            if (occursInOther != null) {
                result += words.get(word) * occursInOther;
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.insidevault.exercise6.KmeansClusterItem#distance(java.lang.Object)
     */
    public double distance(Keyword other) {
        return angle(other);
    }

    /**
     * Returns the Euclidean distance between two keywords(represented as
     * n-dimensional vectors)
     *
     * @param other The keyword against the distance is calculated
     * @return The Euclidean distance between the two keywords
     */
    @SuppressWarnings("unused")
    private double euclideanDistance(Keyword other) {
        double d = 0;
        TreeSet<String> keys = new TreeSet<>();
        keys.addAll(words.keySet());
        keys.addAll(other.words.keySet());

        for (String word : keys) {
            Double val1 = words.get(word);
            Double val2 = other.words.get(word);
            val1 = (val1 == null) ? 0 : val1;
            val2 = (val2 == null) ? 0 : val2;

            d += Math.pow(val1 - val2, 2D);
        }
        return Math.sqrt(d);
    }

    /**
     * Returns the angle between two keywords(represented as n-dimensional
     * vectors)
     *
     * @param other The keyword against the angle is calculated
     * @return The angle between two keywords. The smaller the angle, the closer
     * the keywords are.
     */
    private double angle(Keyword other) {
        return Math.acos(dotProduct(other)
                / (this.magnitude() * other.magnitude()))
                * (180 / Math.PI);
    }

    /**
     * Returns the Jensen-Shannon divergence between two keywords
     *
     * @param other The keyword against the Jensen-Shannon divergence is
     *              calculated
     * @return The Jensen-Shannon divergence between two keywords
     */
    @SuppressWarnings("unused")
    private double jensenShannonDivergence(Keyword other) {
        TreeSet<String> keys = new TreeSet<>();
        keys.addAll(words.keySet());
        keys.addAll(other.words.keySet());
        HashMap<String, Double> average = new HashMap<>();

        for (String word : keys) {
            Double val1 = words.get(word);
            Double val2 = other.words.get(word);
            val1 = (val1 == null) ? 0 : val1;
            val2 = (val2 == null) ? 0 : val2;

            average.put(word, (val1 + val2) / 2D);
        }

        Keyword avgKeyword = new Keyword(average);
        return (kullbackLeiblerDivergence(avgKeyword) + other
                .kullbackLeiblerDivergence(avgKeyword)) / 2;
    }

    /**
     * Used to calculate the JSD
     *
     * @param other The keyword against the Kullback-Leibler divergence is
     *              calculated
     * @return The Kullback-Leibler divergence between two keywords
     */
    private double kullbackLeiblerDivergence(Keyword other) {
        double klDiv = 0D;
        Set<String> keys = other.words.keySet();
        for (String word : keys) {
            Double val1 = words.get(word);
            Double val2 = other.words.get(word);
            val1 = (val1 == null) ? 0 : val1;
            val2 = (val2 == null) ? 0 : val2;

            if (val1 != 0D && val2 != 0D) {
                klDiv += val1 * Math.log(val1 / val2);
            }
        }
        return klDiv / LOG2;
    }

    @Override
    public String toString() {
        if (originalPhrase == null) {
            StringBuilder sb = new StringBuilder();
            for (String word : words.keySet()) {
                sb.append(word);
                sb.append(':');
                sb.append(words.get(word));
                sb.append(' ');
            }
            return sb.toString();
        } else {
            return originalPhrase;
        }
    }

    @Override
    public Keyword mean(Map<Keyword, Boolean> elements) {
        Set<Keyword> keywords = elements.keySet();
        return mean(keywords.toArray(new Keyword[keywords.size()]));
    }

    @Override
    public Keyword mean(Keyword[] elements) {
        HashMap<String, Double> average = new HashMap<>();
        for (Keyword kw : elements) {
            for (String word : kw.words.keySet()) {
                Double val = kw.words.get(word);
                Double avgVal = average.get(word);
                val = (val == null) ? 0 : val;
                avgVal = (avgVal == null) ? 0 : avgVal;
                average.put(word, avgVal + val);
            }
        }
        for (String word : average.keySet()) {
            Double avgVal = average.get(word);
            avgVal = (avgVal == null) ? 0 : avgVal;
            average.put(word, avgVal / elements.length);
        }
        return new Keyword(average);
    }

    @Override
    public int hashCode() {
        return words.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Keyword && words.equals(((Keyword) other).words);
    }

    private class DistanceOrder implements Comparator<Keyword> {
        @Override
        public int compare(Keyword kw1, Keyword kw2) {
            Double difference = distance(kw2) - distance(kw1);
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
