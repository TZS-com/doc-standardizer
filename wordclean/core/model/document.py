from typing import List


class Document:
    """
    Word文档对象

    保存：
    - 段落
    - 表格
    - 样式
    - 编号
    """



    def __init__(self):

        self.paragraphs = []

        self.tables = []

        self.styles = {}

        self.numbering = None



    def add_paragraph(self, paragraph):

        """
        添加段落
        """

        self.paragraphs.append(
            paragraph
        )



    def add_table(self, table):

        """
        添加表格
        """

        self.tables.append(
            table
        )



    def add_style(
        self,
        style_id,
        style
    ):

        """
        添加样式
        """

        self.styles[
            style_id
        ] = style



    def get_paragraph_count(self):

        return len(
            self.paragraphs
        )



    def get_table_count(self):

        return len(
            self.tables
        )