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
     * Word automatic-numbering level. Word stores level zero for a first-level
     * item, one for a second-level item, and so on.
     */
    private Integer numberingLevel;


    /** True when the automatic numbering format is a Word bullet rather than an ordered number. */
    private boolean bulletNumbering;


    /** BODY, TABLE, HEADER or FOOTER. */
    private String location = "BODY";


    private Integer tableIndex;


    private Integer rowIndex;


    private Integer cellIndex;


    /**
     * 原始对象
     */
    private Object source;

}
