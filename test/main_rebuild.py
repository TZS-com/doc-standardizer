import json

from core.rebuild_engine import (
    RebuildEngine
)



def main():


    with open(
        "output/rebuild_plan.json",
        encoding="utf-8"
    ) as f:

        plan=json.load(f)



    engine=RebuildEngine()


    count=engine.run(

        "input/test.docx",

        plan,

        "output/test_clean.docx"

    )


    print(
        "重建完成，修改章节:",
        count
    )



if __name__=="__main__":

    main()