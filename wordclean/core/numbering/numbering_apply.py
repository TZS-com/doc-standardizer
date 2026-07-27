import os
import zipfile

from lxml import etree



class NumberingApply:
    """
    Word编号应用器

    修改:

    word/numbering.xml

    """



    def apply(
        self,
        docx_dir,
        template_file
    ):


        target_numbering = os.path.join(

            docx_dir,

            "word",

            "numbering.xml"

        )


        if not os.path.exists(
            target_numbering
        ):

            return False



        #
        # 读取模板编号
        #

        with zipfile.ZipFile(

            template_file,

            "r"

        ) as z:


            template_xml = z.read(

                "word/numbering.xml"

            )



        root = etree.fromstring(
            template_xml
        )



        #
        # 覆盖原编号文件
        #

        tree = etree.ElementTree(
            root
        )


        tree.write(

            target_numbering,

            encoding="UTF-8",

            xml_declaration=True

        )


        return True