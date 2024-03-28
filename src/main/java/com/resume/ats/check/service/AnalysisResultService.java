package com.resume.ats.check.service;

import com.resume.ats.check.component.ExtractorEntity;
import com.resume.ats.check.models.AnalysisResult;
import java.util.List;
import java.util.stream.Stream;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import org.springframework.stereotype.Service;

@Service
public class AnalysisResultService {

  public AnalysisResult analysis(String inputText) {
    String[] tokens = tokenizeAnalysis(inputText, ExtractorEntity.getTokenizer());
    String[] posTags = partsOfSpeechAnalysis(tokens, ExtractorEntity.getPosTagger());
    String[] chunkTags = chunkerAnalysis(tokens, posTags, ExtractorEntity.getChunker());
    return new AnalysisResult(tokens, posTags, chunkTags);
  }

  private String[] tokenizeAnalysis(String inputText, Tokenizer tokenizer) {
    List<String> stopWords = ExtractorEntity.getStopWords();
    String[] tokens = tokenizer.tokenize(inputText);
    List<String> filteredTokens = Stream.of(tokens).filter(token -> !stopWords.contains(token)).distinct().toList();
    return filteredTokens.toArray(new String[0]);
  }

  private String[] partsOfSpeechAnalysis(String[] tokens, POSTaggerME posTagger) {
    return posTagger.tag(tokens);
  }

  private String[] chunkerAnalysis(String[] tokens, String[] posTags, ChunkerME chunker) {
    return chunker.chunk(tokens, posTags);
  }
}
