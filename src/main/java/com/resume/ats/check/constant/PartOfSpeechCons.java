package com.resume.ats.check.constant;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

public class PartOfSpeechCons {
    public static final List<String> USEFUL_CHUNK_KEYS = Arrays.asList("B-NP","I-ADJP", "I-NP", "B-VP", "B-ADJP", "I-VP");
    public static final List<String> USEFUL_POS_KEYS = Arrays.asList("JJ","NN","NNP","NNPS","NNS","RB","RBS","VB","VBG","VBN","VBD","VBZ");
    public static final List<String> NUMS_POS_KEYS = List.of("CD");

}
