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

package me.petulikan1.commons.redis.abstracts;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.petulikan1.commons.redis.completablefuture.CompletableFutureMain;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public abstract class IRedisReader<K extends IRedisMessage, V extends IRedisCFResponse, T extends IRedisCFRequest> {
    @Getter
    public final HashMap<String, CompletableFuture<V>> futureMap = new HashMap<>();
    @Getter
    private final String internalChannel;
    private final Class<K> kClass;
    private final Class<V> vClass;
    private final Class<T> tClass;

    public Gson gson = new Gson();

    private static CompletableFutureMain cfm;

    public static void init(@NotNull CompletableFutureMain cfm) {
        if (IRedisReader.cfm != null) {
            throw new RuntimeException("IRedisMessage already initialized!");
        }
        IRedisReader.cfm = cfm;
    }

    public void handleNormal_(String channel, String message) {
        try {
            K k = gson.fromJson(message, kClass);
            handleNormal(channel, message, k);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRequest_(String channel, String message) {
        try {
            T t = gson.fromJson(message, tClass);
            handleRequest(channel, message, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleResponse_(String message) {
        V v = gson.fromJson(message, vClass);
        try {
            futureMap.get(v.getRequestCFKey()).complete(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        futureMap.remove(v.getRequestCFKey());
    }

    public abstract void handleRequest(String channel, String message, T t);

    public abstract void handleNormal(String channel, String message, K k);

    public CompletableFutureMain getMain() {
        return cfm;
    }

    public void sendMessage(IRedisMessage message, String channel) {
        getMain().sendMessage(gson.toJson(message), channel);
    }

    public void sendMessage(IRedisMessage message) {
        getMain().sendMessage(gson.toJson(message), getInternalChannel());
    }

    public V sendMessageFuture(IRedisCFRequest request) {
        return sendMessageFuture(request, 2);
    }

    public V sendMessageFuture(IRedisCFRequest request, int waitTime) {
        CompletableFuture<V> future = sendMessageCF(request, waitTime);
        V v = null;
        try {
            v = future.get(waitTime, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
        return v;
    }

    public CompletableFuture<V> sendMessageCF(IRedisCFRequest request) {
        return sendMessageCF(request, 2);
    }

    public CompletableFuture<V> sendMessageCF(IRedisCFRequest request, int waitTime) {
        CompletableFuture<V> future = new CompletableFuture<V>().orTimeout(waitTime, TimeUnit.SECONDS);
        futureMap.put(request.getRequestCFKey(), future);
        getMain().sendMessage(request, getInternalChannel());
        return future;
    }


}
