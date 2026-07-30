import re


NUMBER_PATTERN = re.compile(
    r"^\s*\d+(\.\d+)*[\s、.．]+"
)



def clean_manual_number(text):


    if not text:

        return text



    return NUMBER_PATTERN.sub(
        "",
        text
    )