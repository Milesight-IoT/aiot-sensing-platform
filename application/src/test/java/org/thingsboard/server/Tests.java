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
package org.thingsboard.server;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Tests {

    @Test
    public void test() {
        int i = 1 / 0;
    }

    // 时间转换
    @Test
    public void dateTest() {
        Date date = new Date(1675389600000L); // 1675389600000L - 北京时间2023-02-03 10:00:00
        System.out.println(date);

        String timeZone = "GMT+8:00"; // 东八区
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        System.out.println(simpleDateFormat.format(date));
    }


    // 找出字符串中出现频率最高的字符
    @Test
    public void findRepeatChar() {
        String str = "xu_yan_chuan";
        Map<Character, Integer> map = new HashMap<>();
        char[] chars = str.toCharArray();
        for (char aChar : chars) {
            System.out.println(aChar);
            if (map.containsKey(aChar)) {
                map.put(aChar, map.get(aChar) + 1);
                continue;
            }
            map.put(aChar, 1);
        }
        System.out.println(map);

        Integer max = 0;
        Character c = null;
//        Set<Character> set = new HashSet<>();
        for (Map.Entry<Character, Integer> characterIntegerEntry : map.entrySet()) {
            if (characterIntegerEntry.getValue() > max) {
                max = characterIntegerEntry.getValue();
//                set.add(characterIntegerEntry.getKey());
                c = characterIntegerEntry.getKey();
            }
        }
        System.out.println(max);
        System.out.println(c);
//        System.out.println(set);
    }

    @Test
    public void generateSNCheckBit() {
        String[] snArr = {
                "26402304ekt",
                "264023040nz",
                "26402304m4h",
                "264023047r0",
                "26402304gxs",
                "26402304c4l",
                "2640230497y",
                "264023048i4",
                "26402304jhm",
                "26402304mh1",
                "264023047fr",
                "26402304owx",
                "26402304gwe",
                "26402304cxq",
                "26402304mjo",
                "264023040v2",
                "264023041nm",
                "264023043wr",
                "26402304h90",
                "2640230406c",
                "264023045ku",
                "2640230429n",
                "264023041i9"
        };

        for (String sn : snArr) {
//            String sn = "264023041i9";
            sn = sn.toLowerCase();

            String encryptString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            String decryptString = "29*xy+ab57c4d3efg6hijk81lm@%^rz!n.-#";
            String keyForCode = "YEASTAR";

            int ADD_NUMBER = 1;
            int SUB_NUMBER = 2;

            // ******************************************

            char bit;
            List<Character> buf = new ArrayList<>();
            int i = 0;
            int j = 0;
            int checkBit = 0x12;

            for (i = 0; i < sn.length(); i++) {
                for (j = 0; j < encryptString.length(); j++) {
                    if (sn.charAt(i) == encryptString.charAt(j)) {
                        buf.add(decryptString.charAt(j));
                        break;
                    }
                }
                if (j == encryptString.length()) {
                    buf.add(sn.charAt(i));
                }
            }
            System.out.println(111);
            System.out.println(buf);

            for (i = 0; i < keyForCode.length(); i++) {
                for (j = 0; j < encryptString.length(); j++) {
                    if (keyForCode.charAt(i) == encryptString.charAt(j)) {
                        buf.add(decryptString.charAt(j));
                        break;
                    }
                }
                if (j == encryptString.length()) {
                    buf.add(keyForCode.charAt(i));
                }
            }
            System.out.println(222);
            System.out.println(buf);

            for (i = 0; i < buf.size(); i++) {
//                boolean addBoolean = i % ADD_NUMBER > 0;
                boolean subBoolean = i % SUB_NUMBER > 0;
                if (buf.get(i) != 0) {
                    checkBit += (int) buf.get(i);
                } else if (buf.get(i) != 0 && !subBoolean) {
                    checkBit -= (int) buf.get(i);
                }
            }
            System.out.println(333);
            System.out.println(checkBit);

            bit = 0;
            bit += (char) (checkBit & 0xFF);
            bit += (char) ((checkBit >> 8) & 0xFF);
            bit += (char) ((checkBit >> 16) & 0xFF);
            bit += (char) ((checkBit >> 24) & 0xFF);
            bit = (char) (((int) bit) % 10 + 0x30);
            System.out.println(444);
            System.out.println("**************************");
            System.out.println("sn:" + sn);
            System.out.println(bit);
        }
    }
}
