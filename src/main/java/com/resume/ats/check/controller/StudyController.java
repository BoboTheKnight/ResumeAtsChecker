package com.resume.ats.check.controller;

import com.resume.ats.check.service.DiscoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StudyController {
    private final DiscoverService discoverService;

    @Autowired
    public StudyController(DiscoverService discoverService) {
        this.discoverService = discoverService;
    }

    @PostMapping("tags")
    public Map<String, Map<String, String>> discoverByTags(String text) {
        return discoverService.discoverByTags(text);
    }

    @PostMapping("discover/input")
    public Map<String, Map<String, String>> discoverByInput(String text) {
        return discoverService.discoverByInput(text);
    }

}
