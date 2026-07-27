WORD_NAMESPACE = {

    "w":
    "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

}



def xpath(
    node,
    path
):

    """
    Word XML XPath查询
    """


    return node.xpath(

        path,

        namespaces=WORD_NAMESPACE

    )





def get_text(
    paragraph
):

    """
    获取段落文字
    """



    texts = xpath(

        paragraph,

        ".//w:t/text()"

    )



    return "".join(
        texts
    )





def get_style_id(
    paragraph
):

    """
    获取段落样式ID
    """


    styles = xpath(

        paragraph,

        "./w:pPr/w:pStyle/@w:val"

    )



    if styles:

        return styles[0]


    return None





def set_style_id(
    paragraph,
    style_id
):

    """
    设置段落样式
    """



    pPr = xpath(

        paragraph,

        "./w:pPr"

    )


    if not pPr:

        return



    style_nodes = xpath(

        paragraph,

        "./w:pPr/w:pStyle"

    )


    if style_nodes:


        style_nodes[0].set(

            "{%s}val"
            %
            WORD_NAMESPACE["w"],

            style_id

        )