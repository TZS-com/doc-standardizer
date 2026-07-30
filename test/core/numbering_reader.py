from lxml import etree


NS = {
    "w":
    "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
}



class NumberingReader:


    def __init__(self, xml_path):

        self.tree = etree.parse(
            xml_path
        )

    def get_num_map(self):

        result = {}

        nums = self.tree.xpath(
            "//w:num",
            namespaces=NS
        )

        for num in nums:

            # 获取 numId 属性
            num_id = num.get(
                "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}numId"
            )

            # 获取 abstractNumId
            abstract_id_list = num.xpath(
                "./w:abstractNumId/@w:val",
                namespaces=NS
            )

            # 异常节点跳过

            if not num_id:
                continue

            if not abstract_id_list:
                continue

            abstract_id = abstract_id_list[0]

            result[num_id] = abstract_id

        return result



    def get_level_format(
            self,
            abstract_id,
            level
    ):

        """

        获取：

        %1
        %1.%2
        %1.%2.%3

        """


        nodes = self.tree.xpath(

            f"//w:abstractNum[@w:abstractNumId='{abstract_id}']"
            f"/w:lvl[@w:ilvl='{level}']/w:lvlText/@w:val",

            namespaces=NS
        )


        if nodes:

            return nodes[0]


        return None