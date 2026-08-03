from pathlib import Path


from style_importer import (
    replace_styles_xml
)


from heading_mapper import (
    update_heading_style
)


BASE_DIR = Path(__file__).parent



SOURCE_DOC = (
    BASE_DIR
    /
    "input"
    /
    "source.docx"
)



TEMPLATE_DOC = (
    BASE_DIR
    /
    "template"
    /
    "standard.docx"
)



STYLE_IMPORT_DOC = (
    BASE_DIR
    /
    "output"
    /
    "style_import.docx"
)



FINAL_DOC = (
    BASE_DIR
    /
    "output"
    /
    "final.docx"
)




# 测试数据
# 后续替换为你的json文件

LEVEL_PLAN = [

    {
        "index":1,

        "text":
        "第一章 总则",

        "level":0
    },


    {
        "index":2,

        "text":
        "1.1 范围",

        "level":1
    },


    {
        "index":3,

        "text":
        "1.1.1 内容",

        "level":2
    }

]





def main():


    print(
        "="*30
    )

    print(
        "开始导入模板样式"
    )



    replace_styles_xml(

        source_docx=str(
            SOURCE_DOC
        ),


        template_docx=str(
            TEMPLATE_DOC
        ),


        output_docx=str(
            STYLE_IMPORT_DOC
        )

    )



    print(
        "="*30
    )


    print(
        "开始根据level更新标题样式"
    )



    update_heading_style(

        docx_file=str(
            STYLE_IMPORT_DOC
        ),


        level_plan=LEVEL_PLAN,


        output_file=str(
            FINAL_DOC
        )

    )



    print(
        "="*30
    )


    print(
        "全部处理完成"
    )


    print(
        FINAL_DOC
    )





if __name__ == "__main__":

    main()