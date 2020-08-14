package com.arianthox.predictor.hmm.engine;

import com.arianthox.predictor.hmm.model.CodeBookModel;
import com.arianthox.predictor.hmm.persistence.ObjectIO;
import com.arianthox.predictor.hmm.persistence.PatternFileIO;
import com.arianthox.predictor.hmm.vq.CentroId;
import com.arianthox.predictor.hmm.vq.Points;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

@Log
@Data
public class CodeBook {
    /**
     * split factor (should be in the range of 0.01 <= SPLIT <= 0.05)
     */
    protected final double SPLIT = 0.01;
    /**
     * minimum distortion
     */
    protected final double MIN_DISTORTION = 0.1;
    /**
     * Codebook size - number of codewords (codevectors)<br>
     * default is: 256
     */
    protected int codebook_size = 256;
    /**
     * centroids array
     */
    protected CentroId centroids[];
    /**
     * training points
     */
    protected Points pt[];
    /**
     * dimension /////no of features
     */
    protected int dimension;

    @Autowired
    private PatternFileIO patternFileIO;


    public CodeBook(Path path) {
        try {
            ObjectIO<CodeBookModel> io = new ObjectIO<>();
            CodeBookModel model = io.readModel(path);
            dimension = model.getDimension();
            centroids = model.getCent();
            pt = model.getPoints();
            codebook_size = model.getSize();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * constructor to train a Codebook with given training points and default
     * Codebook size (256)<br>
     * calls: none<br>
     * called by: trainCodebook
     *
     * @param tmpPt training vectors
     */
    public CodeBook(Points tmpPt[]) {
        pt = tmpPt;
        if (pt.length < codebook_size) {
            pt = rebuild(tmpPt);
        }
        dimension = pt[0].getDimension();
        initialize();
    }

    private Points[] rebuild(Points tmpPt[]) {
        Points newPt[] = new Points[codebook_size];
        for (int i = 0; i < codebook_size; i++) {
            newPt[i] = tmpPt[i % tmpPt.length];
        }
        return newPt;
    }


    private void showParameters() {
        for (int c = 0; c < centroids.length; c++) {
            // bw.write("c" + c + ": (");
            for (int k = 0; k < dimension; k++) {
                System.out.print(centroids[c].getCo(k) + "\t");
            }
            System.out.println();
        }
    }

    /**
     * creates a Codebook using LBG algorithm which includes K-means<br>
     * calls: Centroid<br>
     * called by: Codebook
     */
    protected void initialize() {
        double distortion_before_update = 0; // distortion measure before
        // updating centroids
        double distortion_after_update = 0; // distortion measure after update
        // centroids

        // design a 1-vector Codebook
        centroids = new CentroId[1];

        // then initialize it with (0, 0) coordinates
        double origin[] = new double[dimension];
        centroids[0] = new CentroId(origin);

        // initially, all training points will belong to 1 single cell
        for (int i = 0; i < pt.length; i++) {
            centroids[0].add(pt[i], 0);
        }

        // calls update to set the initial codevector as the average of all
        // points
        centroids[0].update();

        // Iteration 1: repeat splitting step and K-means until required number
        // of codewords is reached
        while (centroids.length < codebook_size) {
            // split codevectors by a binary splitting method
            split();

            // group training points to centroids closest to them
            groupPtoC();

            // Iteration 2: perform K-means algorithm
            do {
                for (int i = 0; i < centroids.length; i++) {
                    distortion_before_update += centroids[i].getDistortion();
                    centroids[i].update();
                }

                // regroup
                groupPtoC();

                for (int i = 0; i < centroids.length; i++) {
                    distortion_after_update += centroids[i].getDistortion();
                }

            } while (Math.abs(distortion_after_update - distortion_before_update) < MIN_DISTORTION);
        }
    }

    /**
     * save Codebook to cbk object file<br>
     * calls: none<br>
     * called by: train
     */
    public void save(Path path) throws Exception {

        log.log(Level.FINE,"---------SAVE into CodeBook---------------------------");
        CodeBookModel cbd = new CodeBookModel();
        cbd.setPoints(pt);
        for (int i = 0; i < centroids.length; i++) {
            centroids[i].pts.removeAllElements();
        }
        cbd.setDimension(dimension);
        cbd.setCent(centroids);
        cbd.setSize(codebook_size);
        patternFileIO.setModel(cbd);
        patternFileIO.saveModel(path);

    }

    public void saveToFileUVQ(String filepath, int[][] samples) {
        try {
            FileWriter fw = new FileWriter(filepath);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int row[] : samples) {
                for (int col : row) {
                    bw.write(col + " ");
                }
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void saveUVQ(Path filepath, int[][] samples) throws Exception {
            StringBuffer sb = new StringBuffer();
            for (int row[] : samples) {
                for (int col : row) {
                    sb.append(col + " ");
                }
                sb.append("\n");
            }
        patternFileIO.saveUVQ(filepath, sb.toString());
    }
    /**
     * splitting algorithm to increase number of centroids by multiple of 2<br>
     * calls: Centroid<br>
     * called by: Codebook
     */
    protected void split() {
        log.log(Level.FINE,"Centroids length now becomes " + centroids.length + 2);
        CentroId temp[] = new CentroId[centroids.length * 2];
        double tCo[][];
        for (int i = 0; i < temp.length; i += 2) {
            tCo = new double[2][dimension];
            for (int j = 0; j < dimension; j++) {
                tCo[0][j] = centroids[i / 2].getCo(j) * (1 + SPLIT);
            }
            temp[i] = new CentroId(tCo[0]);
            for (int j = 0; j < dimension; j++) {
                tCo[1][j] = centroids[i / 2].getCo(j) * (1 - SPLIT);
            }
            temp[i + 1] = new CentroId(tCo[1]);
        }

        // replace old centroids array with new one
        centroids = new CentroId[temp.length];
        centroids = temp;
    }

    /**
     * quantize the input array of points in k-dimensional space<br>
     * calls: none<br>
     * called by: volume
     *
     * @param pts points to be quantized
     * @return quantized index array
     */
    public int[] quantize(Points pts[]) {
        int output[] = new int[pts.length];
        for (int i = 0; i < pts.length; i++) {
            output[i] = closestCentroidToPoint(pts[i]);
        }
        return output;
    }

    /**
     * quantize the input array of points in k-dimensional space<br>
     * calls: none<br>
     * called by: volume
     *
     * @return quantized index array
     */
    public int[] quantize() {
        int output[] = new int[pt.length];
        for (int i = 0; i < pt.length; i++) {
            output[i] = closestCentroidToPoint(pt[i]);
        }
        return output;
    }


    /**
     * calculates the distortion<br>
     * calls: none<br>
     * called by: volume
     *
     * @param pts points to calculate the distortion with
     * @return distortion measure
     */
    public double getDistortion(Points pts[]) {
        double dist = 0;
        for (int i = 0; i < pts.length; i++) {
            int index = closestCentroidToPoint(pts[i]);
            double d = getDistance(pts[i], centroids[index]);
            dist += d;
        }
        return dist;
    }

    /**
     * finds the closest Centroid to a specific Points<br>
     * calls: none<br>
     * called by: Codebook
     *
     * @param pt Points
     * @return index number of the closest Centroid
     */
    private int closestCentroidToPoint(Points pt) {
        double tmp_dist = 0;
        double lowest_dist = 0; // = getDistance(pt, centroids[0]);
        int lowest_index = 0;

        for (int i = 0; i < centroids.length; i++) {
            tmp_dist = getDistance(pt, centroids[i]);
            if (tmp_dist < lowest_dist || i == 0) {
                lowest_dist = tmp_dist;
                lowest_index = i;
            }
        }
        return lowest_index;
    }

    /**
     * finds the closest Centroid to a specific Centroid<br>
     * calls: none<br>
     * called by: Codebook
     *
     * @param c Points
     * @return index number of the closest Centroid
     */
    private int closestCentroidToCentroid(CentroId c) {
        double tmp_dist = 0;
        double lowest_dist = Double.MAX_VALUE;
        int lowest_index = 0;
        for (int i = 0; i < centroids.length; i++) {
            tmp_dist = getDistance(c, centroids[i]);
            if (tmp_dist < lowest_dist && centroids[i].getNumPts() > 1) {
                lowest_dist = tmp_dist;
                lowest_index = i;
            }
        }
        return lowest_index;
    }

    /**
     * finds the closest Points in c2's cell to c1<br>
     * calls: none<br>
     * called by: Codebook
     *
     * @param c1 first Centroid
     * @param c2 second Centroid
     * @return index of Points
     */
    private int closestPoint(CentroId c1, CentroId c2) {
        double tmp_dist = 0;
        double lowest_dist = getDistance(c2.getPoint(0), c1);
        int lowest_index = 0;
        for (int i = 1; i < c2.getNumPts(); i++) {
            tmp_dist = getDistance(c2.getPoint(i), c1);
            if (tmp_dist < lowest_dist) {
                lowest_dist = tmp_dist;
                lowest_index = i;
            }
        }
        return lowest_index;
    }

    /**
     * grouping points to cells<br>
     * calls: none<br>
     * called by: Codebook
     */
    private void groupPtoC() {
        // find closest Centroid and assign Points to it
        for (int i = 0; i < pt.length; i++) {
            int index = closestCentroidToPoint(pt[i]);
            centroids[index].add(pt[i], getDistance(pt[i], centroids[index]));
        }
        // make sure that all centroids have at least one Points assigned to it
        // no cell should be empty or else NaN error will occur due to division
        // of 0 by 0
        for (int i = 0; i < centroids.length; i++) {
            if (centroids[i].getNumPts() == 0) {
                // find the closest Centroid with more than one points assigned
                // to it
                int index = closestCentroidToCentroid(centroids[i]);
                // find the closest Points in the closest Centroid's cell
                int closestIndex = closestPoint(centroids[i], centroids[index]);
                Points closestPt = centroids[index].getPoint(closestIndex);
                centroids[index].remove(closestPt, getDistance(closestPt, centroids[index]));
                centroids[i].add(closestPt, getDistance(closestPt, centroids[i]));
            }
        }
    }

    /**
     * calculates the distance of a Points to a Centroid<br>
     * calls: none<br>
     * called by: Codebook
     *
     * @param tPt points
     * @param tC  Centroid
     */
    private double getDistance(Points tPt, CentroId tC) {
        double distance = 0;
        double temp = 0;
        for (int i = 0; i < dimension; i++) {
            temp = tPt.getCo(i) - tC.getCo(i);
            distance += temp * temp;
        }
        distance = Math.sqrt(distance);
        return distance;
    }

    public HiddenMarkov generateHiddenMarkov() {
        HiddenMarkov mkv = new HiddenMarkov(6, 256);
        int[][] quantized = new int[1][];
        quantized[0] = quantize();
        mkv.setTrainSeq(quantized);
        mkv.train();
        return mkv;
    }

    public void setPatternFileIO(PatternFileIO patternFileIO) {
        this.patternFileIO = patternFileIO;
    }
}
