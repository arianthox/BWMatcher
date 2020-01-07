package com.arianthox.predictor.service;


import com.arianthox.predictor.hmm.EngineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatcherService {

    private final EngineManager engineManager;

    @Autowired
    public MatcherService(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    public boolean addKey(String key, List<short[]> allSamples) throws Exception {
        engineManager.trainKey( key, allSamples);
        return false;
    }
}
