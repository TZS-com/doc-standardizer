"""
段落样式处理
"""


from config.style_mapping import (
    TITLE_STYLE_MAP,
    BODY_STYLE
)




def apply_paragraph_style(item):


    paragraph=item["object"]



    if item["type"]=="heading":


        level=item["level"]


        paragraph.style = (

            TITLE_STYLE_MAP.get(

                level,

                BODY_STYLE

            )

        )



        # 替换文本

        paragraph.text=item["text"]



    elif item["type"]=="body":


        paragraph.style=BODY_STYLE