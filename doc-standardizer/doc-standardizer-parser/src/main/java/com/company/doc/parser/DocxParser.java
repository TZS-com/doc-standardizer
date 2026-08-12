package com.company.doc.parser;


import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.model.ParagraphNode;


import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;


import java.io.File;
import java.io.FileInputStream;



public class DocxParser {


    public DocumentModel parse(File file)
            throws Exception {


        DocumentModel model =
                new DocumentModel();


        try(
                FileInputStream fis =
                        new FileInputStream(file);

                XWPFDocument document =
                        new XWPFDocument(fis)
        ){


            int index = 0;


            for(XWPFParagraph paragraph :
                    document.getParagraphs()){


                ParagraphNode node =
                        new ParagraphNode();



                node.setIndex(index++);



                node.setText(
                        paragraph.getText()
                );

                node.setStyleId(
                        paragraph.getStyle()
                );



                XWPFStyle style = document.getStyles() == null ? null : document.getStyles().getStyle(paragraph.getStyle());
                node.setStyleName(style == null ? null : style.getName());



                /*
                 * 获取Word大纲级别
                 */
                if(
                        paragraph.getCTP()
                                .getPPr() != null
                                &&
                                paragraph.getCTP()
                                        .getPPr()
                                        .getOutlineLvl() != null
                ){

                    node.setOutlineLevel(
                            paragraph.getCTP()
                                    .getPPr()
                                    .getOutlineLvl()
                                    .getVal()
                                    .intValue()
                    );

                }



                /*
                 * 判断是否自动编号
                 */
                node.setNumbered(
                        paragraph.getCTP()
                                .getPPr() != null
                                &&
                                paragraph.getCTP()
                                        .getPPr()
                                        .getNumPr() != null
                );



                node.setSource(
                        paragraph
                );



                model.getParagraphs()
                        .add(node);

            }

        }


        return model;

    }

}
