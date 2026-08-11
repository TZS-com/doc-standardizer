package com.company.doc.template;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;



@Data
public class TemplateDefinition {


    /**
     * 模板名称
     */
    private String name;



    /**
     * 样式集合
     */
    private List<TemplateStyle> styles =
            new ArrayList<>();


}