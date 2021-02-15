package Assessment;

import java.util.ArrayList;
import smile.math.distance.JensenShannonDistance;

public class DTOSIAssessment {
    private ArrayList<DTOSICategory> probsSICategories;

    DTOSIAssessment(ArrayList<DTOSICategory> probsSICategories) {
        this.probsSICategories = probsSICategories;
    }

    public DTOSIAssessment() {
        this.probsSICategories = new ArrayList<>();
    }

    public ArrayList<DTOSICategory> getProbsSICategories() {
        return probsSICategories;
    }

    public double computeDistanceTo(DTOSIAssessment compareTo) {
        JensenShannonDistance jsd = new JensenShannonDistance();
        return jsd.d(this.toDouble(), compareTo.toDouble());
    }

    public Boolean isEqualAssessment(DTOSIAssessment compareTo) {
        return this.mostProbableCategory().toLowerCase().equals(compareTo.mostProbableCategory().toLowerCase());
    }

    private double[] toDouble() {
        double[] ret = new double[this.probsSICategories.size()];
        for (int i = 0; i < this.probsSICategories.size(); i++) {
            ret[i] = this.probsSICategories.get(i).getProbSICategory();
        }
        return ret;
    }

    public String[] buildDayProbabilities() {
        String[] ret = new String[this.getProbsSICategories().size() + 1];
        for (int i = 0; i < this.getProbsSICategories().size(); i++) {
            ret[i] = String.valueOf(this.getProbsSICategories().get(i).getProbSICategory());
        }
        ret[ret.length - 1] = this.mostProbableCategory();
        return ret;
    }

    public String mostProbableCategory() {
        float highestProbability = 0;
        String highestProbabilityCategory = "";

        for (DTOSICategory categoryAndProbablity : this.probsSICategories) {
            if (categoryAndProbablity.getProbSICategory() > highestProbability) {
                highestProbabilityCategory = categoryAndProbablity.getIdSICategory();
                highestProbability = categoryAndProbablity.getProbSICategory();
            }
        }
        return highestProbabilityCategory;
    }

    @Override
    public String toString() {
        return probsSICategories.toString();
    }
}
