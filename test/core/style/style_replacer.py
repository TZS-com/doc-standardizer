from zipfile import ZipFile, ZIP_DEFLATED
from lxml import etree


NS = {
    "w":
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"
}


W = (
    "{http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main}"
)



def read_styles_xml(docx_file):
    """
    读取docx中的styles.xml
    """

    with ZipFile(docx_file, "r") as z:

        xml = z.read(
            "word/styles.xml"
        )

    return etree.fromstring(xml)



def find_style_by_id(
        styles_root,
        style_id
):
    """
    根据styleId查找样式
    """

    styles = styles_root.xpath(
        "./w:style",
        namespaces=NS
    )


    for style in styles:

        sid = style.get(
            W + "styleId"
        )

        if sid == style_id:

            return style


    return None



def remove_style(
        styles_root,
        style
):
    """
    删除样式节点
    """

    parent = style.getparent()

    if parent is not None:

        parent.remove(
            style
        )



def replace_doc_defaults(
        source_root,
        template_root
):
    """
    替换docDefaults
    """

    source_default = source_root.find(
        "w:docDefaults",
        namespaces=NS
    )


    template_default = template_root.find(
        "w:docDefaults",
        namespaces=NS
    )


    if template_default is None:

        return


    if source_default is not None:

        source_root.remove(
            source_default
        )


    # 插入到最前

    source_root.insert(
        0,
        template_default
    )



def replace_styles(
        source_root,
        template_root
):
    """
    模板样式覆盖原文样式
    """

    replace_count = 0
    add_count = 0



    template_styles = template_root.xpath(
        "./w:style",
        namespaces=NS
    )



    for template_style in template_styles:


        style_id = template_style.get(
            W + "styleId"
        )


        if not style_id:

            continue



        old_style = find_style_by_id(
            source_root,
            style_id
        )


        if old_style is not None:

            remove_style(
                source_root,
                old_style
            )

            replace_count += 1


        else:

            add_count += 1



        # 插入模板样式

        source_root.append(
            template_style
        )



    return (
        replace_count,
        add_count
    )



def replace_styles_xml(
        source_docx,
        template_docx,
        output_docx
):
    """
    将模板样式库替换到原文

    """

    source_styles = read_styles_xml(
        source_docx
    )


    template_styles = read_styles_xml(
        template_docx
    )



    # docDefaults覆盖

    replace_doc_defaults(
        source_styles,
        template_styles
    )



    # style替换

    replace_count, add_count = (
        replace_styles(
            source_styles,
            template_styles
        )
    )



    new_styles_xml = etree.tostring(
        source_styles,
        xml_declaration=True,
        encoding="UTF-8",
        standalone=True
    )



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
                    "word/styles.xml"
                ):

                    data = new_styles_xml



                zout.writestr(
                    item,
                    data
                )



    print(
        "样式替换完成"
    )

    print(
        f"覆盖样式:{replace_count}"
    )

    print(
        f"新增样式:{add_count}"
    )



if __name__ == "__main__":


    replace_styles_xml(

        source_docx=
        "input/source.docx",

        template_docx=
        "template/standard.docx",

        output_docx=
        "output/style_replace.docx"

    )