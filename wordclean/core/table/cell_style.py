"""
单元格格式处理
"""


from docx.shared import Pt



def set_cell_font(cell,font_name="宋体",size=10.5):


    for paragraph in cell.paragraphs:


        for run in paragraph.runs:


            run.font.name=font_name


            run.font.size=Pt(size)




def set_cell_alignment(cell):


    for paragraph in cell.paragraphs:


        paragraph.alignment=1