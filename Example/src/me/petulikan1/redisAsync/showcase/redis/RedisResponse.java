package me.petulikan1.redisAsync.showcase.redis;

import lombok.Getter;
import me.petulikan1.commons.redis.abstracts.IRedisCFResponse;

@Getter
public class RedisResponse extends IRedisCFResponse {

    private final RedisRequest request;
    private final AuthType authType;
    private final String user;


    public RedisResponse(RedisRequest request, AuthType authType,String user) {
        super(request);
        this.request = request;
        this.authType = authType;
        this.user = user;
    }
}
