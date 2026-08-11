package com.company.doc.core;


import com.company.doc.common.model.DocumentModel;

import java.io.File;



public class DocumentProcessor {


    private final StandardizePipeline pipeline =
            new StandardizePipeline();



    public DocumentModel process(File file)
            throws Exception {


        return pipeline.execute(file);


    }


}