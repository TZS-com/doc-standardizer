from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil
import tempfile


from analyzer.outline_reader import analyze_docx

from mapper.heading_mapper import HeadingMapper

from writer.style_applier import apply_styles

from core.style_cleaner import clean_document_style

from core.style_importer import replace_styles_xml

from core.cleaner.document_preprocessor import (
    preprocess_document
)


BASE_DIR = Path(__file__).parent


INPUT_DIR = BASE_DIR / "input"

OUTPUT_DIR = BASE_DIR / "output"

TEMPLATE_DIR = BASE_DIR / "template"




def unzip_docx(
        docx_file,
        target_dir
):

    with ZipFile(
            docx_file,
            "r"
    ) as z:

        z.extractall(
            target_dir
        )




def zip_docx(
        source_dir,
        output_file
):

    with ZipFile(
            output_file,
            "w",
            ZIP_DEFLATED
    ) as z:

        for file in source_dir.rglob("*"):

            if file.is_file():

                z.write(
                    file,
                    file.relative_to(source_dir)
                )





def clean_docx_style(
        docx_file
):

    with tempfile.TemporaryDirectory() as temp:

        temp_dir = Path(temp)


        unzip_docx(
            docx_file,
            temp_dir
        )


        document_xml = (
            temp_dir
            /
            "word"
            /
            "document.xml"
        )




        clean_document_style(
            document_xml
        )


        zip_docx(
            temp_dir,
            docx_file
        )





def process(
        source_docx,
        template_docx,
        output_docx
):


    print("==============================")
    print("开始处理")
    print("输入文件:", source_docx)
    print("==============================")


    temp_docx = (
        BASE_DIR
        /
        "temp_clean.docx"
    )



    # =========================
    # 1.读取原始目录结构
    # =========================

    print("\n[1] 开始分析原始文档目录")


    outline = analyze_docx(
        source_docx
    )


    print(
        "目录识别完成"
    )


    print(
        "识别数量:",
        len(outline)
    )


    for item in outline[:10]:

        print(item)



    # =========================
    # 2.映射标题样式
    # =========================

    print("\n[2] 开始样式映射")


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
        "映射完成"
    )


    print(
        "标题数量:",
        len(outline)
    )


    for item in outline[:10]:

        print(item)



    # =========================
    # 3.复制原始文件
    # =========================

    print("\n[3] 复制原始文档")


    shutil.copy(
        source_docx,
        temp_docx
    )


    print(
        "复制完成:",
        temp_docx
    )



    # =========================
    # 4.提前导入模板样式
    # =========================

    print("\n[4] 导入模板样式")


    replace_styles_xml(
        temp_docx,
        template_docx,
        temp_docx
    )


    print(
        "模板样式导入完成"
    )



    # =========================
    # 5.清洗全文格式
    # =========================

    print("\n[5] 开始清洗全文格式")


    clean_docx_style(
        temp_docx
    )


    print(
        "正文格式清洗完成"
    )



    # =========================
    # 6.恢复标题样式
    # =========================

    print("\n[6] 开始恢复标题样式")


    apply_styles(
        temp_docx,
        output_docx,
        outline
    )


    print(
        "标题样式恢复完成"
    )



    print("\n==============================")
    print("全部处理完成")
    print("输出:", output_docx)
    print("==============================")






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



    source_docx = files[0]



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
        source_docx,
        template_docx,
        output_docx
    )




if __name__ == "__main__":

    main()