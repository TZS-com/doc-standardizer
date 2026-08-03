
from zipfile import ZipFile, ZIP_DEFLATED
from pathlib import Path



def replace_styles_xml(
        source_docx,
        template_docx,
        output_docx
):

    """
    使用模板docx中的styles.xml替换目标文档
    """



    temp_file = Path(
        output_docx
    ).with_suffix(
        ".tmp.docx"
    )


    with ZipFile(
            source_docx,
            "r"
    ) as zin:


        with ZipFile(
                temp_file,
                "w",
                ZIP_DEFLATED
        ) as zout:


            for item in zin.infolist():


                if item.filename == "word/styles.xml":


                    with ZipFile(
                            template_docx,
                            "r"
                    ) as template_zip:


                        styles = template_zip.read(
                            "word/styles.xml"
                        )


                    zout.writestr(
                        item,
                        styles
                    )


                else:


                    zout.writestr(
                        item,
                        zin.read(
                            item.filename
                        )
                    )


    temp_file.replace(
        output_docx
    )



if __name__ == "__main__":


    replace_styles_xml(

        "input.docx",

        "template.docx",

        "output.docx"

    )