package com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.redis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.*;

public final class RedisSlidingWindowLimiter implements RateLimiter {
    private final RedisExecutor redis; // sua porta (Lettuce/Jedis/Redisson)
    private final String scriptSha1;

    public interface RedisExecutor {
        Object evalsha(String sha1, List<String> keys, List<String> argv);
        String scriptLoad(String script);
    }

    public RedisSlidingWindowLimiter(RedisExecutor r, String luaScript){
        this.redis = r; this.scriptSha1 = r.scriptLoad(luaScript);
    }

    @Override public Decision evaluateAndConsume(RateKey key, LimitSpec spec) {
        long now = System.currentTimeMillis();
        var redisKey = "rl:%s:%s".formatted(spec.name(), hashKey(key.asString()));
        var argv = List.of(Long.toString(now), Long.toString(spec.window().toMillis()), Integer.toString(spec.capacity()));
        List<Long> res = (List<Long>) redis.evalsha(scriptSha1, List.of(redisKey), argv);
        boolean allowed = res.get(0) == 1L;
        long remaining = res.get(1);
        long reset = res.get(2);
        return new Decision(allowed, remaining, reset, Map.of("distributed", true, "count", res.get(3)));
    }

    private static String hashKey(String s){
        try {
            var md = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e){ throw new RuntimeException(e); }
    }
}
