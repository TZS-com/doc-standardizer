package com.company.doc.template;


import lombok.Data;


/**
 * Word模板样式定义
 */
@Data
public class TemplateStyle {


    /**
     * POI styleId
     */
    private String styleId;


    /**
     * Word显示名称
     */
    private String styleName;


    /**
     * 样式类型
     *
     * paragraph
     * character
     */
    private String type;


    /**
     * 是否标题
     */
    private boolean heading;


    /**
     * 标题等级
     */
    private Integer level;


}