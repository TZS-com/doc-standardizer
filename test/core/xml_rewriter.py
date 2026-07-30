from lxml import etree


NS={
"w":
"http://schemas.openxmlformats.org/wordprocessingml/2006/main"
}



def replace_numbering(xml_path, indexes):


    tree=etree.parse(xml_path)


    paragraphs=tree.xpath(
        "//w:body/w:p",
        namespaces=NS
    )


    for i in indexes:


        p=paragraphs[i]


        pPr=p.find(
            "w:pPr",
            NS
        )


        if pPr is None:

            pPr=etree.SubElement(
                p,
                "{%s}pPr"%NS["w"]
            )


        numPr=pPr.find(
            "w:numPr",
            NS
        )


        if numPr is None:

            numPr=etree.SubElement(
                pPr,
                "{%s}numPr"%NS["w"]
            )


        numId=numPr.find(
            "w:numId",
            NS
        )


        if numId is None:

            numId=etree.SubElement(
                numPr,
                "{%s}numId"%NS["w"]
            )


        numId.set(
            "{%s}val"%NS["w"],
            "999"
        )



    tree.write(
        xml_path,
        encoding="UTF-8",
        xml_declaration=True
    )