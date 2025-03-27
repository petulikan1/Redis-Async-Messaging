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

package me.petulikan1.commons.redis.comunication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.petulikan1.commons.redis.abstracts.IRedisReader;
import me.petulikan1.commons.redis.abstracts.Initializer;
import me.petulikan1.commons.redis.completablefuture.CompletableFutureMain;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

@AllArgsConstructor
@Getter
public class Subscription extends JedisPubSub implements Runnable {

    private final String DEFAULT_CHANNEL_ID, SERVER_ID;
    private final Gson gson;
    private final CompletableFutureMain cfm;
    private final Initializer initializer;


    @Override
    public void run() {
        boolean first = true;
        while (!cfm.closing && !Thread.currentThread().isInterrupted() && !cfm.jedisPool.isClosed()) {
            try (Jedis jedis = cfm.jedisPool.getResource()) {
                if (first) {
                    first = false;
                } else {
                    initializer.log("Redis connection re-established");
                }
                if (cfm.channels.isEmpty()) {
                    cfm.channels.add(DEFAULT_CHANNEL_ID);
                }
                jedis.subscribe(this, cfm.channels.toArray(new String[0]));
            } catch (Exception e) {
                if (cfm.closing) return;
                initializer.error("Redis connection dropped, trying to re-open connection");
                e.printStackTrace();
                try {
                    unsubscribe();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception eb) {
                    eb.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!cfm.readers.containsKey(channel)) return;
        if (!message.startsWith("{") || !message.endsWith("}")) {
            initializer.log("Discarding of incompatible Redis message: " + message + " | Channel: " + channel);
            return;
        }
        cfm.poolExecutor.submit(new Thread(() -> {
            try {
                IRedisReader<?, ?, ?> reader = cfm.readers.get(channel);
                JsonObject object = gson.fromJson(message, JsonObject.class);
                if (object.has("requestCFKey")) {
                    String server = object.get("serverID").getAsString();
                    if (server.equals(SERVER_ID)) return;
                    if (object.has("responseKey")) {
                        String requestFromServer = object.get("requestFromServerID").getAsString();
                        if (SERVER_ID.equals(requestFromServer)) {
                            reader.handleResponse_(message);
                        }
                        return;
                    }
                    reader.handleRequest_(channel, message);
                    return;
                }
                reader.handleNormal_(channel, message);
            } catch (Exception e) {
                initializer.error("An error occurred while handling Redis Request!", e);
            }
        }));
    }
}
