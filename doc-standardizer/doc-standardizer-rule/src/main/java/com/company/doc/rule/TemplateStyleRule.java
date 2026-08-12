package com.company.doc.rule;


import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateStyle;



public class TemplateStyleRule {


    public String match(
            String styleId,
            TemplateDefinition template
    ){


        for(TemplateStyle style :
                template.getStyles()){


            if(styleId.equals(
                    style.getStyleId()
            )){


                return style.getRole();

            }

        }


        return "NORMAL";

    }


}