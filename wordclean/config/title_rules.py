import re



TITLE_RULES = {


    # 一级标题

    1: [

        r"^[一二三四五六七八九十]+、",

        r"^[0-9]+[、.]"

    ],



    # 二级标题

    2: [

        r"^[0-9]+\.[0-9]+",

        r"^[（(][0-9]+[）)]"

    ],



    # 三级标题

    3: [

        r"^[0-9]+\.[0-9]+\.[0-9]+"

    ]

}



def get_title_level(text):

    """
    根据文本判断标题等级

    返回:
        int | None
    """


    for level, rules in TITLE_RULES.items():

        for rule in rules:

            if re.match(
                rule,
                text.strip()
            ):

                return level


    return None