package co.com.runtime.kmeans.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.com.runtime.kmeans.DataAnalysis;
import co.com.runtime.kmeans.datatypes.Keyword;
import junit.framework.TestCase;

public class KeywordTest extends TestCase {

    public void testMean() {
        Keyword kw0 = new Keyword("adams state college alamosa co");
        Keyword kw1 = new Keyword("rush university college of nursing");
        Keyword kw2 = new Keyword("rush university nurse practitioner program");
        Keyword kw3 = new Keyword("lawson state community college bessemer al");

        Map<Keyword, Boolean> elements = new HashMap<Keyword, Boolean>();
        elements.put(kw1, true);
        elements.put(kw2, true);

        Keyword mean = kw3.mean(elements);
        System.out.println(mean);

        elements.put(kw0, true);
        elements.put(kw3, true);

        mean = kw3.mean(elements);
        System.out.println(mean);
    }

    public void testTimeMeans() throws IOException {
        File input = new File(
                "D:/workspace_kepler/ExerciseIV6/IV6 - RAW keywords.txt");
        // final File input = new
        // File("J:/iv/ExerciseIV6/IV6 - RAW keywords.txt");
        Keyword[] keywords = DataAnalysis.loadKeywords(input);
        Map<Keyword, Boolean> elements = new HashMap<Keyword, Boolean>();
        for (Keyword kw : keywords) {
            elements.put(kw, true);
        }

        Keyword dummy = new Keyword();
        long start = System.currentTimeMillis();
        Keyword mean1 = dummy.mean(elements);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(mean1);
        System.out.println("Time elapsed new mean: " + elapsed);
    }

    public void testOrderComparator() {
        Keyword kw1 = new Keyword("rush university college of nursing");
        Keyword kw2 = new Keyword("rush university nurse practitioner program");

        Map<Keyword, Boolean> elements = new HashMap<Keyword, Boolean>();
        elements.put(kw1, true);
        elements.put(kw2, true);

        Keyword mean = kw1.mean(elements);
        System.out.println(mean);
        System.out.println(mean.distance(kw1));
        System.out.println(mean.distance(kw2));

        assertTrue(mean.DISTANCE_ORDER.compare(kw1, kw2) > 0);
        System.out.println(mean.DISTANCE_ORDER.compare(kw1, kw2));
    }
}
