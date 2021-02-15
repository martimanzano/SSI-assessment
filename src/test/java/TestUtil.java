import Assessment.SIAssessment;
import Assessment.DTOSIAssessment;
import Util_Assessment_SI.BayesUtils;

import java.io.File;
import java.util.Map;

public class TestUtil {

    public static void main(String[] args) {
        try {
/*            String dneFiles = "src/test/java/";

            // TEST 1
            Map<String, String> factorStates = new HashMap<>();
            factorStates.put("blockingcode", "Low");
            factorStates.put("softwarestability", "High");
            factorStates.put("codequality", "High");

            assessmentTest(dneFiles + "WSA_Blocking.dne", "blocking", factorStates);

            // TEST 2
            factorStates = new HashMap<>();
            factorStates.put("CodeQuality", "VeryHigh");
            factorStates.put("TestingStatus", "High");

            assessmentTest(dneFiles + "WSA_ProductQuality2factors.dne", "ProductQuality", factorStates);

            // TEST 3
            factorStates = new HashMap<>();
            factorStates.put("codequality", "Low");
            factorStates.put("softwarestability", "Medium");
            factorStates.put("testingstatus", "High");

            assessmentTest(dneFiles + "WSA_ProductQuality.dne", "productquality", factorStates);*/

            // TEST 4
//            factorStates = new HashMap<>();
//            factorStates.put("codequality", "Medium");
//            factorStates.put("softwarestability", "Very_Low");
//
//            assessmentTest(dneFiles + "WSA_productquality_nokia.dne", "productquality", factorStates);

            // TEST WITH NUMERIC VALUES
            File file = new File("---");
            String[] elements = {"complex", ""};
                    //{"developmenttaskcompletion", "specificationtaskcompletion", "posponedissuesratio",
                    //"buildstability", "criticalissuesratio", "testsuccess"};
            double[] elementValues = {0.96f, 1f, 0.58f, 1f, 0.79f, 1f};
            String si = "productreadiness";
            double[][] intervals_per_element = {{0.45f, 0.7f, 0.9f, 0.95f},
                    {0.2f, 0.7f, 0.9f, 0.99f},
                    {0.45f, 0.8f},
                    {0.4f, 0.7f, 0.8f, 0.95f},
                    {0.4f, 0.7f, 0.8f, 0.98f},
                    {0.4f, 0.7f, 0.8f, 0.98f}};

            DTOSIAssessment assessmentTest = SIAssessment.AssessSI(si, elements, elementValues, intervals_per_element, file);
            System.out.print("Probabilites after entering values for node " + si + ": ");
            System.out.println(elementValues.toString());
            System.out.println(assessmentTest.toString());
            System.out.println("Most probable state: " + (assessmentTest.mostProbableCategory()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void assessmentTest(String pathName, String SInodeName, Map<String, String> factorStates) {
        try {
            File file = new File(pathName);

            DTOSIAssessment assessmentTest = SIAssessment.AssessSI(SInodeName, factorStates, file);

            System.out.print("Probabilites after entering beliefs for node " + SInodeName + ": ");
            System.out.println(factorStates.toString());
            System.out.println(assessmentTest.toString());
    } catch (Exception e) {
        e.printStackTrace();
    }

}


}
