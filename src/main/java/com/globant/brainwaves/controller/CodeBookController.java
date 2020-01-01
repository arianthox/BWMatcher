package com.globant.brainwaves.controller;

import com.globant.brainwaves.service.CodeBookService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/codebook")
public class CodeBookController {

    @Autowired
    private CodeBookService codeBookService;

    @RequestMapping(value="/generate", method = RequestMethod.POST, produces="application/json")
    @ApiOperation(value = "Generate codebook")
    public ResponseEntity generateCodeBook() throws Exception {
        codeBookService.generateCodeBook();
        return new ResponseEntity(HttpStatus.OK);
    }
}
