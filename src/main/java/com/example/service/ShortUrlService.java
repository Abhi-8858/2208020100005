package com.example.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CreateShortUrlRequest;
import com.example.dto.CreateShortUrlResponse;
import com.example.dto.ShortUrlStatsResponse;
import com.example.entity.Click;
import com.example.entity.ShortUrl;
import com.example.exception.NotFoundException;
import com.example.repository.ClickRepository;
import com.example.repository.ShortUrlRepository;

@Service
public class ShortUrlService {
    private final ShortUrlRepository shortUrlRepository;
    private final ClickRepository clickRepository;

    @Value("${server.port:8080}")
    private int serverPort;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, ClickRepository clickRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.clickRepository = clickRepository;
    }

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int GENERATED_LENGTH = 6;
    private static final int DEFAULT_VALIDITY_MINUTES = 30;

    @Transactional
    public CreateShortUrlResponse createShortUrl(CreateShortUrlRequest req, String host) {
        int validity = Optional.ofNullable(req.getValidity()).orElse(DEFAULT_VALIDITY_MINUTES);

        String shortcode = req.getShortcode();
        if (shortcode != null && !shortcode.isBlank()) {
            // validate uniqueness
            if (shortUrlRepository.existsByShortcode(shortcode)) {
                throw new IllegalArgumentException("Custom shortcode already in use");
            }
        } else {
            shortcode = generateUniqueShortcode();
        }

        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofMinutes(validity));

        
        ShortUrl s = ShortUrl.builder()
                .originalUrl(req.getUrl())
                .shortcode(shortcode)
                .createdAt(now)
                .expiresAt(expiry)
                .build();
        shortUrlRepository.save(s);

        String shortLink = buildShortLink(host, shortcode);

        return CreateShortUrlResponse.builder()
                .shortLink(shortLink)
                .expiry(expiry.toString())
                .build();
    }

    private String buildShortLink(String host, String shortcode) {
        // host will be like http://localhost:8080 or hostname:port; if host absent default to localhost:8080
        String prefix = host;
        if (!prefix.startsWith("http://") && !prefix.startsWith("https://")) {
            prefix = "http://" + prefix;
        }
        if (prefix.endsWith("/")) prefix = prefix.substring(0, prefix.length()-1);
        return prefix + "/" + shortcode;
    }

    private String generateUniqueShortcode() {
        for (int attempt = 0; attempt < 10_000; attempt++) {
            String candidate = randomBase62(GENERATED_LENGTH);
            if (!shortUrlRepository.existsByShortcode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique shortcode");
    }

    private String randomBase62(int length) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            int idx = rnd.nextInt(BASE62.length());
            sb.append(BASE62.charAt(idx));
        }
        return sb.toString();
    }

    @Transactional
    public ShortUrl getAndRecordClick(String shortcode, String referrer, String ip, String userAgent) {
        ShortUrl s = shortUrlRepository.findByShortcode(shortcode)
                .orElseThrow(() -> new NotFoundException("Shortcode not found"));

        Instant now = Instant.now();
        if (s.getExpiresAt() != null && now.isAfter(s.getExpiresAt())) {
            throw new NotFoundException("Short link expired");
        }

        Click c = Click.builder()
                .clickedAt(now)
                .referrer(referrer)
                .ip(ip)
                .userAgent(userAgent)
                .location(deriveLocationFromIp(ip))
                .shortUrl(s)
                .build();
        clickRepository.save(c);
        s.getClicks().add(c);
        shortUrlRepository.save(s);

        return s;
    }

    private String deriveLocationFromIp(String ip) {
        // We don't call external services here. For coarse-grained location, we simply return IP.
        // If you want country, integrate a geoip DB or external API.
        return ip;
    }

    @Transactional(readOnly = true)
    public ShortUrlStatsResponse getStats(String shortcode) {
        ShortUrl s = shortUrlRepository.findByShortcode(shortcode)
                .orElseThrow(() -> new NotFoundException("Shortcode not found"));

        List<ShortUrlStatsResponse.ClickDetail> clicks = s.getClicks().stream()
                .map(c -> new ShortUrlStatsResponse.ClickDetail(
                        c.getClickedAt(), c.getReferrer(), c.getIp(), c.getUserAgent(), c.getLocation()
                )).collect(Collectors.toList());

        return ShortUrlStatsResponse.builder()
                .originalUrl(s.getOriginalUrl())
                .shortcode(s.getShortcode())
                .createdAt(s.getCreatedAt())
                .expiry(s.getExpiresAt())
                .totalClicks(clicks.size())
                .clicks(clicks)
                .build();
    }
}
