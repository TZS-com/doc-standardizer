package com.company.doc.template;


import java.io.File;


public class TemplateLoader {


    private final WordTemplateParser parser =
            new WordTemplateParser();



    public TemplateDefinition load(File file)
            throws Exception {


        return parser.parse(file);

    }

}