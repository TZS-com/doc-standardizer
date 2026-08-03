from zipfile import ZipFile
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



def read_styles(
        docx
):

    with ZipFile(
            docx,
            "r"
    ) as z:


        xml = z.read(
            "word/styles.xml"
        )


    root = etree.fromstring(
        xml
    )


    result = {}


    styles = root.xpath(
        "./w:style",
        namespaces=NS
    )


    for style in styles:


        if style.get(
            W+"type"
        ) != "paragraph":

            continue



        style_id = style.get(
            W+"styleId"
        )


        name = style.find(
            "w:name",
            namespaces=NS
        )


        if name is None:

            continue



        style_name = name.get(
            W+"val"
        )


        result[style_name] = {

            "style_id": style_id

        }


    return result