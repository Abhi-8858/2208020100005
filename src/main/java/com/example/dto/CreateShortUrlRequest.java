package com.example.dto;

import jakarta.validation.constraints.*;

public class CreateShortUrlRequest {

	public CreateShortUrlRequest() {
		// TODO Auto-generated constructor stub
	}
    @NotBlank
    @Size(max = 2000)
    private String url;

    /**
     * in minutes; optional
     */
    @Min(1)
    private Integer validity;

    /**
     * optional custom shortcode
     */
    @Pattern(regexp = "^[A-Za-z0-9\\-_.]{3,50}$", message = "shortcode must be alphanumeric (and -_. allowed) and length 3-50")
    private String shortcode;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getValidity() {
		return validity;
	}

	public void setValidity(Integer validity) {
		this.validity = validity;
	}

	public String getShortcode() {
		return shortcode;
	}

	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}
    
    
    
}
