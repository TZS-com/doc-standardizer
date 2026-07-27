"""
Word表格检测
"""


def detect_tables(doc):


    tables=[]


    for table in doc.tables:


        tables.append(table)



    return tables