package com.company.doc.rule;


import com.company.doc.common.model.ParagraphNode;
import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateStyle;


public class TemplateMatchService {


    public void match(
            ParagraphNode node,
            TemplateDefinition template
    ){


        if(node.getStyleId()==null){

            node.setRole(
                    "NORMAL"
            );

            return;

        }


        for(TemplateStyle style :
                template.getStyles()){


            if(node.getStyleId()
                    .equals(style.getStyleId())){


                node.setRole(
                        style.getRole()
                );


                return;

            }

        }


        node.setRole(
                "NORMAL"
        );

    }

}