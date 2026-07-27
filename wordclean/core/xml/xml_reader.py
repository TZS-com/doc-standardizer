from lxml import etree



class XMLReader:
    """
    XML读取工具
    """



    def read(
        self,
        filepath
    ):

        """
        读取XML文件

        返回:
            Element
        """


        parser = etree.XMLParser(
            remove_blank_text=False
        )


        with open(
            filepath,
            "rb"
        ) as f:


            content = f.read()



        return etree.fromstring(
            content,
            parser
        )