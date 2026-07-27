TABLE_RULES = {


    "equipment_confirm":{


        "name":
            "设备确认表",


        "keywords":[

            "设备名称",

            "规格型号",

            "设备编号"

        ]

    },



    "instrument_confirm":{


        "name":
            "仪器确认表",


        "keywords":[

            "仪器名称",

            "型号",

            "校准日期"

        ]

    },



    "verification_record":{


        "name":
            "验证记录表",


        "keywords":[

            "项目",

            "标准",

            "结果"

        ]

    }

}



def match_table_type(headers):

    """
    根据表头判断表格类型
    """


    for key, rule in TABLE_RULES.items():


        count = 0


        for word in rule["keywords"]:

            if word in headers:

                count += 1



        if count >= 2:

            return key



    return None