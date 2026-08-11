from lxml import etree


NS = {
    "w":
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"
}



def remove_blank_paragraphs(
        xml_file
):
    """
    删除 Word 正文中的空段落

    参数:
        xml_file:
            document.xml路径

    保留:
        图片段落
        对象段落
    """

    tree = etree.parse(
        xml_file
    )


    body = tree.find(
        ".//w:body",
        namespaces=NS
    )


    if body is None:
        return


    remove_list = []


    for p in body.findall(
            "w:p",
            namespaces=NS
    ):


        # 获取文字
        texts = p.findall(
            ".//w:t",
            namespaces=NS
        )


        text = "".join(
            t.text or ""
            for t in texts
        ).strip()



        # 图片
        has_drawing = p.findall(
            ".//w:drawing",
            namespaces=NS
        )


        # OLE对象
        has_object = p.findall(
            ".//w:object",
            namespaces=NS
        )



        # 空段落
        if (
            not text
            and not has_drawing
            and not has_object
        ):
            remove_list.append(
                p
            )



    for p in remove_list:
        body.remove(
            p
        )



    tree.write(
        xml_file,
        encoding="utf-8",
        xml_declaration=True
    )


    print(
        f"删除空行数量: {len(remove_list)}"
    )