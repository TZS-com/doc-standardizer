"""
表格处理总入口
"""

from core.table.table_detector import detect_tables

from core.table.table_classifier import classify_table

from core.table.table_style import apply_basic_table_style

from core.table.xml_style_copy import copy_table_properties




def process_tables(doc, templates):


    tables = detect_tables(doc)


    for table in tables:


        table_info = classify_table(table)



        if table_info["type"] == "unknown":


            apply_basic_table_style(table)


            continue



        template_name = table_info["template"]



        if template_name in templates:


            template_table = templates[template_name]


            copy_table_properties(

                template_table,

                table

            )