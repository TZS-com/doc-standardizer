class TableStyleMapper:
    """
    表格样式映射
    """



    def mapping(
        self,
        table_structure
    ):


        table_type = (
            table_structure
            .get(
                "type"
            )
        )



        style_map = {



            "equipment_confirm":

                "StandardTable",



            "instrument_confirm":

                "StandardTable",



            "verification_record":

                "RecordTable"



        }



        return style_map.get(
            table_type,
            "StandardTable"
        )