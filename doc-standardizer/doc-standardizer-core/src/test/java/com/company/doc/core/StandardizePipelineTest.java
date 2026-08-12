package com.company.doc.core;


import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.model.ParagraphNode;

import org.junit.jupiter.api.Test;


import java.io.File;



public class StandardizePipelineTest {


    @Test
    public void testStandardize()
            throws Exception {



        /*
         * 公司标准模板
         */
        File template =
                new File(
                        "D:\\template.docx"
                );



        /*
         * 待处理文档
         */
        File input =
                new File(
                        "D:\\test.docx"
                );



        DocumentProcessor processor =
                new DocumentProcessor(
                        template
                );



        DocumentModel model =
                processor.process(
                        input
                );



        System.out.println(
                "==========识别结果=========="
        );







        for(ParagraphNode node :
                model.getParagraphs()){


            System.out.println(

                    "index="
                            +
                            node.getIndex()

                            +

                            " | text="
                            +
                            node.getText()

                            +

                            " | style="
                            +
                            node.getStyleId()

                            +

                            " | role="
                            +
                            node.getRole()

                            +

                            " | level="
                            +
                            node.getLevel()

            );


        }


    }


}