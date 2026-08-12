package com.company.doc.template;


import lombok.Data;


/**
 * 模板样式业务映射
 */
@Data
public class TemplateStyleMapping {


    /**
     * Word styleId
     */
    private String styleId;


    /**
     * 标准角色
     *
     * TITLE_ONE
     * TITLE_TWO
     * NORMAL
     */
    private String role;


}