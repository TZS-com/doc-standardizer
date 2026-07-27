from lxml import etree



class XMLWriter:
    """
    XML写入工具
    """



    def write(
        self,
        root,
        filepath
    ):


        tree = etree.ElementTree(
            root
        )



        tree.write(

            filepath,

            encoding="UTF-8",

            xml_declaration=True,

            standalone=False

        )