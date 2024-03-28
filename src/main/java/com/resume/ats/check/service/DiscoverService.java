package com.resume.ats.check.service;

import com.resume.ats.check.component.ExtractorEntity;
import com.resume.ats.check.models.ExtractorDetail;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.resume.ats.check.constant.PartOfSpeechCons.*;

@Service
public class DiscoverService {

    private static final Logger logger = LogManager.getLogger(DiscoverService.class);

    public Map<String, Map<String, String>> discoverByInput(String inputText){
        String[] tokens = tokenizeInputText(inputText, ExtractorEntity.getTokenizer());
        String[] posTags = tagPartsOfSpeech(tokens, ExtractorEntity.getPosTagger());
        String[] chunkTags = chunkerAnalysis(tokens, posTags, ExtractorEntity.getChunker());
        return dealByInputOrder(tokens, posTags, chunkTags, ExtractorEntity.getStopWords());
    }

    private Map<String, Map<String, String>> dealByInputOrder(String[] tokens, String[] posTags, String[] chunkTags, List<String> stopWords) {
        int n = tokens.length;
        Map<String, Map<String, String>> map = new LinkedHashMap<>(n);
        for (int i = 0; i < n; i++) {
            String token = tokens[i];
            if(stopWords.contains(token)){
                continue;
            }
            String posTag = posTags[i];
            String chunkTag = chunkTags[i];
            if(USEFUL_CHUNK_KEYS.contains(chunkTag) && USEFUL_POS_KEYS.contains(posTag)){
                Map<String, String> chunkPosMap = map.getOrDefault(token, new LinkedHashMap<>());
                if(chunkPosMap.containsKey(chunkTag)){
                    logger.warn("Token ({}) chunkTag ({}) has conflict posTag: {}, new posTag is:{}", token, chunkTag, chunkPosMap.get(chunkTag), posTag);
                }
                chunkPosMap.put(chunkTag, pobsTag);
                map.put(token, chunkPosMap);
            }
        }
        return map;
    }

    public Map<String, Map<String, String>> discoverByTags(String inputText) {
        String[] tokens = tokenizeInputText(inputText, ExtractorEntity.getTokenizer());
        String[] posTags = tagPartsOfSpeech(tokens, ExtractorEntity.getPosTagger());
        String[] chunkTags = chunkerAnalysis(tokens, posTags, ExtractorEntity.getChunker());
        return learnFromTags(tokens, posTags, chunkTags, ExtractorEntity.getStopWords());
    }

    Map<String, Map<String, String>> learnFromTags(String[] tokens, String[] posTags, String[] chunkTags, List<String> stopWords) {
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
                    && USEFUL_POS_KEYS.contains(detail.getPosTag())
                    && !stopWords.contains(detail.getToken()))
            .collect(Collectors.groupingBy(ExtractorDetail::getChunkTag,
                Collectors.groupingBy(ExtractorDetail::getPosTag,
                    Collectors.collectingAndThen(
                        Collectors.mapping(ExtractorDetail::getToken, Collectors.toSet()),
                        tokenSet -> tokenSet.stream().sorted().collect(Collectors.joining(","))
                    )
                )
            ));
    }

    private String[] tokenizeInputText(String inputText, Tokenizer tokenizer) {
        return tokenizer.tokenize(inputText);
    }

    private String[] tagPartsOfSpeech(String[] tokens, POSTaggerME posTagger) {
        return posTagger.tag(tokens);
    }

    private String[] chunkerAnalysis(String[] tokens, String[] posTags, ChunkerME chunker) {
        return chunker.chunk(tokens, posTags);
    }
}
