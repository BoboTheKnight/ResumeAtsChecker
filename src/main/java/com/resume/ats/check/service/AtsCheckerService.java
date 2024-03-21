package com.resume.ats.check.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.resume.ats.check.models.ATSDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtsCheckerService {

	private final KeywordExtractorService keywordExtractorService;
	private final ScanPdfService scanPdfService;

	public ATSDetail generateAtsDetails(MultipartFile file,String desc) throws IOException{
		Map<String, Integer> descKeywords = keywordExtractorService.extractKeywords(desc);

		String pdfContent = scanPdfService.scanPdfFromFile(file);
		Map<String, Integer> unmatched = unmatchedKeywords(pdfContent, descKeywords);

		int matchedNum = descKeywords.size() - unmatched.size();
	    double percentage = (double) matchedNum / descKeywords.size() * 100;
	    String matchPercentage = String.format("%.2f", percentage) + "% of keywords matched" ;

	    return new ATSDetail(descKeywords, unmatched, matchPercentage);
	}
	private Map<String, Integer> unmatchedKeywords(String pdfContent, Map<String, Integer> total){
		Map<String, Integer> unmatchedKeywords = new LinkedHashMap<>();

		for (Map.Entry<String, Integer> entry : total.entrySet()) {
			String keyword = entry.getKey().toLowerCase();
			Integer frequencies = entry.getValue();
			if (!pdfContent.contains(keyword)) {
				unmatchedKeywords.put(keyword, frequencies);
			}
		}
		return unmatchedKeywords;
	}
}
