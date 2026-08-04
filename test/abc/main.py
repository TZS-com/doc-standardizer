from pathlib import Path


from analyzer.outline_reader import analyze_docx

from mapper.heading_mapper import HeadingMapper

from writer.style_applier import apply_styles

from core.style_importer import replace_styles_xml



BASE_DIR = Path(__file__).parent


INPUT_DIR = BASE_DIR / "input"

TEMPLATE_DIR = BASE_DIR / "template"

OUTPUT_DIR = BASE_DIR / "output"

TEMP_DIR = BASE_DIR / "temp"




def process(
        input_docx,
        template_docx,
        output_docx
):


    print(
        "开始处理:",
        input_docx
    )



    middle_docx = (
        TEMP_DIR
        /
        "middle.docx"
    )


    TEMP_DIR.mkdir(
        exist_ok=True
    )



    # 1.分析文档目录结构

    outline = analyze_docx(
        input_docx
    )


    print(
        "目录识别完成"
    )



    # 2.level映射样式

    mapper = HeadingMapper(
        BASE_DIR
        /
        "mapper"
        /
        "level_style_mapping.json"
    )


    outline = mapper.map_outline(
        outline
    )


    print(
        "样式映射完成"
    )



    # 3.根据level写入Heading样式

    apply_styles(
        input_docx,
        middle_docx,
        outline
    )


    print(
        "标题样式写入完成"
    )



    # 4.导入模板样式

    replace_styles_xml(
        middle_docx,
        template_docx,
        output_docx
    )


    print(
        "模板样式替换完成"
    )




def main():


    OUTPUT_DIR.mkdir(
        exist_ok=True
    )


    files = list(
        INPUT_DIR.glob(
            "*.docx"
        )
    )


    if not files:

        print(
            "input目录没有docx文件"
        )

        return



    input_docx = files[0]



    template_docx = (
        TEMPLATE_DIR
        /
        "template.docx"
    )



    output_docx = (
        OUTPUT_DIR
        /
        "result.docx"
    )



    process(
        input_docx,
        template_docx,
        output_docx
    )




if __name__ == "__main__":

    main()