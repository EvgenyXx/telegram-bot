package com.example.parser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParserController {

    private final ResultService resultService;

    public ParserController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/parse")
    public Result parse(
            @RequestParam String url,
            @RequestParam String player
    ) throws Exception {

        return resultService.calculate(url, player);
    }
}