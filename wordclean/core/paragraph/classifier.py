"""
段落分类器
"""


from core.paragraph.title_detector import (
    detect_title_level
)



from core.paragraph.number_cleaner import (
    clean_number
)



def classify_paragraph(paragraph):


    text=paragraph.text.strip()



    if not text:


        return {


            "type":
            "empty",


            "object":
            paragraph


        }



    level = detect_title_level(
        text
    )



    if level:



        return {


            "type":
            "heading",


            "level":
            level,


            "text":
            clean_number(text),


            "object":
            paragraph


        }



    return {


        "type":
        "body",


        "text":
        text,


        "object":
        paragraph


    }