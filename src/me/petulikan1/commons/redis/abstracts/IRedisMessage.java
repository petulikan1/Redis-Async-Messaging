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

import lombok.Getter;
import me.petulikan1.commons.redis.completablefuture.CompletableFutureMain;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public abstract class IRedisMessage {

    private final String serverID;

    private static CompletableFutureMain cfm;

    public static void init(@NotNull CompletableFutureMain cfm) {
        if (IRedisMessage.cfm != null) {
            throw new RuntimeException("IRedisMessage already initialized!");
        }
        IRedisMessage.cfm = cfm;
    }


    protected IRedisMessage() {
        Objects.requireNonNull(cfm, "CompletableFutureMain not initialized!");
        this.serverID = cfm.getSERVER_ID();
    }
}
