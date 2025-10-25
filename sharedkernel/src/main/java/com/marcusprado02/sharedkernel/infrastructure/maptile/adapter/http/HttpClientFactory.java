package com.marcusprado02.sharedkernel.infrastructure.maptile.adapter.http;

import java.net.http.HttpClient;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.ProviderConfig;

public interface HttpClientFactory {
    HttpClient create(ProviderConfig cfg);

    static HttpClient defaultClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}