import re

print("新的模式判断函数")

NUMBER_PATTERN = re.compile(
    r"^\s*\d+\.\d+(?:\.\d+)*(\s|、|\.|．)"
)


def detect_document_mode(
        paragraphs,
        check_count=100
):

    manual_count = 0

    auto_count = 0

    checked = 0


    for p in paragraphs:


        text = get_text(p)


        if not text:
            continue


        checked += 1


        # 手动编号

        if NUMBER_PATTERN.match(text):

            manual_count += 1


        # 自动编号

        num_id, ilvl = get_number_info(p)


        if num_id is not None:

            auto_count += 1



        if checked >= check_count:

            break



    print(
        "手动编号数量:",
        manual_count
    )


    print(
        "自动编号数量:",
        auto_count
    )


    if manual_count >= 3:

        return "number"



    if auto_count >= 3:

        return "number"



    return "navigation"