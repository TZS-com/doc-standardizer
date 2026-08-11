package com.company.doc.template;


import java.io.File;



public class TemplateTest {


    public static void main(String[] args)
            throws Exception {


        WordTemplateParser parser =
                new WordTemplateParser();



        TemplateDefinition definition =
                parser.parse(
                        new File(
                                "D:\\template.docx"
                        )
                );



        definition.getStyles()
                .forEach(
                        s -> {

                            System.out.println(
                                    s.getStyleId()
                                            +
                                            " | "
                                            +
                                            s.getStyleName()
                                            +
                                            " | heading="
                                            +
                                            s.isHeading()
                                            +
                                            " | level="
                                            +
                                            s.getLevel()
                            );

                        }
                );


    }

}