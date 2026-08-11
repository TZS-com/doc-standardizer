package com.company.doc.core;


import com.company.doc.common.model.DocumentModel;


import java.io.File;



public class CoreTest {


    public static void main(String[] args)
            throws Exception {


        DocumentProcessor processor =
                new DocumentProcessor();



        DocumentModel model =
                processor.process(
                        new File(
                                "D:\\test.docx"
                        )
                );



        model.getParagraphs()
                .forEach(
                        paragraph -> {

                            System.out.println(
                                    paragraph.getIndex()
                                            +
                                            " | style="
                                            +
                                            paragraph.getStyleName()
                                            +
                                            " | outline="
                                            +
                                            paragraph.getOutlineLevel()
                                            +
                                            " | numbered="
                                            +
                                            paragraph.isNumbered()
                                            +
                                            " | "
                                            +
                                            paragraph.getText()
                            );

                        }
                );


    }

}