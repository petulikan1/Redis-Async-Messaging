package me.petulikan1.redisAsync.showcase.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.petulikan1.commons.redis.abstracts.IRedisCFRequest;

@RequiredArgsConstructor
@Getter
public class RedisRequest extends IRedisCFRequest {

    private final MessageType type;
    private final String username,password;

}
