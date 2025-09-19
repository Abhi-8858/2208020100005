package com.example.dto;

import lombok.Builder;

@Builder
public class CreateShortUrlResponse {
    private String shortLink;
    private String expiry; // ISO 8601
    
    
    public CreateShortUrlResponse() {
		// TODO Auto-generated constructor stub
	}

    
    

	public String getShortLink() {
		return shortLink;
	}


	public void setShortLink(String shortLink) {
		this.shortLink = shortLink;
	}


	public String getExpiry() {
		return expiry;
	}


	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}




	public CreateShortUrlResponse(String shortLink, String expiry) {
		super();
		this.shortLink = shortLink;
		this.expiry = expiry;
	}
    
    
}
