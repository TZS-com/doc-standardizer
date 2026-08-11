import re

NUMBER_PATTERN = re.compile(
     r"^\s*\d+(?:\.\d+)+[\.、．]?\s+"
)

from zipfile import ZipFile
from lxml import etree


from .style_parser import parse_styles
from .numbering_parser import parse_numbering



NS={
    "w":
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"
}





def read_xml(docx,filename):

    with ZipFile(docx,"r") as z:

        data=z.read(filename)

    return etree.fromstring(data)





def get_text(p):

    texts=p.xpath(
        ".//w:t/text()",
        namespaces=NS
    )

    return "".join(texts).strip()





def get_style_id(p):

    style=p.xpath(
        "./w:pPr/w:pStyle/@w:val",
        namespaces=NS
    )

    return style[0] if style else None





def get_number_info(p):

    num=p.xpath(
        "./w:pPr/w:numPr",
        namespaces=NS
    )


    if not num:
        return None,None


    num_id=p.xpath(
        "./w:pPr/w:numPr/w:numId/@w:val",
        namespaces=NS
    )


    ilvl=p.xpath(
        "./w:pPr/w:numPr/w:ilvl/@w:val",
        namespaces=NS
    )


    if num_id and ilvl:

        return (
            int(num_id[0]),
            int(ilvl[0])
        )


    return None,None






def detect_text_number(text):


    m=re.match(
        r"^\s*(\d+(?:\.\d+)*)",
        text
    )


    if not m:
        return None


    return len(
        m.group(1).split(".")
    )-1





def detect_level(
        p,
        style_map,
        numbering_map,
        mode
):


    text = get_text(p)



    # 编号文章

    if mode == "number":


        # 自动编号

        num_id, ilvl = get_number_info(p)


        if num_id in numbering_map:

            level = numbering_map[num_id].get(
                ilvl
            )


            if level is not None:

                return level,"auto_number"



        # 手动编号

        level = detect_text_number(
            text
        )


        if level is not None:

            return level,"manual_number"



    # 无编号文章

    if mode == "navigation":


        sid = get_style_id(p)


        if sid is None:

            return None, None



        sid_lower = sid.lower()


        if sid_lower.startswith("toc"):

            return None, None



        if sid in style_map:

            return (
                style_map[sid],
                "navigation"
            )


    # 必须保留

    return None, None


def is_in_table(
        paragraph
):

    parent = paragraph.getparent()


    while parent is not None:

        if parent.tag.endswith("tbl"):

            return True


        parent = parent.getparent()


    return False


def is_toc_paragraph(
        paragraph
):

    style = paragraph.xpath(

        "./w:pPr/w:pStyle/@w:val",

        namespaces=NS

    )


    if not style:

        return False


    style_id = style[0].lower()


    if style_id.startswith(
        "toc"
    ):

        return True


    return False

def analyze_docx(docx):


    style_map = parse_styles(
        docx
    )


    numbering_map = parse_numbering(
        docx
    )


    root = read_xml(
        docx,
        "word/document.xml"
    )

    paragraphs = root.xpath(
        "./w:body/w:p",
        namespaces=NS
    )

    mode = detect_document_mode(
        paragraphs
    )

    print(
        "文档模式:",
        mode
    )


    print(
        "段落数量:",
        len(paragraphs)
    )


    result = []


    for index, p in enumerate(paragraphs):


        # 跳过表格

        if is_in_table(p):

            continue



        # 跳过目录

        if is_toc_paragraph(p):

            continue



        text = get_text(p)


        if not text:

            continue

        level, source = detect_level(
            p,
            style_map,
            numbering_map,
            mode
        )



        print(
            index,
            "|",
            level,
            "|",
            source,
            "|",
            text
        )



        result.append({

            "index": index,

            "text": text,

            "level": level,

            "source": source

        })



    print(
        "有效段落:",
        len(result)
    )

    print(
        "识别模式:",
        mode,
        "|",
        "来源:",
        source,
        "|",
        text
    )


    return result

def detect_document_mode(paragraphs):

    manual_count = 0

    navigation_count = 0


    for p in paragraphs:


        text = get_text(p)


        if not text:

            continue



        # ==================================
        # 模式1：
        # 标题文字自身带编号
        #
        # 示例：
        # 1 项目背景
        # 1.1 系统架构
        # 1.1.1 数据库设计
        #
        # 只认文字内容
        # 不看Word编号属性
        # ==================================

        if NUMBER_PATTERN.match(text):

            manual_count += 1

            print(
                "标题编号:",
                text
            )



        # ==================================
        # 模式2：
        # Word大纲/导航结构
        #
        # 示例：
        # 项目背景   Heading1
        # 系统架构   Heading2
        #
        # 不关心有没有自动编号
        # 只看标题样式
        # ==================================

        sid = get_style_id(p)


        if sid:

            sid_lower = sid.lower()


            if (
                sid_lower.startswith("heading")
                or sid_lower.startswith("标题")
            ):

                navigation_count += 1


                print(
                    "导航标题:",
                    text,
                    "style:",
                    sid
                )



    print("======================")

    print(
        "标题编号数量:",
        manual_count
    )


    print(
        "导航标题数量:",
        navigation_count
    )

    print("======================")



    # ==================================
    # 判断模式
    # ==================================


    # 优先判断文字编号模式

    if manual_count >= 3:

        return "number"



    # 判断Word导航结构模式

    if navigation_count >= 3:

        return "navigation"



    # 默认按导航模式处理
    # 后续可以继续通过style_map判断

    return "navigation"
if __name__=="__main__":


    data=analyze_docx(
        "../input/test.docx"
    )


    for i in data:

        print(i)