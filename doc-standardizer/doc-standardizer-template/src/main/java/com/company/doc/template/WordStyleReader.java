package com.company.doc.template;


import java.io.File;


public class WordStyleReader {


    public void read(File file)
            throws Exception {


        System.out.println(
                "读取模板:"
                        +
                        file.getName()
        );

    }

}