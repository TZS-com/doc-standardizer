import re


NUMBER_REGEX = re.compile(
     r"^\s*(\d+(?:\.\d+)*)(?:\s*|、|．|\.)(.+)$"
)



def parse_manual_number(text):

    """
    识别:

    1 项目
    1.1 项目
    1.1.1 项目

    """

    result = NUMBER_REGEX.match(text)


    if not result:
        return None


    number = result.group(1)

    title = result.group(2)


    level = number.count(".") + 1


    return {

        "number":number,

        "title":title,

        "level":level

    }