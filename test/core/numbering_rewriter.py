from lxml import etree


NS = {

    "w":
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"

}



def rewrite_document(
        xml_file
):


    tree = etree.parse(
        xml_file
    )


    paragraphs = tree.xpath(

        "//w:body/w:p",

        namespaces=NS

    )


    count=0


    for p in paragraphs:


        num_id = p.xpath(

            "./w:pPr/w:numPr/w:numId/@w:val",

            namespaces=NS

        )


        if not num_id:

            continue


        numId = p.xpath(

            "./w:pPr/w:numPr/w:numId",

            namespaces=NS

        )[0]


        numId.set(

            "{%s}val"
            %
            NS["w"],

            "999"

        )


        count+=1



    tree.write(

        xml_file,

        encoding="UTF-8",

        xml_declaration=True

    )


    return count