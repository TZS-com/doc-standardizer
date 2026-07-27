"""
表格样式处理
"""


from docx.enum.table import (
    WD_TABLE_ALIGNMENT
)



def apply_basic_table_style(table):


    # 表格居中

    table.alignment = (

        WD_TABLE_ALIGNMENT.CENTER

    )


    # 自动调整关闭

    table.autofit=False