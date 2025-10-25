package com.marcusprado02.sharedkernel.adapters.in.sse.example.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.adapters.in.sse.core.SseBroadcaster;
import com.marcusprado02.sharedkernel.adapters.in.sse.core.SseMessage;
import com.marcusprado02.sharedkernel.adapters.in.sse.webflux.InProcSseSink;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class RedisBroadcaster implements SseBroadcaster {

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    @Override
    public void broadcast(String topic, SseMessage msg) {
        try {
            redis.convertAndSend("sse:" + topic, om.writeValueAsString(msg));
        } catch (Exception e) {
            // TODO: logar erro
        }
    }

    /** Container de listeners para o padrão de canal "sse:*". */
    @Bean
    RedisMessageListenerContainer sseListenerContainer(RedisConnectionFactory cf,
                                                       InProcSseSink sink) {
        RedisMessageListenerContainer c = new RedisMessageListenerContainer();
        c.setConnectionFactory(cf);

        MessageListener listener = (message, pattern) -> {
            try {
                String channel = new String(message.getChannel());
                String payload = new String(message.getBody());
                SseMessage m = om.readValue(payload, SseMessage.class);
                sink.emit(topicFromChannel(channel), m);
            } catch (Exception ignored) { /* TODO logar */ }
        };

        c.addMessageListener(listener, new PatternTopic("sse:*"));
        return c;
    }

    private static String topicFromChannel(String ch) {
        int i = ch.indexOf(':');
        return (i >= 0 && i < ch.length() - 1) ? ch.substring(i + 1) : ch;
    }
}
