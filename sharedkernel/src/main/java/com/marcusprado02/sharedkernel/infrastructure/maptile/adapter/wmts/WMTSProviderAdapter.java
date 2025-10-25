package com.marcusprado02.sharedkernel.infrastructure.maptile.adapter.wmts;

import java.util.Set;

import com.marcusprado02.sharedkernel.infrastructure.maptile.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.maptile.core.BaseProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.Capabilities;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.ProviderMetadata;

/**
 * Adaptador WMTS genérico (stub). Substitua o fetch() por uma chamada HTTP real ao seu endpoint WMTS.
 */
public class WMTSProviderAdapter extends BaseProviderAdapter {

    private static final String ID = "wmts-generic";

    @Override
    public ProviderMetadata metadata() {
        // Formatos e camadas suportados podem ser ajustados conforme seu provedor
        return new ProviderMetadata(
                ID,
                "WMTS Generic",
                "1.0",
                Set.of(TileFormat.PNG, TileFormat.JPEG, TileFormat.WEBP),
                Set.of("base"),                 // layers / estilos
                Set.of("GLOBAL"),               // regiões atendidas
                new Capabilities(true, true, true, true, true) // cache, etag, compressão, ranges, prefetch
        );
    }

    @Override
    public TileData fetch(TileKey key, TileContext ctx, Policy p) {
        // Exemplo mínimo só para compilar: monta meta e retorna um tile vazio (stub).
        // Em produção, aqui você deveria:
        // 1) Montar a URL WMTS (SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=...&TILEMATRIX=...&TILEROW=...&TILECOL=...)
        // 2) Chamar HTTP GET
        // 3) Preencher bytes + contentType de acordo com a resposta
        // 4) Respeitar ETag/Last-Modified (preencher EtagInfo)
        // 5) Tratar TMS invertendo Y se o provedor usar TMS
        String contentType = switch (key.format()) {
            case JPEG -> "image/jpeg";
            case WEBP -> "image/webp";
            default -> "image/png";
        };

        // Meta básica com TTL padrão (p.ex., 1 dia)
        TileMeta meta = new TileMeta(
                ID,
                new EtagInfo(null, null),
                86_400, // ttlSeconds
                System.currentTimeMillis()
        );

        // Tile “vazio” apenas para satisfazer a assinatura e compilar.
        // Troque por bytes vindos da resposta HTTP real.
        byte[] bytes = new byte[0];

        return okRaster(bytes, contentType, meta);
    }
}
