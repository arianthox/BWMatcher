
package com.arianthox.predictor.hmm.persistence;

import lombok.extern.java.Log;

import java.io.*;
import java.nio.file.Path;

/**
 * This Class works for both any object <code><T></code>, which implements the Model interface
 *
 * @param <T>
 * @author
 */
@Log
public class ObjectIO<T extends Model> {

    private ObjectInputStream input;
    private ObjectOutputStream output;
    T model;

    /**
     * default constructor of modelDB
     */
    public ObjectIO() {
    }

    /**
     * sets the model to save to db
     *
     * @param model model of current type to save into db
     */
    public void setModel(T model) {
        this.model = model;
    }

    /**
     * saves the model to {@code filePath} of type T
     *
     * @param filePath
     */
    public void saveModel(Path filePath) throws Exception {

        File file = filePath.toFile();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        // open file
        output = new ObjectOutputStream(new FileOutputStream(file));
        // save model
        log.info("into saveModel ObjectIO");
        output.writeObject(model);
        output.close();
    }

    /**
     * read the model from {@code filePath} of type T
     *
     * @param filePath
     * @return the model of type T
     */
    public T readModel(Path filePath) throws Exception {
        // open file
        input = new ObjectInputStream(new FileInputStream(filePath.toFile()));
        // read
        model = (T) input.readObject();
        input.close();
        return model;
    }
}
