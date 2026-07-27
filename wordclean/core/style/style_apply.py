import os
import zipfile
import shutil


from core.style.style_reader import StyleReader

from core.xml.xml_reader import XMLReader

from core.xml.xml_writer import XMLWriter



class StyleApply:
    """
    样式应用器

    修改:

    word/styles.xml

    """



    def __init__(self):

        self.reader = StyleReader()

        self.xml_reader = XMLReader()

        self.xml_writer = XMLWriter()





    def apply(

        self,

        docx_dir,

        template_file

    ):


        target_style = os.path.join(

            docx_dir,

            "word",

            "styles.xml"

        )



        if not os.path.exists(
            target_style
        ):

            return



        #
        # 读取模板样式
        #

        template_styles = self.reader.read(

            template_file

        )


        #
        # 覆盖原styles.xml
        #


        self.xml_writer.write(

            template_styles,

            target_style

        )


        return True