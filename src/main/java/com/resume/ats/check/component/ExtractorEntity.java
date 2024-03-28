package com.resume.ats.check.component;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.Getter;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static com.resume.ats.check.constant.ModelPathCons.*;

@Data
public class ExtractorEntity {
    private static final Logger logger = LogManager.getLogger(ExtractorEntity.class);

    @Getter
    private static Tokenizer tokenizer;

    @Getter
    private static POSTaggerME posTagger;

    @Getter
    private static ChunkerME chunker;

    @Getter
    private static List<String> stopWords;

    @Getter
    private static Set<String> softSkills;

    static {
        try {
            tokenizer = loadTokenizer();
            posTagger = loadPosTagger();
            chunker = loadChunker();
            stopWords = loadStopWords();
            softSkills = loadSoftSkills();
        }catch(IOException e){
            logger.error(e);
        }
    }

    private static Tokenizer loadTokenizer() throws IOException {
        try (InputStream tokenModelIn = new FileInputStream(TOKEN_MODEL_PATH)) {
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            return new TokenizerME(tokenModel);
        }
    }

    private static POSTaggerME loadPosTagger() throws IOException {
        try (InputStream posModelIn = new FileInputStream(POS_MODEL_PATH)) {
            POSModel posModel = new POSModel(posModelIn);
            return new POSTaggerME(posModel);
        }
    }
    private static ChunkerME loadChunker() throws IOException {
        try (InputStream chunkerModelIn = new FileInputStream(CHUNKER_MODEL_PATH)) {
            ChunkerModel chunkerModel = new ChunkerModel(chunkerModelIn);
            return new ChunkerME(chunkerModel);
        }
    }

    private static List<String> loadStopWords() throws IOException {
        try (Stream<String> lines = Files.lines(Path.of(STOPWORDS_PATH), StandardCharsets.UTF_8)) {
            return lines
                    .flatMap(line -> Stream.of(line.split(",")))
                    .distinct().toList();
        }
    }
    private static Set<String> loadSoftSkills() throws IOException {
        try (Stream<String> lines = Files.lines(Path.of(SOFT_SKILLS_PATH), StandardCharsets.UTF_8)) {
            Set<String> phraseSet =  lines.filter(line->!line.startsWith("#"))
                .flatMap(line -> Stream.of(line.split(",")))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

            Set<String> allSet = phraseSet.stream()
                .flatMap(ele -> Stream.of(ele.split(" ")))
                .collect(Collectors.toSet());
            allSet.addAll(phraseSet);
            return allSet;
        }
    }

}
