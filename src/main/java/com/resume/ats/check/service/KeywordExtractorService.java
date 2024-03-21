package com.resume.ats.check.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

@Service
public class KeywordExtractorService {

	private static final String TOKEN_MODEL_PATH = "src/main/resources/en-token.bin";
	private static final String POS_MODEL_PATH = "src/main/resources/en-pos-maxent.bin";
	private static final String STOPWORDS_PATH = "src/main/resources/stopwords.txt";

	private static Tokenizer tokenizer;
	private static POSTaggerME posTagger;
	private static List<String> stopWords;

	@PostConstruct
	public static void init() throws IOException {
		tokenizer = loadTokenizer();
		posTagger = loadPOSTagger();
		stopWords = loadStopWords();
	}

	public Map<String, Integer> extractKeywords(String inputText){

		// Tokenize input text
		String[] tokens = tokenizeInputText(inputText, tokenizer);

		// Tag parts of speech in input text
		String[] posTags = tagPartsOfSpeech(tokens, posTagger);

		// Extract keywords based on relevant parts of speech
		return extractKeywordsFromPartsOfSpeech(tokens, posTags, stopWords);
	}

	private static Tokenizer loadTokenizer() throws IOException {
		try (InputStream tokenModelIn = new FileInputStream(TOKEN_MODEL_PATH)) {
			TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
			return new TokenizerME(tokenModel);
		}
	}

	private static POSTaggerME loadPOSTagger() throws IOException {
		try (InputStream posModelIn = new FileInputStream(POS_MODEL_PATH)) {
			POSModel posModel = new POSModel(posModelIn);
			return new POSTaggerME(posModel);
		}
	}

	private static List<String> loadStopWords() throws IOException {
		try (InputStream stopWordsIn = new FileInputStream(STOPWORDS_PATH)) {
			return IOUtils.readLines(stopWordsIn, StandardCharsets.UTF_8);
		}
	}

	private String[] tokenizeInputText(String inputText, Tokenizer tokenizer) {
		return tokenizer.tokenize(inputText);
	}

	private String[] tagPartsOfSpeech(String[] tokens, POSTaggerME posTagger) {
		return posTagger.tag(tokens);
	}

	private Map<String, Integer> extractKeywordsFromPartsOfSpeech(String[] tokens, String[] posTags, List<String> stopWords) {
		Map<String, Integer> wordFrequencies = new HashMap<>(tokens.length);
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
				Stream.of(wordsSplit).filter(ele-> !"".equals(ele)).forEach(word ->
						wordFrequencies.merge(word, 1, Integer::sum)
				);
			}
		}

		return wordFrequencies.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(
						LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll
				);
	}
}
