package com.example.refapp.services;

import com.example.refapp.models.RequestParam;

import java.util.List;

public interface RemoteClient {
	<T> T execute(final DataRequestType requestType, final List<RequestParam> requestParams, Class<? extends T> resultType)
			throws Exception;

	void release();
}