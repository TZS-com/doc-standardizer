from lxml import etree


NS = {
    "w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
}


W = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"


# 大纲级别对应样式
LEVEL_STYLE_MAP = {

    0: "Heading1",
    1: "Heading2",
    2: "Heading3",
    3: "Heading4",
    4: "Heading5",
    5: "Heading6"

}



def set_paragraph_style(
        paragraph,
        style_name
):

    """
    设置段落pStyle
    """

    pPr = paragraph.find(
        "w:pPr",
        NS
    )


    if pPr is None:

        pPr = etree.Element(
            W + "pPr"
        )

        paragraph.insert(
            0,
            pPr
        )


    pStyle = pPr.find(
        "w:pStyle",
        NS
    )


    if pStyle is None:

        pStyle = etree.Element(
            W + "pStyle"
        )

        pPr.insert(
            0,
            pStyle
        )


    pStyle.set(
        W + "val",
        style_name
    )




def get_outline_level(
        paragraph
):

    """
    获取Word大纲等级

    返回:
        0,1,2...
        None
    """


    level = paragraph.xpath(
        "./w:pPr/w:outlineLvl/@w:val",
        namespaces=NS
    )


    if level:

        return int(level[0])


    return None




def clean_document_style(
        document_xml
):

    """
    根据outline level调整样式
    """

    tree = etree.parse(
        document_xml
    )


    paragraphs = tree.xpath(
        "//w:p",
        namespaces=NS
    )


    for p in paragraphs:


        level = get_outline_level(
            p
        )


        if level is not None:


            style = LEVEL_STYLE_MAP.get(
                level
            )


            if style:

                set_paragraph_style(
                    p,
                    style
                )


        else:

            # 无大纲等级认为正文

            set_paragraph_style(
                p,
                "Normal"
            )



    tree.write(
        document_xml,
        encoding="UTF-8",
        xml_declaration=True
    )



if __name__ == "__main__":


    clean_document_style(
        "word/document.xml"
    )