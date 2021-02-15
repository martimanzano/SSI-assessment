package Assessment;

import Util_Assessment_SI.BayesUtils;
import Util_Assessment_SI.Common;
import unbbayes.io.BaseIO;
import unbbayes.io.DneIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SIAssessment {

    /**
     *
     * @param IDSI: ID of the Strategic Indicator to be assessed. Has to coincide with its corresponding node's name in
     *           the BN file to use (BNFile). Example: productquality
     * @param elementStates: Map containing the states of the SI's parent nodes. Being the keys the IDs of the parent
     *                    nodes and the values being the state itself. IDs and states need to be consistent with the
     *                    BN file in use (BNFile). Example: "CodeQuality" : "VeryLow"
     *                                                      "Stability" : "Medium"
     *                                                      "TestingStatus" : "High"
     * @param BNFile: File containing the BN to use for the assessment, in DNE format
     * @return Assessment of the SI: ArrayList of DTOSICategory, each one containing the ID of the SI category
     * (idSICategory) and its associated assessed probability (probSICategory).
     * @throws Exception
     */
    public static DTOSIAssessment AssessSI(String IDSI, Map<String, String> elementStates, File BNFile) throws Exception {
        BaseIO io = new DneIO();
        ProbabilisticNetwork net = (ProbabilisticNetwork) io.load(BNFile);
        Node SInode = net.getNode(IDSI);

        return Common.doInferenceUnbbayes(net, SInode, elementStates);
    }

    public static DTOSIAssessment AssessSI(String IDSI, String[] elements, double[] values,
                                           double[][] intervals_per_element, File BNFile) throws Exception {
        Map<String, String> elementStatesMap = new HashMap<>();
        BaseIO io = new DneIO();
        ProbabilisticNetwork net = (ProbabilisticNetwork) io.load(BNFile);
        IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm();
        algorithm.setNetwork(net);
        algorithm.run();

        for (int i = 0; i < elements.length; i++) {
            ProbabilisticNode pnElementNode = (ProbabilisticNode) net.getNode(elements[i]);
            String[] elementStates = getElementStates(pnElementNode);
            String discretizedState = BayesUtils.discretize(values[i], intervals_per_element[i], elementStates);
            elementStatesMap.put(elements[i], discretizedState);
        }
        return AssessSI(IDSI, elementStatesMap, BNFile);
    }

    public static String[] getElementStates(ProbabilisticNode elementNode) {
        String[] ret = new String[elementNode.getStatesSize()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = elementNode.getStateAt(i);
        }
        return ret;
    }
}
