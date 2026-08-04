from zipfile import ZipFile
from lxml import etree


NS = {
    "w":
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"
}



def read_xml(docx, filename):

    with ZipFile(docx, "r") as z:

        data = z.read(filename)

    return etree.fromstring(data)



def parse_styles(docx):

    """
    返回:

    {
        styleId: level
    }

    """

    root = read_xml(
        docx,
        "word/styles.xml"
    )


    result = {}


    styles = root.xpath(
        "//w:style",
        namespaces=NS
    )


    for style in styles:


        style_id = style.get(
            "{%s}styleId"
            %
            NS["w"]
        )


        if not style_id:
            continue



        # 优先读取outlineLvl

        level = style.xpath(
            "./w:pPr/w:outlineLvl/@w:val",
            namespaces=NS
        )


        if level:

            result[style_id] = int(level[0])

            continue



        # 根据名称判断

        name = style.xpath(
            "./w:name/@w:val",
            namespaces=NS
        )


        if name:


            n = name[0].lower()


            if "heading1" in n or "标题1" in n:
                result[style_id] = 0

            elif "heading2" in n or "标题2" in n:
                result[style_id] = 1

            elif "heading3" in n or "标题3" in n:
                result[style_id] = 2

            elif "heading4" in n or "标题4" in n:
                result[style_id] = 3



    return result