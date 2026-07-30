import re


PATTERN = re.compile(
    r"^\s*(\d+)(\.(\d+))*[\.、\s]+(.+)$"
)



def detect_manual_number(text):


    if not text:

        return None



    result = PATTERN.match(
        text
    )


    if not result:

        return None



    prefix = result.group(0)


    title = result.group(4)



    parts = re.findall(
        r"\d+",
        prefix
    )



    level = len(parts)-1



    return {

        "level":level,

        "title":title

    }