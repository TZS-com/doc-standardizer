package com.company.doc.template;


import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyles;


import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;



public class WordTemplateParser
        implements TemplateParser {


    private void detectRole(
            TemplateStyle style,
            String styleId
    ){


        /*
         * 临时规则
         *
         * 后续改成配置
         */


        if("29".equals(styleId)){

            style.setRole(
                    "TITLE_ONE"
            );

            style.setHeading(true);

            style.setLevel(1);

            return;

        }



        if("33".equals(styleId)){

            style.setRole(
                    "TITLE_TWO"
            );

            style.setHeading(true);

            style.setLevel(2);

            return;

        }



        if("120".equals(styleId)){

            style.setRole(
                    "NORMAL"
            );

            return;

        }



        style.setRole(
                "NORMAL"
        );

    }

    @Override
    public TemplateDefinition parse(File file)
            throws Exception {


        TemplateDefinition definition =
                new TemplateDefinition();


        definition.setName(
                file.getName()
        );


        Map<String, TemplateStyle> styleMap =
                new LinkedHashMap<>();


        try(
                FileInputStream fis =
                        new FileInputStream(file);

                XWPFDocument document =
                        new XWPFDocument(fis)
        ){


            XWPFStyles styles =
                    document.getStyles();

            if(styles != null){

                System.out.println(
                        "style count="
                                +
                                styles.getNumberOfStyles()
                );

            }







            for(XWPFParagraph paragraph :
                    document.getParagraphs()){


                String styleId =
                        paragraph.getStyle();



                if(styleId == null
                        || styleId.trim().isEmpty()){

                    continue;

                }



                TemplateStyle templateStyle =
                        styleMap.get(styleId);



                if(templateStyle == null){


                    templateStyle =
                            new TemplateStyle();


                    templateStyle.setStyleId(
                            styleId
                    );


                    detectRole(
                            templateStyle,
                            styleId
                    );


                    styleMap.put(
                            styleId,
                            templateStyle
                    );

                }



                templateStyle.setUseCount(
                        templateStyle.getUseCount()
                                + 1
                );

                detectParagraphRole(
                        templateStyle,
                        paragraph
                );


                System.out.println(
                        "text="
                        +
                        paragraph.getText()
                                +
                                " | style="
                                +
                                styleId
                );

            }


        }



        definition.getStyles()
                .addAll(
                        styleMap.values()
                );


        return definition;

    }


    private void detectParagraphRole(
            TemplateStyle style,
            XWPFParagraph paragraph
    ){


        String text =
                paragraph.getText();



        if(text == null
                || text.trim().isEmpty()){

            return;

        }


        /*
         * 一级标题特征
         *
         * 例如：
         * 1 测试项目
         * 2 验证范围
         */
        if(text.matches(
                "^\\d+\\s+.*"
        )){


            style.setRole(
                    "TITLE_ONE"
            );

            style.setHeading(true);

            style.setLevel(1);


            return;

        }



        /*
         * 二级标题
         *
         * 例如：
         * 1.1 文件确认
         */
        if(text.matches(
                "^\\d+\\.\\d+\\s+.*"
        )){


            style.setRole(
                    "TITLE_TWO"
            );

            style.setHeading(true);

            style.setLevel(2);


            return;

        }



        /*
         * 普通正文
         */
        if(style.getRole()==null){


            style.setRole(
                    "NORMAL"
            );

        }

    }


    private void loadStyleInfo(
            XWPFDocument document,
            TemplateStyle templateStyle
    ){

    }





}