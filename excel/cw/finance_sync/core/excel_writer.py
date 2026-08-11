from openpyxl import load_workbook
from pathlib import Path
import zipfile



def write_sheet(
        file,
        sheet,
        data,
        start_row=4
):


    file = Path(file)


    if not file.exists():

        raise FileNotFoundError(
            f"文件不存在:{file}"
        )


    if not zipfile.is_zipfile(file):

        raise Exception(
            f"Excel文件损坏:{file}"
        )



    wb = load_workbook(file)



    if sheet not in wb.sheetnames:

        wb.close()

        return False



    ws = wb[sheet]



    # generator转列表
    # 用于获取插入行数

    rows = list(data)



    if not rows:

        wb.close()

        return True



    # ==========================
    # 在表头下面插入新行
    # ==========================

    ws.insert_rows(

        start_row,

        amount=len(rows)

    )



    current_row = start_row



    for item in rows:


        for col,value in enumerate(

                item.values(),

                start=1

        ):


            ws.cell(

                row=current_row,

                column=col,

                value=value

            )


        current_row += 1



    wb.save(file)


    wb.close()


    return True