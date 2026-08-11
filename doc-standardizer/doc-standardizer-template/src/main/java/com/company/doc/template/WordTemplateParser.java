package com.company.doc.template;


import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFStyles;

import java.io.File;
import java.io.FileInputStream;



public class WordTemplateParser
        implements TemplateParser {


    @Override
    public TemplateDefinition parse(File file)
            throws Exception {


        TemplateDefinition definition =
                new TemplateDefinition();


        definition.setName(
                file.getName()
        );


        try(
                FileInputStream fis =
                        new FileInputStream(file);

                XWPFDocument document =
                        new XWPFDocument(fis)
        ){


            XWPFStyles styles =
                    document.getStyles();


            if(styles == null){

                return definition;

            }


            System.out.println(
                    styles.getCTStyles()
            );


        }


        return definition;

    }

}