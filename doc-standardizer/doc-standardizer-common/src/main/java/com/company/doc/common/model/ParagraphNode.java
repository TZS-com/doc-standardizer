package com.company.doc.common.model;


import com.company.doc.common.enums.DocumentLevel;

import lombok.Data;


/**
 * Word段落模型
 */
@Data
public class ParagraphNode {


    /**
     * 段落文本
     */
    private String text;

    /**
     * 段落序号
     */
    private int index;


    /**
     * Word原始样式名称
     */
    private String styleName;


    /**
     * Word大纲级别
     */
    private Integer outlineLevel;

    /**
     * 是否存在自动编号
     */
    private boolean numbered;

    /**
     * 是否目录内容
     */
    private boolean toc;


    /**
     * 是否疑似标题
     */
    private boolean headingCandidate;




    /**
     * 标准化后的标题等级
     *
     * 由 HeadingDetector 分析生成
     */
    private DocumentLevel level =
            DocumentLevel.NORMAL;






    /**
     * 原始Word段落对象
     */
    private Object source;


}