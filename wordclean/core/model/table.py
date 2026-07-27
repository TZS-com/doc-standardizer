class Table:
    """
    Word表格对象
    """



    def __init__(self):


        self.rows = []


        self.table_type = None


        self.style = None


        self.target_style = None



    def add_row(
        self,
        row
    ):

        """
        添加一行

        row:
        list
        """

        self.rows.append(
            row
        )



    def get_headers(self):

        """
        获取第一行作为表头
        """

        if self.rows:

            return self.rows[0]


        return []



    def row_count(self):

        return len(
            self.rows
        )



    def column_count(self):

        if self.rows:

            return len(
                self.rows[0]
            )


        return 0