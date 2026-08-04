from zipfile import ZipFile
from lxml import etree


NS={
"w":
"http://schemas.openxmlformats.org/"
"wordprocessingml/2006/main"
}


W_STYLE=(
"{http://schemas.openxmlformats.org/"
"wordprocessingml/2006/main}"
"pStyle"
)



def apply_styles(
        docx,
        output,
        outline
):




    with ZipFile(docx,"r") as zin:

        files={
            name:zin.read(name)
            for name in zin.namelist()
        }


    root=etree.fromstring(
        files["word/document.xml"]
    )


    paragraphs=root.xpath(
        "./w:body/w:p",
        namespaces=NS
    )


    for item in outline:


        if item["style"] is None:

            continue


        index=item["index"]


        p=paragraphs[index]

        if index >= len(paragraphs):
            print(
                "索引超出范围:",
                index,
                "总段落:",
                len(paragraphs)
            )

            continue

        p = paragraphs[index]


        pPr=p.find(
            "w:pPr",
            namespaces=NS
        )


        if pPr is None:

            pPr=etree.SubElement(
                p,
                "{%s}pPr"%NS["w"]
            )


        old=pPr.find(
            "w:pStyle",
            namespaces=NS
        )


        if old is not None:

            pPr.remove(old)



        style=etree.SubElement(
            pPr,
            W_STYLE
        )


        style.set(
            "{%s}val"%NS["w"],
            item["style"]
        )

        print(
            "写入样式完成:",
            item["index"],
            item["style"]
        )

        outlineLvl = etree.SubElement(
            pPr,
            "{%s}outlineLvl" % NS["w"]
        )

        outlineLvl.set(
            "{%s}val" % NS["w"],
            str(item["level"])
        )

        print(
            "处理段落:",
            item["index"],
            "|",
            item["text"][:30],
            "|",
            item["style"]
        )



    files["word/document.xml"]=etree.tostring(
        root,
        xml_declaration=True,
        encoding="UTF-8",
        standalone=True
    )


    with ZipFile(
        output,
        "w"
    ) as zout:


        for name,data in files.items():

            zout.writestr(
                name,
                data
            )

