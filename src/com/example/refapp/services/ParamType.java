package com.example.refapp.services;

public enum ParamType {
    SOURCES("sources"),
    SEARCH_TEXT("query"),
    APP_ID("appid"),
    COUNT("web.count"),
    OFFSET("web.offset"),
    POST_BODY("_");

	public final String paramName;

	ParamType(String paramName) {
		this.paramName = paramName;
	}
}