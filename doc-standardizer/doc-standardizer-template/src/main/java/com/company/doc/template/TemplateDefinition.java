package com.company.doc.template;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;



@Data
public class TemplateDefinition {


    private String name;


    private List<TemplateStyle> styles =
            new ArrayList<>();


    public TemplateStyle findStyle(
            String styleId
    ){

        for(TemplateStyle style :
                styles){

            if(styleId.equals(
                    style.getStyleId()
            )){

                return style;

            }

        }

        return null;

    }

    public TemplateStyle findStyleByRole(String role) {
        for (TemplateStyle style : styles) {
            if (role.equals(style.getRole())) {
                return style;
            }
        }
        return null;
    }

}
