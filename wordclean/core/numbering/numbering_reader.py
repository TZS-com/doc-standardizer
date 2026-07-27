from lxml import etree

import zipfile



class NumberingReader:
    """
    Word编号定义读取器
    """



    def __init__(self):

        self.namespace = {

            "w":
            "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

        }



    def read(
        self,
        template_file=None
    ):

        """
        读取编号定义

        返回:

        {
            numId:
            abstractNumId
        }

        """

        if template_file is None:

            return {}



        result = {}



        with zipfile.ZipFile(
            template_file,
            "r"
        ) as z:


            xml = z.read(
                "word/numbering.xml"
            )



        root = etree.fromstring(
            xml
        )



        nums = root.xpath(
            ".//w:num",
            namespaces=self.namespace
        )



        for num in nums:


            num_id = num.xpath(
                "./@w:numId",
                namespaces=self.namespace
            )


            abstract = num.xpath(
                "./w:abstractNumId/@w:val",
                namespaces=self.namespace
            )



            if num_id and abstract:

                result[
                    num_id[0]
                ] = abstract[0]



        return result