"""
Word文档加载模块
"""

from docx import Document


from utils.exception import WordProcessException



def load_document(path):

    try:

        doc = Document(path)

        return doc


    except Exception as e:


        raise WordProcessException(

            f"Word打开失败:{path},原因:{e}"

        )