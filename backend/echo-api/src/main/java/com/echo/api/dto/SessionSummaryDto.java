package com.echo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for session summary information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryDto {

    private String sessionId;
    private Long recordCount;
}