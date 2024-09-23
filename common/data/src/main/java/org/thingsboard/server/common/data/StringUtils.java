/*
 * Copyright © 2016-2023 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.thingsboard.server.common.data;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.repeat;

public class StringUtils {
    public static final SecureRandom RANDOM = new SecureRandom();

    public static final String EMPTY = "";
    public static final String COMMA = ",";
    public static final String HORIZONTAL_BAR = "-";
    public static final String SEMICOLON = ":";

    public static final int INDEX_NOT_FOUND = -1;

    public static boolean isEmpty(String source) {
        return source == null || source.isEmpty();
    }

    public static boolean isBlank(String source) {
        return source == null || source.isEmpty() || source.trim().isEmpty();
    }

    public static boolean isNotEmpty(String source) {
        return source != null && !source.isEmpty();
    }

    public static boolean isNotBlank(String source) {
        return source != null && !source.isEmpty() && !source.trim().isEmpty();
    }

    public static String notBlankOrDefault(String src, String def) {
        return isNotBlank(src) ? src : def;
    }

    public static String removeStart(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.startsWith(remove)) {
            return str.substring(remove.length());
        }
        return str;
    }

    public static String substringBefore(final String str, final String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String substringBetween(final String str, final String open, final String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        final int start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            final int end = str.indexOf(close, start + open.length());
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    public static String obfuscate(String input, int seenMargin, char obfuscationChar,
                                   int startIndexInclusive, int endIndexExclusive) {

        String part = input.substring(startIndexInclusive, endIndexExclusive);
        String obfuscatedPart;
        if (part.length() <= seenMargin * 2) {
            obfuscatedPart = repeat(obfuscationChar, part.length());
        } else {
            obfuscatedPart = part.substring(0, seenMargin)
                    + repeat(obfuscationChar, part.length() - seenMargin * 2)
                    + part.substring(part.length() - seenMargin);
        }
        return input.substring(0, startIndexInclusive) + obfuscatedPart + input.substring(endIndexExclusive);
    }

    public static Iterable<String> split(String value, int maxPartSize) {
        return Splitter.fixedLength(maxPartSize).split(value);
    }

    public static List<String> split(String value, String separator) {
        if (isBlank(value)) {
            return new ArrayList<>();
        }
        return Arrays.asList(value.split(separator));
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    public static String join(String[] keyArray, String lwm2mSeparatorPath) {
        return org.apache.commons.lang3.StringUtils.join(keyArray, lwm2mSeparatorPath);
    }

    public static String trimToNull(String toString) {
        return org.apache.commons.lang3.StringUtils.trimToNull(toString);
    }

    public static boolean isNoneEmpty(String str) {
        return org.apache.commons.lang3.StringUtils.isNoneEmpty(str);
    }

    public static boolean endsWith(String str, String suffix) {
        return org.apache.commons.lang3.StringUtils.endsWith(str, suffix);
    }

    public static boolean hasLength(String str) {
        return org.springframework.util.StringUtils.hasLength(str);
    }

    public static boolean isNoneBlank(String... str) {
        return org.apache.commons.lang3.StringUtils.isNoneBlank(str);
    }

    public static boolean hasText(String str) {
        return org.springframework.util.StringUtils.hasText(str);
    }

    public static String defaultString(String s, String defaultValue) {
        return org.apache.commons.lang3.StringUtils.defaultString(s, defaultValue);
    }

    public static boolean isNumeric(String str) {
        return org.apache.commons.lang3.StringUtils.isNumeric(str);
    }

    public static boolean equals(String str1, String str2) {
        return org.apache.commons.lang3.StringUtils.equals(str1, str2);
    }

    public static boolean equalsAny(String string, String... otherStrings) {
        for (String otherString : otherStrings) {
            if (equals(string, otherString)) {
                return true;
            }
        }
        return false;
    }

    public static String substringAfterLast(String str, String sep) {
        return org.apache.commons.lang3.StringUtils.substringAfterLast(str, sep);
    }

    public static boolean containedByAny(String searchString, String... strings) {
        if (searchString == null) return false;
        for (String string : strings) {
            if (string != null && string.contains(searchString)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
        return org.apache.commons.lang3.StringUtils.contains(seq, searchSeq);
    }

    public static String randomNumeric(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    public static String random(int length) {
        return RandomStringUtils.random(length);
    }

    public static String random(int length, String chars) {
        return RandomStringUtils.random(length, chars);
    }

    public static String randomAlphanumeric(int count) {
        return RandomStringUtils.randomAlphanumeric(count);
    }

    public static String randomAlphabetic(int count) {
        return RandomStringUtils.randomAlphabetic(count);
    }

    public static String generateSafeToken(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

}
