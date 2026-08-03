from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil
import tempfile

from core.style_cleaner import (
    clean_document_style
)

from core.style_importer import (
    replace_styles_xml
)



BASE_DIR = Path(__file__).parent


INPUT_DIR = BASE_DIR / "input"

TEMPLATE_DIR = BASE_DIR / "template"

OUTPUT_DIR = BASE_DIR / "output"



def unzip_docx(
        docx_file,
        target_dir
):

    """
    解压docx
    """

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

    """
    重新压缩docx
    """

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




def process(
        input_docx,
        template_docx,
        output_docx
):


    print("开始处理:", input_docx)



    with tempfile.TemporaryDirectory() as temp:


        temp_dir = Path(temp)



        # 1 解压原始docx

        unzip_docx(
            input_docx,
            temp_dir
        )


        document_xml = (
            temp_dir
            /
            "word"
            /
            "document.xml"
        )


        # 2 根据level调整样式

        clean_document_style(
            document_xml
        )



        # 3 生成中间docx

        middle_docx = (
            temp_dir
            /
            "middle.docx"
        )


        zip_docx(
            temp_dir,
            middle_docx
        )



        # 4 导入模板样式

        replace_styles_xml(
            middle_docx,
            template_docx,
            output_docx
        )


    print(
        "处理完成:",
        output_docx
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