import os
import zipfile
import shutil



class DocxExporter:
    """
    基于原docx结构重新打包
    """



    def export(
        self,
        source_docx,
        modified_dir,
        output_file
    ):

        """
        source_docx:
            原始docx

        modified_dir:
            修改后的docx解压目录

        output_file:
            输出路径
        """


        with zipfile.ZipFile(
            output_file,
            "w",
            zipfile.ZIP_DEFLATED
        ) as z:



            for root, dirs, files in os.walk(
                modified_dir
            ):


                for file in files:


                    filepath=os.path.join(
                        root,
                        file
                    )


                    arcname=os.path.relpath(
                        filepath,
                        modified_dir
                    )


                    z.write(
                        filepath,
                        arcname
                    )



        return output_file