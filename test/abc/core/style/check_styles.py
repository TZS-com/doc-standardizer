from zipfile import ZipFile
from lxml import etree


NS={
    "w":
    "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
}



def check_styles(docx):

    with ZipFile(docx) as z:

        xml=z.read(
            "word/styles.xml"
        )


    root=etree.fromstring(xml)


    styles=root.xpath(
        "//w:style[@w:type='paragraph']",
        namespaces=NS
    )


    for s in styles:

        sid=s.get(
            "{%s}styleId"%NS["w"]
        )

        name=s.xpath(
            "./w:name/@w:val",
            namespaces=NS
        )


        print(
            sid,
            name
        )



check_styles(
    r"D:\效率提升自动化\test\abc\template\template.docx"
)