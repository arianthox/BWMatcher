package com.globant.brainwaves.service;

import com.globant.brainwaves.hhm.mediator.Operations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeBookService {
    @Autowired
    private Operations hmmOperations;

    public boolean generateCodeBook() throws Exception {
            hmmOperations.generateCodebook();
        return false;
    }
}
