import re
from core.manual_heading_detector import (
    detect_manual_number
)

def is_heading(item):


    # Word自动编号

    if item.get("numId"):

        return True



    # 手敲编号

    text = item.get(
        "text",
        ""
    )


    manual = detect_manual_number(
        text
    )


    if manual:

        return True



    return False


def filter_headings(data):


    result=[]


    for index,item in enumerate(data):


        if is_heading(item):

            manual = detect_manual_number(
                item.get("text", "")
            )

            if item.get("numId"):

                result.append(
                    {
                        "index": index,

                        "text": item["text"],

                        "level": item.get(
                            "level",
                            0
                        ),

                        "old_num_id":
                            item.get("numId"),

                        "source":
                            "word"
                    }
                )



            elif manual:

                result.append(

                    {

                        "index": index,

                        # 清理后的标题文字

                        "text":

                            manual["title"],

                        # 原始段落文字

                        "raw_text":

                            item["text"],

                        "level":

                            manual["level"],

                        "old_num_id":

                            None,

                        "source":

                            "manual"

                    }

                )


    return result