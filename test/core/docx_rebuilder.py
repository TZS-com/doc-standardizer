from lxml import etree

from .numbering_builder import (
    build_numbering
)


W_NS = (
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"
)


NS = {
    "w": W_NS
}



def rewrite_numbering(
        file
):


    tree = etree.parse(
        file
    )


    root = tree.getroot()



    # 删除旧编号定义

    old_abstracts = root.xpath(
        "./w:abstractNum",
        namespaces=NS
    )


    for node in old_abstracts:

        root.remove(node)



    old_nums = root.xpath(
        "./w:num",
        namespaces=NS
    )


    for node in old_nums:

        root.remove(node)



    # 写入新的编号体系

    abstract,num = build_numbering()



    root.append(
        abstract
    )


    root.append(
        num
    )



    tree.write(
        file,
        encoding="UTF-8",
        xml_declaration=True
    )