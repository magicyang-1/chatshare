package com.aiplatform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.aiplatform.entity.PromptTemplate;
import com.aiplatform.entity.PromptCategory;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreeDRecordDTO {
    private String id;
    private String prompt;
    private String mode;
    private String art_style;
    private LocalDateTime created_at;
}