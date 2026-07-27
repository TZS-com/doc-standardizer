class TableParser:
    """
    表格解析器
    """



    def parse(
        self,
        table
    ):

        """
        分析表格结构
        """



        result = {

            "rows":
                table.rows,


            "row_count":
                table.row_count(),


            "column_count":
                table.column_count(),


            "headers":
                table.get_headers()

        }



        return result