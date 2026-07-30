from docx import Document
from docx.text import paragraph


def read_docx(path):


    doc = Document(path)


    paragraphs=[]


    for index,p in enumerate(doc.paragraphs):


        text=p.text.strip()

        if text:
            print(
                index,
                repr(text)
            )


        paragraphs.append(
            {
                "index":index,

                "text":text
            }
        )



    return paragraphs