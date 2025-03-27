package me.petulikan1.redisAsync.showcase.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.petulikan1.commons.redis.abstracts.IRedisMessage;

@RequiredArgsConstructor
@Getter
public class RedisMessage extends IRedisMessage {

    private final MessageType type;
    private final String data;

}
