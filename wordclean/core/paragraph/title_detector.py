"""
标题识别
"""


import re


from config.title_rules import TITLE_RULES




def detect_title_level(text):


    text=text.strip()



    if not text:

        return None



    for rule in TITLE_RULES:


        result=re.match(

            rule["pattern"],

            text

        )


        if result:


            return rule["level"]



    return None