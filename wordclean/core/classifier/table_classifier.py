from config.table_rules import match_table_type



class TableClassifier:
    """
    表格分类器
    """



    def classify(
        self,
        table
    ):


        headers = table.get_headers()



        table_type = match_table_type(
            headers
        )



        table.table_type = table_type



        return table_type