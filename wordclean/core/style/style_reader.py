import zipfile

from lxml import etree



class StyleReader:
    """
    模板样式读取器
    """



    def read(
        self,
        template_file
    ):

        """
        从标准模板读取styles.xml
        """


        with zipfile.ZipFile(

            template_file,

            "r"

        ) as z:


            xml = z.read(

                "word/styles.xml"

            )



        return etree.fromstring(
            xml
        )