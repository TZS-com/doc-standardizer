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


def is_toc(paragraph):


    # 1. 判断样式

    styles = paragraph.xpath(
        "./w:pPr/w:pStyle/@w:val",
        namespaces=NS
    )


    for s in styles:

        if (
            s.lower().startswith("toc")
            or
            "toc" in s.lower()
        ):
            return True



    # 2. 判断域代码

    instr = paragraph.xpath(
        ".//w:instrText/text()",
        namespaces=NS
    )


    for text in instr:

        if "TOC" in text.upper():
            return True



    # 3. 判断目录文字

    texts = paragraph.xpath(
        ".//w:t/text()",
        namespaces=NS
    )


    content = "".join(texts)


    if content.strip() == "目录":
        return True


    return False


def clean_document_style(
        document_xml
):

    """
    根据outline level调整样式
    """

    tree = etree.parse(
        document_xml
    )

    # 删除正文空行


    paragraphs = tree.xpath(
        "//w:p",
        namespaces=NS
    )

    for p in paragraphs:

        if is_toc(p):
            continue

        level = get_outline_level(p)

        if level is not None:

            style = LEVEL_STYLE_MAP.get(level)

            if style:
                set_paragraph_style(
                    p,
                    style
                )

    # for p in paragraphs:
    #
    #     # 清除文字级格式
    #     clear_run_style(
    #         p
    #     )
    #
    #     level = get_outline_level(
    #         p
    #     )
    #
    #     if level is not None:
    #
    #         style = LEVEL_STYLE_MAP.get(
    #             level
    #         )
    #
    #         if style:
    #             set_paragraph_style(
    #                 p,
    #                 style
    #             )
    #
    #
    #     else:
    #
    #         set_paragraph_style(
    #             p,
    #             "Normal"
    #         )


    tree.write(
        document_xml,
        encoding="UTF-8",
        xml_declaration=True
    )

def is_empty_paragraph(paragraph):

    """
    判断段落是否为空
    """

    texts = paragraph.xpath(
        ".//w:t/text()",
        namespaces=NS
    )

    text = "".join(texts).strip()

    return text == ""



def remove_empty_paragraphs(tree):

    """
    删除正文空行
    """

    body = tree.find(
        ".//w:body",
        NS
    )

    if body is None:
        return


    paragraphs = body.findall(
        "w:p",
        NS
    )


    count = 0


    for p in paragraphs:

        if is_empty_paragraph(p):

            body.remove(p)

            count += 1


    print(
        f"删除空行: {count}"
    )


def clear_run_style(
        paragraph
):
    """
    清除文字直接格式
    """

    runs = paragraph.xpath(
        "./w:r",
        namespaces=NS
    )


    for run in runs:

        rPr = run.find(
            "w:rPr",
            NS
        )

        if rPr is not None:

            run.remove(
                rPr
            )


def clear_outline_level(
        paragraph
):

    """
    清除Word大纲等级
    """

    pPr = paragraph.find(
        "w:pPr",
        NS
    )


    if pPr is None:
        return


    outline = pPr.find(
        "w:outlineLvl",
        NS
    )


    if outline is not None:

        pPr.remove(
            outline
        )



if __name__ == "__main__":


    clean_document_style(
        "word/document.xml"
    )