
package com.globant.brainwaves.hhm.classify.speech;

import java.io.Serializable;

import com.globant.brainwaves.hhm.db.Model;
import com.globant.brainwaves.hhm.classify.speech.vq.Centroid;

/**
 * @author
 */
public class CodeBookDictionary implements Serializable, Model {

	private static final long	serialVersionUID	= 2094442679375932181L;
	protected int				dimension;
	protected Centroid[]		cent;

	public CodeBookDictionary( ) {
	}

	public int getDimension( ) {
		return dimension;
	}

	public void setDimension( int dimension ) {
		this.dimension = dimension;
	}

	public Centroid[] getCent( ) {
		return cent;
	}

	public void setCent( Centroid[] cent ) {
		this.cent = cent;
	}
}
