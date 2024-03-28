package com.resume.ats.check.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dimension {
    private List<String> hardSkills;
    private List<String> softSkills;
    private List<String> experience;
    private List<String> characterTags;
}
