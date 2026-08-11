package com.company.doc.template;


import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;



public class WordStyleReader {


    public void read(String file)
            throws Exception {



        try(
                OPCPackage pkg =
                        OPCPackage.open(file)
        ){


            PackagePart part =
                    pkg.getPartsByName(
                                    org.apache.poi.openxml4j.opc.PackagingURIHelper
                                            .createPartName(
                                                    "/word/styles.xml"
                                            )
                            )
                            .get(0);



            try(
                    InputStream is =
                            part.getInputStream()
            ){


                Document doc =
                        DocumentBuilderFactory
                                .newInstance()
                                .newDocumentBuilder()
                                .parse(is);



                NodeList styles =
                        doc.getElementsByTagName(
                                "w:style"
                        );



                for(int i=0;i<styles.getLength();i++){


                    System.out.println(
                            styles.item(i)
                                    .getAttributes()
                    );

                }

            }


        }


    }


}