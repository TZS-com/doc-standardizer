from lxml import etree


NS={
"w":
"http://schemas.openxmlformats.org/"
"wordprocessingml/2006/main"
}



def replace_number(
        paragraph
):


    pPr = paragraph.find(
        "w:pPr",
        NS
    )


    if pPr is None:

        return


    numPr=pPr.find(
        "w:numPr",
        NS
    )


    if numPr is None:

        return



    numId=numPr.find(
        "w:numId",
        NS
    )


    if numId is None:

        numId=etree.SubElement(
            numPr,
            "{%s}numId"
            %
            NS["w"]
        )


    numId.set(
        "{%s}val"
        %
        NS["w"],
        "999"
    )