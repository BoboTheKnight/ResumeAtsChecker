package com.resume.ats.check.controller;

import com.resume.ats.check.models.Dimension;
import com.resume.ats.check.service.DiscoverService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("discover")
public class DiscoverController {
    private final DiscoverService discoverService;

    @Autowired
    public DiscoverController(DiscoverService discoverService) {
        this.discoverService = discoverService;
    }

    @PostMapping("tags")
    public Map<String, Map<String, String>> discoverByTags(@RequestParam String text) {
        return discoverService.discoverByTags(text);
    }

    @PostMapping("input")
    public Map<String, Map<String, String>> discoverByInput(@RequestParam String text) {
        return discoverService.discoverByInput(text);
    }

    @PostMapping("dimension")
    public Dimension discoverByDimension(@RequestParam String text) {
        return discoverService.discoverByDimension(text);
    }

    @PostMapping("phrase")
    public List<String> discoverPhrases(@RequestParam String text) {
        return discoverService.discoverIncludingPhrases(text);
    }

}
