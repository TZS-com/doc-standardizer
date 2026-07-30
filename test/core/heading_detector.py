import re


NUMBER_PATTERN=re.compile(
    r"^\d+(\.\d+)*"
)



def is_heading(text, has_num=False):


    if has_num:

        return True


    if NUMBER_PATTERN.match(text):

        return True


    return False