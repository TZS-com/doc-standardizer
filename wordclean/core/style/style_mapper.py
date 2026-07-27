import os

from core.xml.xml_reader import XMLReader
from core.xml.xml_utils import xpath



class StyleMapper:
    """
    标准模板样式映射器


    功能:

    读取standard.docx中的styles.xml

    建立:

    Heading1
          |
          ↓
    模板真实styleId


    """



    def __init__(self):

        self.reader = XMLReader()

        self.style_map = {}




    def load(
        self,
        template_dir
    ):

        """
        加载模板styles.xml


        template_dir:

            解压后的模板目录


        """

        styles_xml = os.path.join(

            template_dir,

            "styles.xml"

        )


        root = self.reader.read(

            styles_xml

        )


        styles = xpath(

            root,

            ".//w:style"

        )


        for style in styles:


            style_id = style.get(

                "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}styleId"

            )


            name_nodes = xpath(

                style,

                "./w:name"

            )


            if not name_nodes:

                continue



            name = name_nodes[0].get(

                "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}val"

            )



            if not name:

                continue



            name_lower = name.lower()



            if (
                "heading 1" in name_lower
                or
                "heading1" in name_lower
                or
                "标题 1" in name
                or
                "标题1" in name
            ):


                self.style_map[
                    "Heading1"
                ] = style_id




            elif (
                "heading 2" in name_lower
                or
                "heading2" in name_lower
                or
                "标题 2" in name
                or
                "标题2" in name
            ):


                self.style_map[
                    "Heading2"
                ] = style_id




            elif (
                "heading 3" in name_lower
                or
                "heading3" in name_lower
                or
                "标题 3" in name
                or
                "标题3" in name
            ):


                self.style_map[
                    "Heading3"
                ] = style_id



        return self.style_map





    def get_style_id(
        self,
        level
    ):

        """
        获取模板真实styleId

        """

        return self.style_map.get(
            level
        )