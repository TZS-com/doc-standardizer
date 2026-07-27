"""
Word文档结构解析模块

功能：

1. 基于OOXML解析document.xml
2. 保留Word原始结构顺序
3. 识别段落和表格
4. 提取文本内容
5. 为后续:
    - 标题识别
    - 样式处理
    - 表格处理
    - 编号处理

提供统一数据结构


"""



from lxml import etree



from utils.exception import WordProcessException





# Word XML命名空间

NS = {

    "w":

    "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

}






class DocumentParser:
    """
    Word XML解析器
    """



    def __init__(

            self,

            document_tree

    ):

        """
        参数:

        document_tree:
            lxml解析后的document.xml对象

        """


        self.tree = document_tree




    def parse(self):
        """
        解析整个Word正文结构

        返回:

        [
            {
                "type":"paragraph",
                "text":"",
                "xml":Element
            },


            {
                "type":"table",
                "text":"",
                "xml":Element
            }

        ]

        """



        try:


            body = self._get_body()



            result=[]



            for element in body:



                tag = etree.QName(

                    element

                ).localname




                if tag == "p":


                    result.append(

                        self._parse_paragraph(

                            element

                        )

                    )



                elif tag == "tbl":


                    result.append(

                        self._parse_table(

                            element

                        )

                    )



            return result




        except Exception as e:


            raise WordProcessException(

                f"Word结构解析失败:{e}"

            )







    def _get_body(self):
        """
        获取document.xml中的body节点
        """


        body = self.tree.find(

            ".//w:body",

            namespaces=NS

        )


        if body is None:


            raise WordProcessException(

                "document.xml中未找到body节点"

            )



        return body







    def _parse_paragraph(

            self,

            paragraph

    ):

        """
        解析普通段落
        """



        text = self._get_paragraph_text(

            paragraph

        )



        style = self._get_paragraph_style(

            paragraph

        )



        return {


            "type":

            "paragraph",



            "text":

            text,



            "style":

            style,



            "xml":

            paragraph



        }







    def _parse_table(

            self,

            table

    ):

        """
        解析表格
        """



        rows=[]



        for tr in table.findall(

            "w:tr",

            namespaces=NS

        ):


            row=[]



            for tc in tr.findall(

                "w:tc",

                namespaces=NS

            ):



                cell_text=self._get_cell_text(

                    tc

                )



                row.append(

                    cell_text

                )



            rows.append(

                row

            )




        return {


            "type":

            "table",



            "rows":

            rows,



            "xml":

            table


        }







    def _get_paragraph_text(

            self,

            paragraph

    ):

        """
        获取段落文字

        Word:

        <w:p>
            <w:r>
                <w:t>
                    文本
                </w:t>
            </w:r>
        </w:p>

        """



        texts=[]



        nodes=paragraph.findall(

            ".//w:t",

            namespaces=NS

        )



        for node in nodes:


            if node.text:


                texts.append(

                    node.text

                )



        return "".join(texts).strip()







    def _get_cell_text(

            self,

            cell

    ):

        """
        获取单元格文本
        """


        texts=[]



        nodes=cell.findall(

            ".//w:t",

            namespaces=NS

        )



        for node in nodes:


            if node.text:


                texts.append(

                    node.text

                )



        return "".join(texts).strip()







    def _get_paragraph_style(

            self,

            paragraph

    ):

        """
        获取段落原始样式

        对应:

        w:pPr/w:pStyle

        """



        style_node = paragraph.find(

            "w:pPr/w:pStyle",

            namespaces=NS

        )



        if style_node is None:


            return None




        return style_node.get(

            "{%s}val"

            %

            NS["w"]

        )