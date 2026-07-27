"""
Word表格XML复制
"""


from copy import deepcopy



from lxml import etree




NS = {

    "w":

    "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

}




def copy_xml_node(source,target):
    """
    XML节点复制
    """


    target.clear()


    for child in source:


        target.append(

            deepcopy(child)

        )




def copy_table_property(
        source_table,
        target_table
):
    """
    复制表格属性
    """


    source_tblPr = source_table.find(

        "w:tblPr",

        namespaces=NS

    )


    target_tblPr = target_table.find(

        "w:tblPr",

        namespaces=NS

    )



    if (

        source_tblPr is not None

        and

        target_tblPr is not None

    ):


        copy_xml_node(

            source_tblPr,

            target_tblPr

        )




def copy_cell_property(
        source_cell,
        target_cell
):
    """
    复制单元格属性
    """


    source_tcPr = source_cell.find(

        "w:tcPr",

        namespaces=NS

    )


    target_tcPr = target_cell.find(

        "w:tcPr",

        namespaces=NS

    )



    if (

        source_tcPr is not None

        and

        target_tcPr is not None

    ):


        copy_xml_node(

            source_tcPr,

            target_tcPr

        )