import os
import zipfile
import shutil


class DocxUnpacker:
    """
    docx解压器

    将:

        xxx.docx

    转换为:

        临时目录结构
    """



    def unpack(
        self,
        docx_file,
        output_dir
    ):

        """
        解压docx

        参数:

        docx_file:
            原始docx路径


        output_dir:
            解压目录

        """


        if os.path.exists(output_dir):

            shutil.rmtree(
                output_dir
            )


        os.makedirs(
            output_dir
        )



        with zipfile.ZipFile(
            docx_file,
            "r"
        ) as zip_file:


            zip_file.extractall(
                output_dir
            )


        return output_dir