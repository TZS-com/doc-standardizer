package com.company.doc.template;


import org.junit.jupiter.api.Test;


import java.io.File;



public class WordTemplateParserTest {


    @Test
    public void testParseTemplate()
            throws Exception {


        File file =
                new File(
                        "D:\\template.docx"
                );


        WordTemplateParser parser =
                new WordTemplateParser();


        TemplateDefinition definition =
                parser.parse(file);



        System.out.println(
                "==========模板样式=========="
        );


        for(TemplateStyle style :
                definition.getStyles()){


            System.out.println(

                    "styleId="
                            +
                            style.getStyleId()

                            +

                            " | name="
                            +
                            style.getStyleName()

                            +

                            " | role="
                            +
                            style.getRole()

                            +

                            " | level="
                            +
                            style.getLevel()

                            +

                            " | count="
                            +
                            style.getUseCount()

            );

        }


    }


}