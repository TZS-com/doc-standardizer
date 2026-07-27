from core.model.document import Document

from core.model.paragraph import Paragraph

from core.model.table import Table




class DocumentParser:
    """
    Word结构解析器
    """



    def parse(self, xml_package):


        document = Document()



        document_xml = xml_package.get(
            "word/document.xml"
        )


        if document_xml is None:

            return document



        from lxml import etree


        root = etree.fromstring(
            document_xml
        )



        namespace = {

            "w":
            "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

        }



        # 解析段落

        paragraphs = root.xpath(
            ".//w:p",
            namespaces=namespace
        )



        for p in paragraphs:


            text_nodes = p.xpath(
                ".//w:t/text()",
                namespaces=namespace
            )


            text = "".join(
                text_nodes
            )


            paragraph = Paragraph(
                text=text
            )


            document.add_paragraph(
                paragraph
            )



        # 解析表格


        tables = root.xpath(
            ".//w:tbl",
            namespaces=namespace
        )


        for tbl in tables:


            table = Table()



            rows = tbl.xpath(
                ".//w:tr",
                namespaces=namespace
            )


            for row in rows:


                cells=[]


                for cell in row.xpath(
                    ".//w:tc",
                    namespaces=namespace
                ):


                    texts = cell.xpath(
                        ".//w:t/text()",
                        namespaces=namespace
                    )


                    cells.append(
                        "".join(texts)
                    )


                table.add_row(
                    cells
                )



            document.add_table(
                table
            )



        return document