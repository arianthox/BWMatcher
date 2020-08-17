package com.arianthox.predictor.service;


import com.arianthox.predictor.hmm.EngineManager;
import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

@Service
@Log
public class MatcherService {

    private final EngineManager engineManager;

    private static final Gson gson=new Gson();



    @Autowired
    public MatcherService(EngineManager engineManager) {
        this.engineManager = engineManager;

    }
    public boolean addKey(String key, List<int[]> allSamples) throws Exception {
        log.info("Adding Key:"+key);
        engineManager.addKey( key, allSamples);
        return true;
    }

    public HashMap<String, Double> matchKey(int[] signal) throws Exception {
        log.log(Level.FINE,"Matching: {0}",signal);
        return engineManager.matchKey(signal);
    }


}
