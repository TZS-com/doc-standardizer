from lxml import etree




class XmlParser:
    """
    XML解析器
    """



    def parse(self, xml_data):

        """
        bytes转换XML对象
        """


        return etree.fromstring(
            xml_data
        )



    def find_all(
        self,
        root,
        xpath
    ):

        """
        XPath查询
        """


        return root.xpath(
            xpath
        )