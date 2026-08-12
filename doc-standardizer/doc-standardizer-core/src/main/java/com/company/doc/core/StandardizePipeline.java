package com.company.doc.core;


import com.company.doc.common.model.DocumentModel;
import com.company.doc.parser.DocxParser;
import com.company.doc.rule.HeadingDetector;
import com.company.doc.template.TemplateDefinition;

import java.io.File;


/**
 * 文档标准化流程
 */
public class StandardizePipeline {


    private final DocxParser parser;

    private final HeadingDetector detector;

    private final TemplateDefinition template;



    public StandardizePipeline(
            TemplateDefinition template
    ){

        this.parser = new DocxParser();

        this.detector = new HeadingDetector();

        this.template = template;

    }



    /**
     * 执行标准化流程
     *
     * @param input 输入文件
     */
    public DocumentModel execute(File input)
            throws Exception {


        /*
         * 第一步:
         * Word解析
         */
        DocumentModel model =
                parser.parse(input);



        /*
         * 第二步:
         * 标题识别
         */
        detector.detect(
                model,
                template
        );



        return model;

    }


}