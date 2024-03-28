package com.resume.ats.check.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
  String[] tokens;
  String[] posTags;
  String[] chunkTags;
}
