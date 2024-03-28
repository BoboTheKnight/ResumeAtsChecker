package com.resume.ats.check.service;

import com.resume.ats.check.component.ExtractorEntity;
import com.resume.ats.check.models.AnalysisResult;
import com.resume.ats.check.models.Dimension;
import com.resume.ats.check.models.ExtractorDetail;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.resume.ats.check.constant.PartOfSpeechCons.*;

@Service
public class DiscoverServiceImpl implements DiscoverService {

    private static final Logger logger = LogManager.getLogger(DiscoverServiceImpl.class);

    private final AnalysisResultService analysisResultService;

    @Autowired
    public DiscoverServiceImpl(AnalysisResultService analysisResultService) {
        this.analysisResultService = analysisResultService;
    }

    @Override
    public Map<String, Map<String, String>> discoverByInput(String inputText){
        AnalysisResult analysisResult =  analysisResultService.analysis(inputText);
        return dealByInputOrder(analysisResult);
    }

    private Map<String, Map<String, String>> dealByInputOrder(AnalysisResult analysisResult) {
        String[] tokens = analysisResult.getTokens();
        String[] posTags = analysisResult.getPosTags();
        String[] chunkTags = analysisResult.getChunkTags();       ;
        int n = tokens.length;
        Map<String, Map<String, String>> map = new LinkedHashMap<>(n);
        for (int i = 0; i < n; i++) {
            String token = tokens[i];
            String posTag = posTags[i];
            String chunkTag = chunkTags[i];
            if(USEFUL_CHUNK_KEYS.contains(chunkTag) && USEFUL_POS_KEYS.contains(posTag)){
                Map<String, String> chunkPosMap = map.getOrDefault(token, new LinkedHashMap<>());
                chunkPosMap.putIfAbsent(chunkTag, posTag);
                map.put(token, chunkPosMap);
            }
        }
        return map;
    }

    @Override
    public Map<String, Map<String, String>> discoverByTags(String inputText) {
        AnalysisResult analysisResult =  analysisResultService.analysis(inputText);
        return learnFromTags(analysisResult);
    }

    Map<String, Map<String, String>> learnFromTags(AnalysisResult analysisResult) {
        String[] tokens = analysisResult.getTokens();
        String[] posTags = analysisResult.getPosTags();
        String[] chunkTags = analysisResult.getChunkTags();       ;

        List<ExtractorDetail> extractorDetails = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String posTag = posTags[i];
            String chunk = chunkTags[i];
            extractorDetails.add(new ExtractorDetail(token, posTag, chunk));
        }

        return extractorDetails.stream()
            .filter(detail ->
                USEFUL_CHUNK_KEYS.contains(detail.getChunkTag())
                    && USEFUL_POS_KEYS.contains(detail.getPosTag()))
            .collect(Collectors.groupingBy(ExtractorDetail::getChunkTag,
                Collectors.groupingBy(ExtractorDetail::getPosTag,
                    Collectors.collectingAndThen(
                        Collectors.mapping(ExtractorDetail::getToken, Collectors.toSet()),
                        tokenSet -> tokenSet.stream().sorted().collect(Collectors.joining(","))
                    )
                )
            ));
    }


    @Override
    public List<String> discoverIncludingPhrases(String text) {
        AnalysisResult analysisResult =  analysisResultService.analysis(text);
        return identifyPhrases(analysisResult);
    }

    private List<String> identifyPhrases(AnalysisResult analysisResult) {
        String[] tokens = analysisResult.getTokens();
        String[] posTags = analysisResult.getPosTags();
        String[] chunkTags = analysisResult.getChunkTags();       ;
        int n = tokens.length;
        List<String> phrases = new ArrayList<>();
        StringBuilder sb;
        for (int i = 0; i < n; i++) {
            if (USEFUL_CHUNK_KEYS.contains(chunkTags[i]) && USEFUL_POS_KEYS.contains(posTags[i])) {
                i = getNounPhrase(tokens, posTags, chunkTags, n, phrases, i);
                i = getDescription(tokens, posTags, chunkTags, n, phrases, i);
            }
        }
        return phrases;
    }

    //TODO Too much noise to deal with
    private static int getDescription(String[] tokens, String[] posTags, String[] chunkTags, int n, List<String> phrases, int i) {
        StringBuilder sb = new StringBuilder();
        while (i < n
            && (chunkTags[i].equals("I-NP") || chunkTags[i].equals("B-NP"))
            && (posTags[i].equals("RB")||posTags[i].equals("JJ"))
        ) {
            sb.append(tokens[i]).append(" ");
            i++;
        }
        if(!sb.isEmpty()){
            phrases.add(sb.substring(0,sb.length()-1).toString());
        }
        return i;
    }

    private static int getNounPhrase(String[] tokens, String[] posTags, String[] chunkTags, int n, List<String> phrases, int i) {
        StringBuilder sb = new StringBuilder();
        while (i < n && (chunkTags[i].equals("I-NP") || chunkTags[i].equals("B-NP")) && posTags[i].equals("NNP")) {
            sb.append(tokens[i]).append(" ");
            i++;
        }
        if(!sb.isEmpty()){
            phrases.add(sb.toString());
        }
        return i;
    }

    @Override
    public Dimension discoverByDimension(String text) {
//        Map<String, Map<String, String>> wordChunkPosMap = discoverIncludingPhrases(text);
//        List<String> hardSkills = analyticalTechWords(wordChunkPosMap);
//        List<String> softSkills = analyticalSoftSkills(wordChunkPosMap, ExtractorEntity.getSoftSkills());
//        List<String> experience = new ArrayList<>();
//        List<String> characterTags = new ArrayList<>();
//        return new Dimension(hardSkills, softSkills, experience, characterTags);
    return null;}

    //TODO softSkills including phrases, but wordChunkMap doesn't including. need to add phrases.
    private List<String> analyticalSoftSkills(Map<String, Map<String, String>> wordChunkPosMap, Set<String> softSkillDic) {
        List<String> softSkills = new ArrayList<>();
        for (Entry<String, Map<String, String>> entry : wordChunkPosMap.entrySet()) {
            String word = entry.getKey();
            if(softSkillDic.contains(word.toLowerCase())){
                softSkills.add(word);
            }
        }
        return softSkills;
    }

    private List<String> analyticalTechWords(Map<String, Map<String, String>> wordChunkPosMap) {
        List<String> hardSkills = new ArrayList<>();
        for (Entry<String, Map<String, String>> entry : wordChunkPosMap.entrySet()) {
            String word = entry.getKey();
            if(Character.isUpperCase(word.charAt(0))){
                hardSkills.add(word);
            }
        }
        return hardSkills;
    }
}
