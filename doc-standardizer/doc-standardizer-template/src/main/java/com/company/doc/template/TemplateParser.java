package com.company.doc.template;


import java.io.File;



public interface TemplateParser {


    TemplateDefinition parse(File file)
            throws Exception;


}