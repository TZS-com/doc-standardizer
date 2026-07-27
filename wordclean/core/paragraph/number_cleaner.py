"""
标题编号清理
"""


import re



NUMBER_PATTERNS=[


    r'^[一二三四五六七八九十百]+、',


    r'^\d+\.',


    r'^\d+、',


    r'^\d+\.\d+',


    r'^（\d+）',


    r'^\(\d+\)',


]




def clean_number(text):


    result=text.strip()



    for pattern in NUMBER_PATTERNS:


        result=re.sub(

            pattern,

            "",

            result

        )



    return result.strip()