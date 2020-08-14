package com.arianthox.predictor.controller;

import com.arianthox.predictor.service.MatcherService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/matcher")
public class MatcherController {

    private final transient MatcherService matcherService;

    @Autowired
    public MatcherController(MatcherService matcherService) {
        this.matcherService = matcherService;
    }

    @RequestMapping(value = "/add/{key}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add Key")
    public ResponseEntity addKey(@RequestBody List<int[]> samples, @PathVariable String key) throws Exception {
        matcherService.addKey(key, samples);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/match", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Match Key")
    public HashMap<String, Double> matchKey(@RequestBody int[] signal) throws Exception {
        return matcherService.matchKey(signal);
    }


}
