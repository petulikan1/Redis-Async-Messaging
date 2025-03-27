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

import me.petulikan1.commons.redis.completablefuture.CompletableFutureMain;

public abstract class Initializer {

    private CompletableFutureMain cfm;

    public abstract void info(String message);

    public abstract void log(String message);

    public abstract void error(String message);
    public abstract void error(String message,Throwable e);

    public abstract CompletableFutureMain getCfm();

}
