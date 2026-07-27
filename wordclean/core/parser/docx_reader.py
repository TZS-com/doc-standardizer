import zipfile

import os

from utils.exception import DocumentReadError




class DocxReader:
    """
    docx文件读取器
    """



    def read(self, filepath):

        """
        解压docx

        返回:

        {
            xml文件路径: 内容
        }

        """


        if not os.path.exists(filepath):

            raise DocumentReadError(
                "文件不存在"
            )



        result = {}



        try:

            with zipfile.ZipFile(
                filepath,
                "r"
            ) as z:


                for name in z.namelist():


                    if name.endswith(".xml"):


                        result[name] = z.read(
                            name
                        )



        except Exception as e:


            raise DocumentReadError(
                str(e)
            )



        return result