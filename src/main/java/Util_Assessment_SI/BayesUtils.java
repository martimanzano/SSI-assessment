package Util_Assessment_SI;

import Assessment.DTOSIAssessment;
import Assessment.DTOSICategory;
import Assessment.SIAssessment;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.range.Range;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class BayesUtils {
    public static Map<List<String>, Integer> getCommonConfigurations(String projectId, Constants.QMLevel QMType,
                                                                     String[] elements, String[][] categories,
                                                                     double[][] intervals, LocalDate from, LocalDate to)
            throws IOException {
        //construir diccionari
        List<String> elementsArray = Arrays.asList(elements);
        Map<List<String>, Integer> observedCombinations = new HashMap<>();

        for (LocalDate currentDay = from; !currentDay.isAfter(to); currentDay = currentDay.plusDays(1)) {
            SearchResponse sr = Queries.getFilteredDay(projectId, QMType, currentDay, elements);
            SearchHits hits = sr.getHits();
            String[] combination = new String[elements.length];
            for (SearchHit hit : hits) {
                //processar factor i correspondencia value - categoria
                Map<String, Object> hitSource = hit.getSource();
                String element = Queries.getStringFromMap(hitSource, QMtoID(QMType));
                float value = Float.parseFloat(Queries.getStringFromMap(hitSource, Constants.VALUE));
                // construir combinatoria
                int elementIndex = elementsArray.indexOf(element);
                combination[elementIndex] = discretize(value, intervals[elementIndex], categories[elementIndex]);// combinatoria construida
            }
            System.out.println("Date " + currentDay.toString() + " : " + Arrays.asList(combination).toString());
            observedCombinations.merge(Arrays.asList(combination), 1, Integer::sum);
        }
        return observedCombinations;
    }

    public static Map<List<String>, Integer> getChildCommonConfigurations(String projectId, Constants.QMLevel QMType,
                                                                     String[] parentElements, String[][] parentCategories,
                                                                     double[][] parentIntervals, String[] childElements,
                                                                          String BNPath, LocalDate from, LocalDate to)
            throws Exception {
        //construir diccionari
        List<String> elementsArray = Arrays.asList(parentElements);
        Map<List<String>, Integer> observedCombinations = new HashMap<>();
        File file = new File(BNPath);
        for (LocalDate currentDay = from; !currentDay.isAfter(to); currentDay = currentDay.plusDays(1)) {
            SearchResponse sr = Queries.getFilteredDay(projectId, QMType, currentDay, parentElements);
            SearchHits hits = sr.getHits();
            Map<String, String> parentStates = new HashMap<>();
            for (SearchHit hit : hits) {
                //processar factor i correspondencia value - categoria
                Map<String, Object> hitSource = hit.getSource();
                String element = Queries.getStringFromMap(hitSource, QMtoID(QMType));
                float value = Float.parseFloat(Queries.getStringFromMap(hitSource, Constants.VALUE));
                int elementIndex = elementsArray.indexOf(element);
                parentStates.put(element, discretize(value, parentIntervals[elementIndex], parentCategories[elementIndex]));
            }
            if (parentStates.size() > 0) {
                String[] childCombination = new String[childElements.length];
                int childElementIndex = 0;
                for (String childElement : childElements) {
                    DTOSIAssessment childAssessment = SIAssessment.AssessSI(childElement, parentStates, file);
                    childCombination[childElementIndex] = (childAssessment.mostProbableCategory());

                    childElementIndex++;
                }
                observedCombinations.merge(Arrays.asList(childCombination), 1, Integer::sum);
            }
        }
        return observedCombinations;
    }

    public static void buildChildPastStates(String projectId, Constants.QMLevel QMType,
                                            String[] parentElements, String[][] parentCategories,
                                            double[][] parentIntervals, String childElement,String[] childCategories,
                                            String BNPath, LocalDate from, LocalDate to, FileWriter csvWriter,
                                            Boolean writeValues)
            throws Exception {
        //construir diccionari
        List<String> elementsArray = Arrays.asList(parentElements);
        File file = new File(BNPath);

        csvWriter.append("Day,");
        csvWriter.append(String.join(",", parentElements));
        csvWriter.append(",");
        csvWriter.append(String.join(",", childCategories));
        csvWriter.append(",High.Prob.\n");
        for (LocalDate currentDay = from; !currentDay.isAfter(to); currentDay = currentDay.plusDays(1)) {
            SearchResponse sr = Queries.getFilteredDay(projectId, QMType, currentDay, parentElements);
            SearchHits hits = sr.getHits();
            Map<String, String> parentStates = new HashMap<>();
            Map<String, String> parentValuesAndStates = new HashMap<>();
            for (SearchHit hit : hits) {
                //processar factor i correspondencia value - categoria
                Map<String, Object> hitSource = hit.getSource();
                String element = Queries.getStringFromMap(hitSource, QMtoID(QMType));
                float value = Float.parseFloat(Queries.getStringFromMap(hitSource, Constants.VALUE));
                int elementIndex = elementsArray.indexOf(element);
                parentStates.put(element, discretize(value, parentIntervals[elementIndex], parentCategories[elementIndex]));
                parentValuesAndStates.put(element, String.format(Locale.US,"%.2f", value) + " (" +
                        discretize(value, parentIntervals[elementIndex], parentCategories[elementIndex]) + ")");
            }
            if (parentStates.size() > 0) {
                DTOSIAssessment childAssessment = SIAssessment.AssessSI(childElement, parentStates, file);
                String[] parentStatesToWrite = (writeValues == Boolean.TRUE) ?
                        getParentStates(parentElements, parentValuesAndStates) :
                        getParentStates(parentElements, parentStates);
                String[] dayProbabilities = childAssessment.buildDayProbabilities();
                String row = new StringBuilder().append(currentDay).append(",").
                        append(String.join(",",parentStatesToWrite)).append(",").
                        append(String.join(",", dayProbabilities)).toString();
                csvWriter.append(row);
                csvWriter.append("\n");
            }
        }
    }

    public static DTOSIAssessment[] computeAndReturnChildStates(String projectId, Constants.QMLevel QMType,
                                            String[] parentElements, String[][] parentCategories,
                                            double[][] parentIntervals, String childElement, File BNFile,
                                                                         LocalDate from, LocalDate to)
            throws Exception {
        ArrayList<DTOSIAssessment> ret = new ArrayList<>();
        //construir diccionari
        List<String> elementsArray = Arrays.asList(parentElements);

        for (LocalDate currentDay = from; !currentDay.isAfter(to); currentDay = currentDay.plusDays(1)) {
            SearchResponse sr = Queries.getFilteredDay(projectId, QMType, currentDay, parentElements);
            SearchHits hits = sr.getHits();
            Map<String, String> parentStates = new HashMap<>();
            for (SearchHit hit : hits) {
                //processar factor i correspondencia value - categoria
                Map<String, Object> hitSource = hit.getSource();
                String element = Queries.getStringFromMap(hitSource, QMtoID(QMType));
                float value = Float.parseFloat(Queries.getStringFromMap(hitSource, Constants.VALUE));
                int elementIndex = elementsArray.indexOf(element);
                parentStates.put(element, discretize(value, parentIntervals[elementIndex], parentCategories[elementIndex]));
            }
            if (parentStates.size() > 0) {
                DTOSIAssessment childAssessment = SIAssessment.AssessSI(childElement, parentStates, BNFile);
                ret.add(childAssessment);
            }
        }
        return ret.toArray(new DTOSIAssessment[ret.size()]);
    }

    public static int computeCoincidences(DTOSIAssessment[] assessments1, DTOSIAssessment[] assessments2) {
        int retCoincidences = 0;
        int minLength = Math.min(assessments1.length, assessments2.length);

        for (int i = 0; i < minLength; i++) {
            if (assessments1[i].isEqualAssessment(assessments2[i])) {
                i++;
            }
        }
        return retCoincidences;
    }

    public static double computeAverageDistance(DTOSIAssessment[] assessments1, DTOSIAssessment[] assessments2) {
        double accumulatedDistance = 0;
        int minLength = Math.min(assessments1.length, assessments2.length);
        int maxLength = Math.max(assessments1.length, assessments2.length);

        for (int i = 0; i < minLength; i++) {
            accumulatedDistance += assessments1[i].computeDistanceTo(assessments2[i]);
        }
        return (double) accumulatedDistance/maxLength;
    }

    private static String[] getParentStates(String parentElements[], Map<String, String> parentStatesMap) {
        String ret[] = new String[parentElements.length];
        for (int i = 0; i < parentElements.length; i++) {
            ret[i] = parentStatesMap.get(parentElements[i]);
        }
        return ret;
    }

    public static Map<String, Map<String, Float>> getFrequencyQuantification(String projectId, Constants.QMLevel QMType,
                                                                             String[] elements, double[][] intervals,
                                                                             LocalDate from, LocalDate to)
            throws IOException {
        Map<String, Map<String, Float>> ret = new LinkedHashMap<>();
        int elementIndex = 0;
        for (String element : elements) {
            SearchResponse sr = Queries.getFrequencies(projectId, QMType, element, from, to, intervals[elementIndex]);
            long total = sr.getHits().totalHits;
            Range rangeAggregation = sr.getAggregations().get("categoryranges");
            Map<String, Float> dictFrequencies = new LinkedHashMap<>();
            for (Range.Bucket rangebucket : rangeAggregation.getBuckets()) {
                String key = String.format("%.2f",rangebucket.getFrom()) + "-" + String.format("%.2f",rangebucket.getTo());//rangebucket.getKeyAsString();          // bucket key
                long docCount = rangebucket.getDocCount();            // Doc count
                dictFrequencies.put(key, (float)docCount/total);
                System.err.println("Key: " + key + ", Doc Count: " + docCount);
            }
            ret.put(element, dictFrequencies);
            elementIndex++;
        }
        return ret;
    }

    public static String discretize(double value, double[] intervals, String[] categories) {
        int i = 0;
        if (Double.isNaN(value)) return null;
        if (value < intervals[0]) return categories[0];
        else if (value >= intervals[intervals.length-1]) return categories[categories.length-1];
        else {
            while (value >= intervals[i]) {
                //if (value < intervals[i]) break;
                i++;
            }
        }
        return categories[i];
    }

    public static double[] makeEqualWidthIntervals(int numBins) {
        double min = 0d;
        double max = 1d;

        double width = (max - min) / numBins;

        double[] intervals = new double[numBins * 2];
        intervals[0] = min;
        intervals[1] = min + width;
        for (int i = 2; i < intervals.length - 1; i += 2) {
            intervals[i] = Math.nextUp(intervals[i - 1]);
            intervals[i + 1] = intervals[i] + width;
        }
        intervals[intervals.length-1] = max;
        return intervals;
    }

    public static float[] makeEqualFrequencyIntervals(int numBins, String project, Constants.QMLevel QMType,
                                                       String[] elements, LocalDate from, LocalDate to) throws IOException {
        float min = 0f;
        float max = 1f;
        /**/
        //numBins = 3;
        /**/
        float[] intervals = new float[numBins * 2];

        SearchResponse sr = Queries.getElementsAggregations(project, QMType, elements, from, to);
        SearchHits hits = sr.getHits();
        int totalHits = (int) hits.getTotalHits();
        /**/
        //totalHits = 9;
        /**/
        Float[] sortedValues = new Float[totalHits];
        int index = 0;
        for (SearchHit hit : hits.getHits()) {
            float value = Float.parseFloat(Queries.getStringFromMap(hit.getSourceAsMap(), Constants.VALUE));
            sortedValues[index] = value;
            index++;
        }
        /**/
        //sortedValues = new Float[]{0.01F,0.04F,0.12F,0.16F,0.16F,0.18F,0.24F,0.26F,0.28F};
        /**/
        int groupsSize = sortedValues.length / numBins;
        intervals[0] = min;
        for (int i = 1, indexBins = 1; indexBins < numBins; i+=2, indexBins++) {
            intervals[i] = mean(sortedValues[(groupsSize * indexBins) - 1], sortedValues[groupsSize * indexBins]);
            if (i < intervals.length -1) {
                intervals[i + 1] = intervals[i];
            }
        }
        intervals[intervals.length-1] = max;
        return intervals;
    }

    private static float mean(float d1, float d2) {
        return (d1 + d2) / 2;
    }

    public static String QMtoID(Constants.QMLevel QMType) {
        switch (QMType) {
            case strategic_indicators:
                return Constants.STRATEGIC_INDICATOR_ID;
            case factors:
                return Constants.FACTOR_ID;
            default:
            case metrics:
                return Constants.METRIC_ID;
        }
    }
}
