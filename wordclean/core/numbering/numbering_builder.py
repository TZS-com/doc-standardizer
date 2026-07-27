class NumberingBuilder:
    """
    编号关系构造器
    """



    def build(
        self,
        numbering_data
    ):

        """
        根据模板编号生成映射
        """



        result = {}



        index = 1



        for old_id, abstract_id in numbering_data.items():


            result[
                old_id
            ] = {


                "new_num_id":
                    index,


                "abstract_id":
                    abstract_id

            }


            index += 1



        return result