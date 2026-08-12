package com.company.doc.core;


import com.company.doc.common.model.DocumentModel;
import com.company.doc.template.TemplateDefinition;
import com.company.doc.template.TemplateLoader;


import java.io.File;



public class DocumentProcessor {


    private final StandardizePipeline pipeline;



    public DocumentProcessor(
            File templateFile
    )
            throws Exception {


        TemplateLoader loader =
                new TemplateLoader();



        TemplateDefinition template =
                loader.load(
                        templateFile
                );



        this.pipeline =
                new StandardizePipeline(
                        template
                );

    }



    public DocumentModel process(
            File file
    )
            throws Exception {


        return pipeline.execute(
                file
        );

    }


}