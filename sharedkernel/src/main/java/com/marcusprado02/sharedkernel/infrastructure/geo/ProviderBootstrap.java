package com.marcusprado02.sharedkernel.infrastructure.geo;

import io.micrometer.core.instrument.MeterRegistry;
import okhttp3.OkHttpClient;

import java.util.*;

import com.marcusprado02.sharedkernel.infrastructure.geo.spi.GeocodingProvider;

public class ProviderBootstrap {
    public static List<GeocodingProvider> load(OkHttpClient http, MeterRegistry metrics, Map<String,String> env){
        ServiceLoader<GeocodingProvider> loader = ServiceLoader.load(GeocodingProvider.class);
        List<GeocodingProvider> list = new ArrayList<>();
        for (GeocodingProvider p : loader) {
            // Exemplo: se precisa de apiKey, o construtor do provedor deve aceitar via env, use reflection simples ou provide factories.
            // Aqui, assuma provedores com construtores (http, key, metrics) e chaves: GOOGLE_API_KEY, MAPBOX_TOKEN, HERE_API_KEY, NOMINATIM_BASE_URL.
            try {
                var cls = p.getClass();
                var ctor = cls.getDeclaredConstructors()[0];
                String key = switch (p.name()) {
                    case "google"   -> env.getOrDefault("GOOGLE_API_KEY", "");
                    case "mapbox"   -> env.getOrDefault("MAPBOX_TOKEN", "");
                    case "here"     -> env.getOrDefault("HERE_API_KEY", "");
                    case "nominatim"-> env.getOrDefault("NOMINATIM_BASE_URL", "https://nominatim.openstreetmap.org");
                    default -> "";
                };
                var prov = ctor.getParameterCount()==3
                    ? (GeocodingProvider) ctor.newInstance(http, key, metrics)
                    : p; // fallback
                list.add(prov);
            } catch (Exception e) { /* logue e ignore provedor mal configurado */ }
        }
        if (list.isEmpty()) throw new IllegalStateException("Nenhum GeocodingProvider carregado");
        // Ordenação por prioridade via env: GEOCODING_PROVIDERS=google,mapbox,here,nominatim
        var order = env.getOrDefault("GEOCODING_PROVIDERS","google,mapbox,here,nominatim").split(",");
        list.sort(Comparator.comparingInt(p -> indexOf(order, p.name())));
        return list;
    }
    private static int indexOf(String[] arr, String name){
        for (int i=0;i<arr.length;i++) if (arr[i].trim().equalsIgnoreCase(name)) return i;
        return Integer.MAX_VALUE;
    }
}
