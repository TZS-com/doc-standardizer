package com.company.doc.rule;

import java.util.regex.Pattern;

/** Detects common manually typed Chinese and Arabic heading numbers. */
public class NumberHeadingRule {
    private static final Pattern LEVEL_ONE = Pattern.compile("^\\s*(?:第\\s*)?\\d+(?:\\s*[、.．])?\\s+\\S.*$");
    private static final Pattern LEVEL_TWO = Pattern.compile("^\\s*\\d+[.．]\\d+(?:\\s*[、.．])?\\s+\\S.*$");
    private static final Pattern LEVEL_THREE = Pattern.compile("^\\s*\\d+(?:[.．]\\d+){2,}(?:\\s*[、.．])?\\s+\\S.*$");
    private static final Pattern CHINESE_ONE = Pattern.compile("^\\s*第[一二三四五六七八九十百千万零〇]+[章节部分]\\s*.+$");
    private static final Pattern CHINESE_TWO = Pattern.compile("^\\s*[（(][一二三四五六七八九十百千万零〇]+[）)]\\s*.+$");
    private static final Pattern CHINESE_THREE = Pattern.compile("^\\s*[一二三四五六七八九十百千万零〇]+[、.]\\s*.+$");

    public int matchLevel(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        String value = text.trim();
        if (LEVEL_THREE.matcher(value).matches() || CHINESE_THREE.matcher(value).matches()) return 3;
        if (LEVEL_TWO.matcher(value).matches() || CHINESE_TWO.matcher(value).matches()) return 2;
        if (LEVEL_ONE.matcher(value).matches() || CHINESE_ONE.matcher(value).matches()) return 1;
        return 0;
    }
}
