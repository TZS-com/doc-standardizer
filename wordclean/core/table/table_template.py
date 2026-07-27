"""
标准表格模板读取模块
"""

from docx import Document



def load_table_templates(path):
    """
    加载标准表格模板

    参数:
        path:
            table_templates.docx路径

    返回:
        dict:
            {
                "equipment_table": table对象
            }
    """


    doc = Document(path)


    templates = {}


    for index, table in enumerate(doc.tables):


        templates[f"table_{index}"] = table


    return templates