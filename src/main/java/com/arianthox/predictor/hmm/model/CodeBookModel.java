
package com.arianthox.predictor.hmm.model;

import com.arianthox.predictor.hmm.persistence.Model;
import com.arianthox.predictor.hmm.vq.CentroId;
import com.arianthox.predictor.hmm.vq.Points;

import java.io.Serializable;


public class CodeBookModel implements Serializable, Model {

    private static final long serialVersionUID = 2354442679375932181L;


    protected CentroId[] cent;
    protected Points[] points;
    protected int dimension;
    protected int size;

    public CodeBookModel() {
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public CentroId[] getCent() {
        return cent;
    }

    public void setCent(CentroId[] cent) {
        this.cent = cent;
    }

    public Points[] getPoints() {
        return points;
    }

    public void setPoints(Points[] points) {
        this.points = points;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
