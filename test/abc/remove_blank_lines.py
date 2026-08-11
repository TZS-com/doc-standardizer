from docx import Document
from pathlib import Path
from lxml import etree


W_NS = {
    "w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
}


def is_toc_paragraph(paragraph):
    """
    判断是否为目录段落
    """

    element = paragraph._element


    # python-docx xpath 已经内置 w 命名空间
    instr_text = element.xpath(
        ".//w:instrText"
    )


    for item in instr_text:
        if "TOC" in (item.text or ""):
            return True


    # 超链接目录
    hyperlinks = element.xpath(
        ".//w:hyperlink"
    )

    if hyperlinks:
        return True


    return False


def remove_blank_lines(
        input_docx,
        output_docx
):

    doc = Document(
        input_docx
    )


    remove_count = 0

    in_body = False


    for paragraph in list(doc.paragraphs):


        style_name = paragraph.style.name


        # ---------------------
        # 判断目录
        # ---------------------
        if (
            style_name.startswith("TOC")
            or is_toc_paragraph(paragraph)
        ):
            continue



        text = paragraph.text.strip()



        # ---------------------
        # 遇到第一个有内容段落
        # 先认为进入正文
        # ---------------------
        if text:

            in_body = True



        # ---------------------
        # 正文开始后删除空行
        # ---------------------
        if (
            in_body
            and text == ""
        ):

            # 图片保护
            if paragraph._element.xpath(
                ".//w:drawing"
            ):
                continue


            p = paragraph._element

            p.getparent().remove(
                p
            )


            remove_count += 1



    doc.save(
        output_docx
    )


    print(
        f"删除正文空行: {remove_count}"
    )



if __name__ == "__main__":


    input_file = Path(
        r"D:\效率提升自动化\test\abc\input\test.docx"
    )


    output_file = Path(
        r"D:\效率提升自动化\test\abc\处理后.docx"
    )


    remove_blank_lines(
        input_file,
        output_file
    )