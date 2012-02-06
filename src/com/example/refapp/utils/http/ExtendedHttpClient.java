package com.example.refapp.utils.http;

import org.apache.http.client.HttpClient;

public interface ExtendedHttpClient extends HttpClient
{
  void close();
}