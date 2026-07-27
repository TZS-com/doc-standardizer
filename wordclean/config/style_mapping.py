"""
Word样式映射规则

key:
    原始文档可能存在的样式

value:
    标准模板中的样式
"""


STYLE_MAPPING = {


    # 正文

    "正文":
        "Normal",


    "Normal":
        "Normal",



    # 标题

    "标题 1":
        "Heading1",


    "标题1":
        "Heading1",



    "标题 2":
        "Heading2",


    "标题2":
        "Heading2",



    "标题 3":
        "Heading3",


    "标题3":
        "Heading3",



    # 表格文字

    "表格正文":
        "TableText",



    # 默认

    "默认段落字体":
        "Normal"

}



DEFAULT_STYLE = "Normal"