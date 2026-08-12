package com.company.doc.common.model;


import com.company.doc.common.enums.DocumentLevel;
import lombok.Data;

@Data
public class ParagraphNode {


    private int index;


    private String text;


    /**
     * Word原始样式ID
     */
    private String styleId;


    /**
     * Word样式名称
     */
    private String styleName;


    /**
     * Word大纲级别
     */
    private Integer outlineLevel;


    /**
     * 标准文档级别
     */
    private DocumentLevel level;


    /**
     * 标准角色
     */
    private String role;


    /**
     * 是否编号
     */
    private boolean numbered;


    /**
     * 原始对象
     */
    private Object source;

}
