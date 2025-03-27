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


package me.petulikan1.redisAsync.showcase;

import me.petulikan1.commons.redis.abstracts.IRedisMessage;
import me.petulikan1.commons.redis.abstracts.Initializer;
import me.petulikan1.commons.redis.completablefuture.CompletableFutureMain;
import me.petulikan1.redisAsync.showcase.redis.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Loader extends Initializer {

    public static Logger LOGGER = Logger.getLogger(Loader.class.getName());

    private CompletableFutureMain cfm;

    public static void main(String[] args) {
        Loader loader = new Loader();

        //INITIALIZATION LOGIC - THIS LOADS THE CLASS & CONNECTS TO THE REDIS DATABASE
        CompletableFutureMain cfm = new CompletableFutureMain();
        cfm.init("address", "username", "password", "proxy_1", loader);
        loader.cfm=cfm;
        IRedisMessage.init(cfm);


        //THIS IS THE REAL USAGE
        RedisReader reader;
        // We create and register a new instance of the RedisReader that handles all the messages for us, there are methods that allow us to send messages trough it
        cfm.registerReader(reader = new RedisReader("testing_channel"));


        // SEND A REGULAR MESSAGE THAT DOESN'T REQUIRE A RESPONSE
        reader.sendMessage(new RedisMessage(MessageType.MESSAGE, "This is a test message to show that it works!"));

        // SEND A COMPLETABLE FUTURE MESSAGE HANDLED WHEN WE RECEIVE RESPONSE OR REACH THE TIMEOUT THRESHOLD
        reader.sendMessageCF(new RedisRequest(MessageType.AUTHENTICATE, "petulikan1", "mySecretPassword"))
                .orTimeout(3, TimeUnit.SECONDS) //SET THE TIMEOUT THRESHOLD
                .whenComplete((a, b) -> { //AWAIT FOR THE RESPONSE
                    if (b != null) { // b is a throwable - in case of TimeoutException or any other error
                        LOGGER.log(Level.SEVERE, "An error occurred while handling response!", b);
                        return;
                    }
                    // Here we get the real data that we received from the other side
                    if (a.getAuthType() == AuthType.AUTHENTICATED) {
                        LOGGER.fine("User has been successfully authenticated!"); //THIS MEESAGE IN THIS EXAMPLE WILL GET PRINTED AS THE RESULT MATCHES ON THE OTHER SIDE
                        return;
                    }
                    if (a.getAuthType() == AuthType.UNAUTHENTICATED) {
                        LOGGER.fine("User was not unauthenticated!");
                        return;
                    }
                });
    }

    @Override
    public void info(String message) {
        LOGGER.info(message);
    }

    @Override
    public void log(String message) {
        LOGGER.finest(message);
    }

    @Override
    public void error(String message) {
        LOGGER.severe(message);
    }

    @Override
    public void error(String message, Throwable e) {
        LOGGER.log(Level.SEVERE, message, e);
    }

    @Override
    public CompletableFutureMain getCfm() {
        return cfm;
    }
}
