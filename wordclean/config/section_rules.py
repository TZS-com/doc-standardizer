SECTION_RULES = {


    "project_overview":[

        "项目概述",

        "项目简介"

    ],



    "purpose":[

        "验证目的",

        "目的"

    ],



    "responsibility":[

        "职责",

        "人员职责"

    ],



    "procedure":[

        "验证内容",

        "实施过程"

    ],



    "attachment":[

        "附件",

        "附录"

    ]

}



def match_section(text):

    """
    判断当前标题属于哪个章节
    """


    text = text.strip()



    for section, keywords in SECTION_RULES.items():

        for keyword in keywords:


            if keyword in text:

                return section



    return None