package Util_Assessment_SI;


public class Constants {
    //IDs FOR ELEMENT AGGREGATION, the name of the attribute that is used as element ID
    static final String METRIC_ID = "metric";
    public static final String FACTOR_ID = "factor";
    static final String STRATEGIC_INDICATOR_ID = "strategic_indicator";

    //FIELDS//
    // related to the evaluation
    public static final String EVALUATION_DATE = "evaluationDate";
    public static final String VALUE = "value";


    //INDEXES//
    public static String PATH = "";             // public, it is accessed from outside of the package
    public static String INDEX_PREFIX = "poc."; // public, it is accessed from outside of the package
    static final String INDEX_STRATEGIC_INDICATORS = "strategic_indicators";
    public static final String INDEX_FACTORS = "factors";
    public static final String INDEX_METRICS = "metrics";
    public static final String INDEX_RELATIONS = "relations";

    //OTHERS//
    public enum QMLevel {metrics, factors, strategic_indicators, relations}

}
