from lxml import etree

from core.manual_number_cleaner import (
    clean_manual_number
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



def replace_text_nodes(
        texts,
        new_text
):

    """
    替换文本节点

    保留原有run结构，
    避免清理编号时误删除其他文字
    """

    if not texts:

        return


    if len(texts) == 1:

        texts[0].text = new_text

        return



    # 第一个节点写入清理后的文字

    texts[0].text = new_text



    # 后续节点保持原内容

    # 不清空，避免丢失格式文字



def create_num_pr(
        pPr,
        level
):

    """
    创建新的编号属性
    """

    # 删除旧编号

    old_numPr = pPr.find(
        "w:numPr",
        NS
    )


    if old_numPr is not None:

        pPr.remove(
            old_numPr
        )



    # 创建新的编号

    numPr = etree.SubElement(
        pPr,
        W + "numPr"
    )


    ilvl = etree.SubElement(
        numPr,
        W + "ilvl"
    )


    ilvl.set(
        W + "val",
        str(level)
    )


    numId = etree.SubElement(
        numPr,
        W + "numId"
    )


    numId.set(
        W + "val",
        "999"
    )


    return numPr



def rewrite_document(
        xml_file,
        plan
):


    tree = etree.parse(
        xml_file
    )


    paragraphs = tree.xpath(
        "//w:body/w:p",
        namespaces=NS
    )


    changed = 0



    for item in plan:


        index = item["index"]


        level = item["level"]


        source = item.get(
            "source",
            "word"
        )



        if index >= len(paragraphs):

            continue



        p = paragraphs[index]



        # ==========================
        # 1. 处理手敲编号
        # ==========================

        if source == "manual":


            texts = p.xpath(
                ".//w:t",
                namespaces=NS
            )


            if texts:


                old_text = "".join(
                    [
                        t.text or ""
                        for t in texts
                    ]
                )


                new_text = clean_manual_number(
                    old_text
                )


                replace_text_nodes(
                    texts,
                    new_text
                )



        # ==========================
        # 2. 获取段落属性
        # ==========================

        pPr = p.find(
            "w:pPr",
            NS
        )


        if pPr is None:

            pPr = etree.SubElement(
                p,
                W + "pPr"
            )



        # ==========================
        # 3. 重新绑定编号
        # ==========================

        create_num_pr(
            pPr,
            level
        )



        changed += 1



    tree.write(
        xml_file,
        encoding="UTF-8",
        xml_declaration=True
    )


    return changed