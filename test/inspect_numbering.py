import json
import os

from core.docx_xml import DocxXML
from core.inspector import NumberingInspector



def main():


    input_file= "core/style/input/source.docx"


    extractor=DocxXML(
        input_file
    )


    docx_dir=extractor.extract()



    inspector=NumberingInspector(
        docx_dir
    )


    result=inspector.analyze()



    os.makedirs(
        "output",
        exist_ok=True
    )


    with open(
        "output/numbering_analysis.json",
        "w",
        encoding="utf-8"
    ) as f:


        json.dump(
            result,
            f,
            ensure_ascii=False,
            indent=4
        )


    print(
        "编号分析完成"
    )



if __name__=="__main__":

    main()