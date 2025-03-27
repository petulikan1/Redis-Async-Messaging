package me.petulikan1.redisAsync.showcase.redis;

import me.petulikan1.commons.redis.abstracts.IRedisReader;
import me.petulikan1.redisAsync.showcase.Loader;

import java.util.Objects;

public class RedisReader extends IRedisReader<RedisMessage, RedisResponse, RedisRequest> {
    public RedisReader(String internalChannel) {
        super(internalChannel, RedisMessage.class, RedisResponse.class, RedisRequest.class);
    }

    @Override
    public void handleRequest(String channel, String message, RedisRequest redisRequest) {
        if(redisRequest.getType()==MessageType.AUTHENTICATE){
            handleAuthentication(redisRequest);
        }
    }

    private void handleAuthentication(RedisRequest redisRequest) {
        if(redisRequest.getUsername().equals(username) && redisRequest.getPassword().equals(password)){
            sendMessage(new RedisResponse(redisRequest,AuthType.AUTHENTICATED,redisRequest.getUsername())); // IN THIS SCENARIO, THIS WILL RUN AS THE COMPARE WILL RETURN TRUE AND WE'LL SEND A AuthType.AUTHENTICATED state
        }else if(redisRequest.getUsername().equals(username)){
            sendMessage(new RedisResponse(redisRequest,AuthType.UNAUTHENTICATED,redisRequest.getUsername())); // THIS WON'T BE SENT AS THE INITIAL VALUES ARE MATCHED, BUT THIS WAY, WE COULD DENY A REQUEST
        }
    }

    private final String username = "petulikan1",password="mySecretPassword";

    @Override
    public void handleNormal(String channel, String message, RedisMessage redisMessage) {
        if (Objects.requireNonNull(redisMessage.getType()) == MessageType.MESSAGE) {
            handleMessage(redisMessage);
        } else {
            return;
        }
    }
    private void handleMessage(RedisMessage redisMessage) {
        Loader.LOGGER.info("THIS IS RECEIVED REDIS MESSAGE: ["+redisMessage.getData()+"]");
    }
}
