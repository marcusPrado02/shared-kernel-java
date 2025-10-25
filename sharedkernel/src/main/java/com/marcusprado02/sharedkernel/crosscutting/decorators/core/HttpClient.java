package com.marcusprado02.sharedkernel.crosscutting.decorators.core;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SuppressWarnings("rawtypes")
public interface HttpClient extends Port<HttpRequest, HttpResponse> {}

