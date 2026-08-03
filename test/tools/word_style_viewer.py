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



def read_style_names(docx):

    with ZipFile(docx, "r") as z:

        xml = z.read(
            "word/styles.xml"
        )

    return etree.fromstring(xml)



def show_styles(docx):

    root = read_style_names(
        docx
    )


    styles = root.xpath(
        "./w:style",
        namespaces=NS
    )


    print()

    print(
        "{:<25} {:<15} {:<10}".format(
            "样式显示名称",
            "styleId",
            "类型"
        )
    )

    print("-"*55)



    for style in styles:


        style_type = style.get(
            W+"type"
        )


        if style_type != "paragraph":

            continue



        style_id = style.get(
            W+"styleId"
        )


        name = style.find(
            "w:name",
            namespaces=NS
        )


        if name is not None:

            display_name = name.get(
                W+"val"
            )

        else:

            display_name = ""



        print(
            "{:<25} {:<15} {:<10}".format(
                display_name,
                style_id,
                style_type
            )
        )



if __name__ == "__main__":


    show_styles(
        "../core/style/output/style_replace.docx"
    )