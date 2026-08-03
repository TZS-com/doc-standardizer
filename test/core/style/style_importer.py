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



def read_styles(docx):

    with ZipFile(docx, "r") as z:

        xml = z.read(
            "word/styles.xml"
        )

    return etree.fromstring(xml)



def find_style(
        root,
        style_id
):

    styles = root.xpath(
        "./w:style",
        namespaces=NS
    )


    for style in styles:

        sid = style.get(
            W+"styleId"
        )

        if sid == style_id:

            return style


    return None



def merge_styles(
        source_root,
        template_root
):

    replace = 0
    add = 0


    template_styles = template_root.xpath(
        "./w:style",
        namespaces=NS
    )


    for style in template_styles:


        style_id = style.get(
            W+"styleId"
        )


        if not style_id:

            continue



        old = find_style(
            source_root,
            style_id
        )


        if old is not None:

            source_root.remove(
                old
            )

            replace += 1


        else:

            add += 1



        source_root.append(
            style
        )


    return replace, add



def replace_doc_defaults(
        source_root,
        template_root
):

    old = source_root.find(
        "w:docDefaults",
        namespaces=NS
    )


    new = template_root.find(
        "w:docDefaults",
        namespaces=NS
    )


    if new is None:

        return



    if old is not None:

        source_root.remove(
            old
        )


    source_root.insert(
        0,
        new
    )



def import_styles(
        source_docx,
        template_docx,
        output_docx
):

    source_root = read_styles(
        source_docx
    )


    template_root = read_styles(
        template_docx
    )


    replace_doc_defaults(
        source_root,
        template_root
    )


    replace, add = merge_styles(
        source_root,
        template_root
    )


    styles_xml = etree.tostring(
        source_root,
        encoding="UTF-8",
        xml_declaration=True,
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


                if item.filename == "word/styles.xml":

                    data = styles_xml


                zout.writestr(
                    item,
                    data
                )



    print(
        "样式库融合完成"
    )

    print(
        f"覆盖:{replace}"
    )

    print(
        f"新增:{add}"
    )



if __name__ == "__main__":


    import_styles(

        "input/source.docx",

        "template/standard.docx",

        "output/merged.docx"

    )