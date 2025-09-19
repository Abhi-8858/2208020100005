package com.example.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrlStatsResponse {
    private String originalUrl;
    private String shortcode;
    private Instant createdAt;
    private Instant expiry;
    private long totalClicks;
    private List<ClickDetail> clicks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClickDetail {
        private Instant clickedAt;
        private String referrer;
        private String ip;
        private String userAgent;
        private String location; // coarse
    }
}
