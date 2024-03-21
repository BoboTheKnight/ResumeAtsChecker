package com.resume.ats.check.models;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ATSDetail {
	
	private Map<String, Integer> totalKeywords;
	private Map<String, Integer> unMatchedKeywords;
	private String matchPercentage;

	public ATSDetail(Map<String, Integer> descKeywords, Map<String, Integer> unmatched, double percentage) {
	}
}
