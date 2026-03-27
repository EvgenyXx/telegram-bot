package com.example.parser.API;

import com.example.parser.model.Result;
import com.example.parser.service.ResultService;
import com.example.parser.dto.ResultDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/parse/all")
    public List<ResultDto> parseAll(@RequestParam String url) throws Exception {
        return resultService.calculateAll(url);
    }
}