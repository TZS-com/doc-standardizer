import re



def detect_text_number(
        text
):
    """
    识别文本中的手动编号

    支持:

    1 标题
    1.1 标题
    1.1.1 标题
    1.1.1.1 标题

    返回:

    level

    """

    if not text:
        return None



    match = re.match(
        r"^\s*\d+(?:\.\d+)*[\.、．]?\s*",
        text
    )


    if not match:

        return None



    number = re.search(
        r"\d+(?:\.\d+)*",
        match.group()
    )


    if not number:

        return None



    value = number.group()



    return len(
        value.split(".")
    ) - 1