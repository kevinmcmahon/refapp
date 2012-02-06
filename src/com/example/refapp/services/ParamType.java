package com.example.refapp.services;

public enum ParamType {
    SOURCES("sources"),
    SEARCH_TEXT("query"),
    APP_ID("appid"),
    POST_BODY("_");

	public final String paramName;

	ParamType(String paramName) {
		this.paramName = paramName;
	}
}