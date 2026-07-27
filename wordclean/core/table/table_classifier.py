"""
表格分类器
"""


from config.table_rules import TABLE_TYPES




def get_table_text(table):


    texts=[]


    for row in table.rows:


        for cell in row.cells:


            texts.append(

                cell.text.strip()

            )


    return " ".join(texts)




def classify_table(table):


    content=get_table_text(table)



    for name,rule in TABLE_TYPES.items():


        for keyword in rule["keywords"]:


            if keyword in content:


                return {


                    "type":
                    name,


                    "template":
                    rule["template"]


                }



    return {


        "type":
        "unknown",


        "template":
        None

    }