from zipfile import ZipFile, ZIP_DEFLATED
from lxml import etree
import json


from core.style.style_mapper import StyleMapper



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

    with ZipFile(docx_file, "r") as z:

        xml = z.read(
            "word/document.xml"
        )

    return etree.fromstring(xml)



def get_paragraphs(root):
    """
    获取正文段落

    按document.xml顺序
    """

    return root.xpath(
        ".//w:body/w:p",
        namespaces=NS
    )



def set_paragraph_style(
        paragraph,
        style_id
):
    """
    强制设置段落样式
    """

    pPr = paragraph.find(
        "w:pPr",
        namespaces=NS
    )


    if pPr is None:

        pPr = etree.Element(
            W+"pPr"
        )

        paragraph.insert(
            0,
            pPr
        )


    # 删除旧pStyle

    old_style = pPr.find(
        "w:pStyle",
        namespaces=NS
    )

    if old_style is not None:

        pPr.remove(
            old_style
        )


    # 新增模板style

    pStyle = etree.Element(
        W+"pStyle"
    )

    pStyle.set(
        W+"val",
        style_id
    )


    pPr.insert(
        0,
        pStyle
    )
    """
    设置段落样式

    保留原pPr内容
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


    # 查找已有pStyle

    old_style = pPr.find(
        "w:pStyle",
        namespaces=NS
    )


    if old_style is not None:

        old_style.set(
            W + "val",
            style_id
        )


    else:

        pStyle = etree.Element(
            W + "pStyle"
        )

        pStyle.set(
            W + "val",
            style_id
        )


        # pStyle应该放在pPr最前

        pPr.insert(
            0,
            pStyle
        )



def write_paragraph_styles(
        source_docx,
        analysis_json,
        template_docx,
        mapping_file,
        output_docx
):
    """
    根据level设置段落样式

    """

    # 读取分析结果

    with open(
            analysis_json,
            "r",
            encoding="utf-8"
    ) as f:

        plans = json.load(f)



    mapper = StyleMapper(
        template_docx,
        mapping_file
    )


    # 读取document.xml

    document_root = read_document_xml(
        source_docx
    )


    paragraphs = get_paragraphs(
        document_root
    )



    changed = 0



    for item in plans:


        index = item.get(
            "index"
        )


        level = item.get(
            "level"
        )


        if index is None:
            continue


        if index > len(paragraphs):

            continue



        paragraph = paragraphs[
            index - 1
        ]



        style_info = mapper.get_style(
            level
        )
        print(
            index,
            paragraph.xpath(
                "string(.)",
                namespaces=NS
            )[:30],
            "level=",
            level,
            "style=",
            style_info["style_id"]
        )


        set_paragraph_style(
            paragraph,
            style_info["style_id"]
        )


        changed += 1



    new_document_xml = etree.tostring(
        document_root,
        xml_declaration=True,
        encoding="UTF-8",
        standalone=True
    )



    # 写回docx

    with ZipFile(
            source_docx,
            "r"
    ) as zin:


        with ZipFile(
                output_docx,
                "w",
                ZIP_DEFLATED
        ) as zout:


            for item in zin.infolist():

                data = zin.read(
                    item.filename
                )


                if (
                    item.filename
                    ==
                    "word/document.xml"
                ):

                    data = new_document_xml



                zout.writestr(
                    item,
                    data
                )



    print(
        f"段落样式处理完成，共修改 {changed} 个段落"
    )



if __name__ == "__main__":


    write_paragraph_styles(
        source_docx=
        "output/style_import.docx",

        analysis_json=
        "output/numbering_analysis.json",

        template_docx=
        "template/standard.docx",

        mapping_file=
        "config/outline_style_mapping.json",

        output_docx=
        "output/style_apply.docx"
    )