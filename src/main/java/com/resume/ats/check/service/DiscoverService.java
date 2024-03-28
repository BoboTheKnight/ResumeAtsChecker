package com.resume.ats.check.service;

import com.resume.ats.check.models.Dimension;
import java.util.List;
import java.util.Map;

public interface DiscoverService {

  Map<String, Map<String, String>> discoverByInput(String inputText);

  Map<String, Map<String, String>> discoverByTags(String inputText);

  Dimension discoverByDimension(String text);

  List<String> discoverIncludingPhrases(String text);
}
