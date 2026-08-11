package com.company.doc.rule;


import com.company.doc.common.model.DocumentModel;
import com.company.doc.common.model.ParagraphNode;
import com.company.doc.common.enums.DocumentLevel;


public class HeadingDetector {


    private final NumberHeadingRule numberRule =
            new NumberHeadingRule();



    public void detect(DocumentModel model){


        for(ParagraphNode paragraph :
                model.getParagraphs()){


            int level =
                    numberRule.matchLevel(
                            paragraph.getText()
                    );


            switch(level){


                case 1:

                    paragraph.setLevel(
                            DocumentLevel.TITLE_ONE
                    );

                    break;


                case 2:

                    paragraph.setLevel(
                            DocumentLevel.TITLE_TWO
                    );

                    break;


                case 3:

                    paragraph.setLevel(
                            DocumentLevel.TITLE_THREE
                    );

                    break;


                default:

                    paragraph.setLevel(
                            DocumentLevel.NORMAL
                    );

            }

        }

    }

}