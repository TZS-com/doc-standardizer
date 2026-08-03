from zipfile import ZipFile, ZIP_DEFLATED
from lxml import etree


from style_mapper import (
    get_heading_style
)


NS = {

    "w":
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"

}


W = (
    "{http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main}"
)



def read_document_xml(docx_file):
    """
    读取document.xml
    """

    with ZipFile(
            docx_file,
            "r"
    ) as z:

        xml = z.read(
            "word/document.xml"
        )


    return etree.fromstring(xml)





def get_paragraph_text(p):
    """
    获取段落文本
    """

    texts = p.xpath(
        ".//w:t/text()",
        namespaces=NS
    )


    return "".join(
        texts
    ).strip()





def update_paragraph_style(
        paragraph,
        level
):
    """
    根据level设置Heading样式

    level:
        0 -> Heading1
        1 -> Heading2
        2 -> Heading3
    """


    pPr = paragraph.find(
        "w:pPr",
        namespaces=NS
    )


    if pPr is None:

        pPr = etree.Element(
            W + "pPr"
        )

        paragraph.insert(
            0,
            pPr
        )



    # 删除原来的pStyle

    old_style = pPr.find(
        "w:pStyle",
        namespaces=NS
    )


    if old_style is not None:

        pPr.remove(
            old_style
        )



    style_name = get_heading_style(
        level
    )



    new_style = etree.Element(
        W + "pStyle"
    )


    new_style.set(
        W + "val",
        style_name
    )


    pPr.insert(
        0,
        new_style
    )





def update_heading_style(
        docx_file,
        level_plan,
        output_file
):
    """
    根据level更新标题样式

    不修改:
        - 文本
        - 编号
        - numId
        - 表格

    只修改:
        pStyle
    """



    tree = read_document_xml(
        docx_file
    )



    paragraphs = tree.xpath(
        ".//w:body/w:p",
        namespaces=NS
    )



    # 建立文本和level关系

    level_map = {}


    for item in level_plan:

        text = item[
            "text"
        ].strip()


        level_map[text] = item[
            "level"
        ]



    update_count = 0



    for paragraph in paragraphs:


        text = get_paragraph_text(
            paragraph
        )


        if text in level_map:


            update_paragraph_style(

                paragraph,

                level_map[text]

            )


            update_count += 1




    document_xml = etree.tostring(

        tree,

        encoding="UTF-8",

        xml_declaration=True,

        standalone=True

    )




    with ZipFile(
            docx_file,
            "r"
    ) as zin:


        with ZipFile(
                output_file,
                "w",
                ZIP_DEFLATED
        ) as zout:



            for item in zin.infolist():


                data = zin.read(
                    item.filename
                )


                if item.filename == "word/document.xml":

                    data = document_xml



                zout.writestr(
                    item,
                    data
                )



    print(
        "标题样式更新完成"
    )

    print(
        f"更新段落数量:{update_count}"
    )