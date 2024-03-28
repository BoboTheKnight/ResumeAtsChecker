package com.resume.ats.check.service;

import com.resume.ats.check.component.ExtractorEntity;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class KeywordExtractorService {
	private static final Logger logger = LogManager.getLogger(KeywordExtractorService.class);
	public Map<String, Integer> extractKeywords(String inputText) {

		// Tokenize input text
		String[] tokens = tokenizeInputText(inputText, ExtractorEntity.getTokenizer());

		// Tag parts of speech in input text
		String[] posTags = tagPartsOfSpeech(tokens, ExtractorEntity.getPosTagger());

		String[] chunkTags = chunkerAnalysis(tokens, posTags, ExtractorEntity.getChunker());

		// Extract keywords based on relevant parts of speech
		return extractKeywordsFromPartsOfSpeech(tokens, posTags, chunkTags, ExtractorEntity.getStopWords());
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

	private Map<String, Integer> extractKeywordsFromPartsOfSpeech(String[] tokens, String[] posTags, String[] chunkTags, List<String> stopWords) {
		Map<String, Integer> wordFrequencies = new HashMap<>(tokens.length);
		dealWithSpecialTokens(tokens, posTags, stopWords, wordFrequencies);
		return wordFrequencies.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(
						LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll
				);
	}



	private void dealWithSpecialTokens(String[] tokens, String[] posTags, List<String> stopWords, Map<String, Integer> wordFrequencies) {
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			String posTag = posTags[i];
			// Consider only nouns and adjectives
			if (!posTag.startsWith("N") && !posTag.startsWith("J")) {
				continue;
			}
			if (token.matches(".-*\\[.*\\].*")) {
				// Remove any parts-of-speech tags from token
				token = token.replaceAll("-\\[.*\\]", "");
			}
			token = token.replaceAll("[^a-zA-Z0-9]", " ").trim().toLowerCase();
			token = token.replace("/", " ").trim().toLowerCase();
			if (!stopWords.contains(token)) {
				String[] wordsSplit = token.split(" ");
				Stream.of(wordsSplit).filter(ele -> !"".equals(ele)).forEach(word ->
						wordFrequencies.merge(word, 1, Integer::sum)
				);
			}
		}
	}
}
