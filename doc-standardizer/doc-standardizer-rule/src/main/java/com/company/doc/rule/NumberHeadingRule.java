package com.company.doc.rule;


import java.util.regex.Pattern;


/**
 * 标题编号识别规则
 *
 * 支持:
 * 1 xxx
 * 1.1 xxx
 * 1.1.1 xxx
 */
public class NumberHeadingRule {


    /**
     * 一级标题
     * 示例:
     * 1 项目概述
     */
    private static final Pattern LEVEL_ONE =
            Pattern.compile(
                    "^\\s*\\d+\\s+.+"
            );


    /**
     * 二级标题
     * 示例:
     * 1.1 项目背景
     */
    private static final Pattern LEVEL_TWO =
            Pattern.compile(
                    "^\\s*\\d+\\.\\d+\\s+.+"
            );


    /**
     * 三级标题
     * 示例:
     * 1.1.1 建设目标
     */
    private static final Pattern LEVEL_THREE =
            Pattern.compile(
                    "^\\s*\\d+\\.\\d+\\.\\d+\\s+.+"
            );



    /**
     * 返回标题等级
     *
     * @return
     * 0 普通文本
     * 1 一级标题
     * 2 二级标题
     * 3 三级标题
     */
    public int matchLevel(String text){


        if(text == null ||
                text.trim().isEmpty()){

            return 0;

        }


        text = text.trim();


        // 注意顺序
        // 三级必须最先判断

        if(LEVEL_THREE.matcher(text).matches()){

            return 3;

        }


        if(LEVEL_TWO.matcher(text).matches()){

            return 2;

        }


        if(LEVEL_ONE.matcher(text).matches()){

            return 1;

        }


        return 0;

    }

}