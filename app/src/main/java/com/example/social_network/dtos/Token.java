package com.example.social_network.dtos;

public class Token {

    private static Token instance;
    private String token;
    private String accessToken;
    private String refreshToken;

    private Token() {
    }

    public static Token getInstance() {
        if (instance == null) {
            instance = new Token();
        }
        return instance;
    }

    public String getToken() {
        if(token == null) return "";

        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getAccessToken() {
        if(accessToken == null) return "";

        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {

        if(refreshToken == null) return "";
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
