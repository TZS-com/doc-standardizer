import json
import os

from core.heading_filter import (
    filter_headings
)

from core.rebuild_plan import (
    create_plan
)



INPUT = (
    "output/numbering_analysis.json"
)


OUTPUT = (
    "output/rebuild_plan.json"
)



def main():


    with open(
        INPUT,
        "r",
        encoding="utf-8"
    ) as f:

        data=json.load(f)



    # 1. 筛选章节

    headings=filter_headings(
        data
    )


    print(
        "识别章节数量:",
        len(headings)
    )


    # 2. 生成新编号方案

    plan=create_plan(
        headings
    )


    # 3. 保存方案

    os.makedirs(
        "output",
        exist_ok=True
    )


    with open(
        OUTPUT,
        "w",
        encoding="utf-8"
    ) as f:


        json.dump(
            plan,
            f,
            ensure_ascii=False,
            indent=4
        )


    print(
        "重建方案生成:",
        OUTPUT
    )



if __name__=="__main__":

    main()