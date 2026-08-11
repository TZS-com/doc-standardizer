package com.company.doc.parser;


import com.company.doc.common.model.DocumentModel;


import java.io.File;



public class ParserTest {


    public static void main(String[] args)
            throws Exception {


        DocxParser parser =
                new DocxParser();



        DocumentModel model =
                parser.parse(
                        new File(
                                "D:\\test.docx"
                        )
                );


        model.getParagraphs()
                .forEach(
                        p -> {

                            System.out.println(
                                    p.getIndex()
                                            + " | "
                                            + p.getStyleName()
                                            + " | "
                                            + p.getText()
                            );

                        }
                );


    }

}