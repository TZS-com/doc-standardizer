"""
Word XML表格样式复制
"""


from copy import deepcopy




def copy_table_properties(
        source_table,
        target_table
):


    source_tblPr = (

        source_table
        ._tbl
        .tblPr

    )


    target_tblPr=(

        target_table
        ._tbl
        .tblPr

    )


    if source_tblPr is None:


        return



    if target_tblPr is None:


        return



    # 清空目标格式

    for child in target_tblPr:


        target_tblPr.remove(child)



    # 复制

    for child in source_tblPr:


        target_tblPr.append(

            deepcopy(child)

        )