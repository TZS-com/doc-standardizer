from core.excel_reader import (
    get_sheet_names,
    read_journal
)


from core.splitter import (
    split_income_expense
)


from core.mapper import (
    map_income,
    map_expense
)


from core.excel_writer import (
    write_sheet
)



class SyncService:


    def __init__(
            self,
            journal,
            income_file,
            expense_file,
            logger
    ):

        self.journal = journal

        self.income_file = income_file

        self.expense_file = expense_file

        self.logger = logger



    def run(self):


        sheets = get_sheet_names(
            self.journal
        )


        self.logger.info(
            f"发现日记账Sheet数量:{len(sheets)}"
        )



        for sheet in sheets:


            self.logger.info(
                f"开始处理:{sheet}"
            )


            try:


                # ==========================
                # 1.读取日记账
                # ==========================

                df = read_journal(
                    self.journal,
                    sheet
                )


                if df is None:


                    self.logger.warning(

                        f"{sheet}不是有效日记账，跳过"

                    )

                    continue



                # ==========================
                # 2.生成收入数据流
                # ==========================

                income_rows = []


                expense_rows = []



                for category,row in split_income_expense(df):


                    if category == "income":


                        income_rows.append(

                            map_income(row)

                        )


                    elif category == "expense":


                        expense_rows.append(

                            map_expense(row)

                        )



                # ==========================
                # 3.目标Sheet名称
                # ==========================

                income_sheet = (

                    sheet + "收入"

                )


                expense_sheet = (

                    sheet + "支出"

                )



                # ==========================
                # 4.写收入
                # ==========================

                if income_rows:


                    result = write_sheet(

                        self.income_file,

                        income_sheet,

                        income_rows,

                        start_row=4

                    )


                    if result is False:


                        self.logger.warning(

                            f"收入账缺少Sheet:"
                            f"{income_sheet}"

                        )

                    else:


                        self.logger.info(

                            f"{income_sheet}"
                            f"写入:{len(income_rows)}条"

                        )



                # ==========================
                # 5.写支出
                # ==========================

                if expense_rows:


                    result = write_sheet(

                        self.expense_file,

                        expense_sheet,

                        expense_rows,

                        start_row=4

                    )


                    if result is False:


                        self.logger.warning(

                            f"支出账缺少Sheet:"
                            f"{expense_sheet}"

                        )


                    else:


                        self.logger.info(

                            f"{expense_sheet}"
                            f"写入:{len(expense_rows)}条"

                        )




            except Exception as e:


                self.logger.exception(

                    f"{sheet}处理异常:{e}"

                )


                continue



        self.logger.info(

            "全部同步完成"

        )