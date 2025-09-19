package com.example.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.CreateShortUrlRequest;
import com.example.dto.CreateShortUrlResponse;
import com.example.dto.ErrorResponse;
import com.example.dto.ShortUrlStatsResponse;
import com.example.entity.ShortUrl;
import com.example.exception.NotFoundException;
import com.example.service.ShortUrlService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/shorturls") // 👈 cleaner base path
public class ShortUrlController {

    private final ShortUrlService svc;

    public ShortUrlController(ShortUrlService svc) {
        this.svc = svc;
    }

    // 🔗 Create Short URL
    @PostMapping
    public ResponseEntity<?> createShortUrl(@Valid @RequestBody CreateShortUrlRequest req, HttpServletRequest request) {
        try {
            String hostHeader = request.getHeader("X-Forwarded-Host");
            String host = hostHeader != null
                    ? request.getScheme() + "://" + hostHeader
                    : request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

            CreateShortUrlResponse resp = svc.createShortUrl(req, host);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 👈 helpful for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Server error"));
        }
    }

    // 🔁 Redirect to Original URL
    @GetMapping("/{shortcode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortcode, HttpServletRequest request) {
        try {
            String referrer = request.getHeader("Referer");
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");

            ShortUrl s = svc.getAndRecordClick(shortcode, referrer, ip, ua);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(java.net.URI.create(s.getOriginalUrl()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 👈 show exact error in console
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error");
        }
    }

    // 📊 Get Stats for Short URL
    @GetMapping("/stats/{shortcode}")
    public ResponseEntity<?> getStats(@PathVariable String shortcode) {
        System.out.println("ShortUrlController.getStats() called for: " + shortcode);
        try {
            ShortUrlStatsResponse stats = svc.getStats(shortcode);
            return ResponseEntity.ok(stats);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 👈 log full error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Server error"));
        }
    }
}
