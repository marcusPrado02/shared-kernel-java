package com.marcusprado02.sharedkernel.observability.dashboard.publish;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

import com.marcusprado02.sharedkernel.observability.dashboard.model.Dashboard;
import com.marcusprado02.sharedkernel.observability.dashboard.spi.DashboardPublisher;

import java.nio.charset.StandardCharsets;

public final class GrafanaPublisher implements DashboardPublisher {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String baseUrl, token;

    public GrafanaPublisher(String baseUrl, String token){
        this.baseUrl = baseUrl.endsWith("/")? baseUrl.substring(0, baseUrl.length()-1): baseUrl;
        this.token = token;
    }

    @Override public String upsert(Dashboard d, byte[] payload, String contentType) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl+"/api/dashboards/db"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization","Bearer "+token)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();
        var resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            // resposta inclui uid
            String body = resp.body();
            // Best-effort: extrair "uid":"..."
            int i = body.indexOf("\"uid\":\"");
            if (i>0) {
                int j = body.indexOf('"', i+7);
                return body.substring(i+7, j);
            }
            return d.uid;
        }
        throw new IllegalStateException("Grafana upsert failed: "+resp.statusCode()+" "+resp.body());
    }

    @Override public byte[] fetchCurrent(String uid) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl+"/api/dashboards/uid/"+uid))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization","Bearer "+token)
                .GET().build();
        var resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() == 200) return resp.body();
        return null;
    }

    @Override public String backend(){ return "grafana"; }
}
