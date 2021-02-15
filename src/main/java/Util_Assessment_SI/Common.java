package Util_Assessment_SI;

import Assessment.DTOSIAssessment;
import Assessment.DTOSICategory;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

import java.util.Map;

public class Common {
    //UNBBAYES METHODS
    public static DTOSIAssessment doInferenceUnbbayes(ProbabilisticNetwork net, Node SInode, Map<String, String> elementStates) throws Exception {
        DTOSIAssessment ret = new DTOSIAssessment();

        IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm();
        algorithm.setNetwork(net);
        algorithm.run();

        for (String element : elementStates.keySet()) {
            ProbabilisticNode pnElementNode = (ProbabilisticNode) net.getNode(element);
            int stateIndex = getStateIndex(pnElementNode, elementStates.get(element));
            pnElementNode.initMarginalList();
            pnElementNode.addFinding(stateIndex);
        }

        net.updateEvidences();

        for (int i = 0; i < SInode.getStatesSize(); i++) {
            String stateName = SInode.getStateAt(i);
            Float stateProb = ((ProbabilisticNode) SInode).getMarginalAt(i);

            DTOSICategory SIcategory = new DTOSICategory(stateName, stateProb);
            ret.getProbsSICategories().add(SIcategory);
        }
        return ret;
    }

    private static int getStateIndex(Node node, String state) {
            for(int i = 0; i < node.getStatesSize(); ++i) {
                if (node.getStateAt(i).equals(state)) {
                    return i;
                }
            }
            return -1;
    }

    private static float[] setLikelihoodArray(ProbabilisticNode factorNode, int stateIndex) {
        float[] ret = new float[factorNode.getStatesSize()];

        for(int i = 0; i < factorNode.getStatesSize(); ++i) {
             ret[i] = (float)(i == stateIndex ? 1 : 0);
        }
        return ret;
    }
}
