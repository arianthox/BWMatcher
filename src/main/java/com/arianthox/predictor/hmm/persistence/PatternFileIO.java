package com.arianthox.predictor.hmm.persistence;


import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

@Log
@Service
public class PatternFileIO <T extends Model> extends ObjectIO{



    private String EXTENSION = ".";




    public void saveUVQ(Path filePath, String content) throws Exception {
        log.log(Level.FINE,"Saving model: {0}",filePath);
        Gson gson = new Gson();
        String content_gson = gson.toJson(content);
        Files.write(filePath,content_gson.getBytes());
    }




}
