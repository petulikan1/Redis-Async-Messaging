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

package me.petulikan1.commons.redis.utils;

import java.security.SecureRandom;
import java.util.HashSet;

public class Utils {
    private static final char[] alphabet = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final HashSet<String> ids = new HashSet<>();

    public static String generateID() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            builder.append(Character.toUpperCase(alphabet[secureRandom.nextInt(alphabet.length)]));
        }
        String key = builder.toString();
        while (ids.contains(key)) {
            key = generateID();
        }
        ids.add(key);
        return key;
    }

    public static void validate(boolean question, String error) {
        if (question) {
            throw new NullPointerException(error);
        }
    }
}
