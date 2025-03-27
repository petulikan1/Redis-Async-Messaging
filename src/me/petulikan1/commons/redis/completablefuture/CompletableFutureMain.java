/*
 * Copyright (c) 2025 petulikan1
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */


package me.petulikan1.commons.redis.completablefuture;

import com.google.gson.Gson;
import lombok.Getter;
import me.petulikan1.commons.redis.abstracts.*;
import me.petulikan1.commons.redis.comunication.Subscription;
import me.petulikan1.commons.redis.utils.Utils;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CompletableFutureMain {

    public final ScheduledExecutorService poolExecutor = Executors.newScheduledThreadPool(20);
    public final HashMap<String, IRedisReader<?, ?, ?>> readers = new HashMap<>();
    public final HashSet<String> channels = new HashSet<>();
    private final String DEFAULT_CHANNEL_ID = "mRedisVelocity";
    public boolean closing;
    public Gson gson = new Gson();
    public JedisPool jedisPool;
    private Subscription sub;
    @Getter
    private Initializer initializer;
    @Getter
    private String SERVER_ID;


    public CompletableFutureMain init(String serverID, JedisPool jedisPool, Initializer initializer) {
        this.jedisPool = jedisPool;
        this.SERVER_ID = serverID;
        this.initializer = initializer;
        this.sub = new Subscription(DEFAULT_CHANNEL_ID, SERVER_ID, gson, this, initializer);
        poolExecutor.submit(sub);
        initializer.info("Successfully initialized CompletableFutureMain! ServerID: &c" + serverID);
        return this;
    }

    public CompletableFutureMain init(String address, String username, String password, String serverID, Initializer initializer) {
        String[] split = address.split(":");
        String host = split[0];
        Utils.validate(serverID == null, "ServerID cannot be null");
        int port = address.length() > 1 ? Integer.parseInt(split[1]) : Protocol.DEFAULT_PORT;
        if (username == null || username.isEmpty()) {
            this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, password, false);
        } else {
            this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, username, password, false);
        }
        this.SERVER_ID = serverID;
        this.initializer = initializer;
        this.sub = new Subscription(DEFAULT_CHANNEL_ID, SERVER_ID, gson, this, initializer);
        poolExecutor.submit(sub);
        initializer.info("Successfully initialized CompletableFutureMain! ServerID: &c" + serverID);
        return this;
    }

    public void registerReader(IRedisReader<?, ?, ?> reader) {
        if (readers.containsKey(reader.getInternalChannel())) {
            throw new RuntimeException("Reader with channel name " + reader.getInternalChannel() + " is already registered!");
        }
        initializer.log("Registering RedisReader with channel name: " + reader.getInternalChannel());
        readers.put(reader.getInternalChannel(), reader);
        registerChannel(reader.getInternalChannel());
    }

    public void unregisterReader(IRedisReader<?, ?, ?> reader) {
        initializer.log("Unregistering RedisReader with channel name: " + reader.getInternalChannel());
        readers.remove(reader.getInternalChannel());
        unregisterChannel(reader.getInternalChannel());
    }

    private void registerChannel(String channel) {
        synchronized (channels) {
            channels.add(channel);
        }
        sub.subscribe(channel);
    }

    private void unregisterChannel(String channel) {
        synchronized (channels) {
            channels.remove(channel);
        }
        sub.unsubscribe(channel);
    }

    public void sendMessage(String msg, String channel) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(IRedisMessage message, String channel) {
        sendMessage(gson.toJson(message), channel);
    }

    public static <V extends IRedisCFResponse> V sendMessageCF(@NotNull IRedisReader<?, V, ?> reader, @NotNull IRedisCFRequest request) {
        return reader.sendMessageFuture(request);
    }

    public static <V extends IRedisCFResponse> V sendMessageCF(@NotNull IRedisReader<?, V, ?> reader, @NotNull IRedisCFRequest request, int timeout) {
        return reader.sendMessageFuture(request, timeout);
    }

    public static <V extends IRedisCFResponse> CompletableFuture<V> sendMessageFuture(IRedisReader<?, V, ?> reader, IRedisCFRequest request, int timeout) {
        return reader.sendMessageCF(request, timeout);
    }

    public static <V extends IRedisCFResponse> CompletableFuture<V> sendMessageFuture(IRedisReader<?, V, ?> reader, IRedisCFRequest request) {
        return reader.sendMessageCF(request);
    }

    public void close() {
        for (IRedisReader<?, ?, ?> reader : readers.values()) {
            unregisterReader(reader);
        }
        this.closing = true;
        channels.clear();
    }


}
