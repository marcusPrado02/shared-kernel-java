package com.marcusprado02.sharedkernel.adapters.in.rest.versioning;


import java.net.URI;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public final class AcceptHeader {
    public record Choice(String raw, double q, Map<String,String> params){}
    private static final Pattern PART = Pattern.compile("([^;]+)(;.*)?");

    public static List<Choice> parse(String accept){
        if (accept == null || accept.isBlank()) return List.of(new Choice("application/json",1.0, Map.of()));
        return Arrays.stream(accept.split(","))
                .map(String::trim)
                .map(AcceptHeader::parsePart)
                .sorted(Comparator.comparingDouble((Choice c) -> -c.q))
                .toList();
    }
    private static Choice parsePart(String p){
        var m = PART.matcher(p);
        if (!m.matches()) return new Choice(p,1.0, Map.of());
        var mt = m.group(1).trim();
        Map<String,String> params = new HashMap<>();
        double q = 1.0;
        if (m.group(2)!=null){
            for (var s : m.group(2).split(";")){
                var kv = s.trim().split("=",2);
                if (kv.length==2){
                    var k = kv[0].trim().toLowerCase(); var v = kv[1].trim().replace("\"","");
                    if (k.equals("q")) q = Double.parseDouble(v);
                    else params.put(k, v);
                }
            }
        }
        return new Choice(mt,q,params);
    }

    /** Extrai `version` ou `profile` -> versão (convenção: .../profiles/<type>.v<major>[.<minor>]). */
    public static Optional<ApiVersion> versionOf(Choice c){
        if (c.params().containsKey("version")) return Optional.of(ApiVersion.parse(c.params().get("version")));
        if (c.params().containsKey("profile")){
            try {
                var u = URI.create(c.params().get("profile"));
                var p = u.getPath();
                var m = Pattern.compile("\\.v(\\d+)(?:\\.(\\d+))?$").matcher(p);
                if (m.find()){
                    var M = Integer.parseInt(m.group(1));
                    var mnr = m.group(2)==null?0:Integer.parseInt(m.group(2));
                    return Optional.of(ApiVersion.of(M,mnr));
                }
            } catch (Exception ignored){}
        }
        return Optional.empty();
    }
}

