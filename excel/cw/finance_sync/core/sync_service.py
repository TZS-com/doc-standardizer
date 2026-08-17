from core.excel_reader import get_sheet_names, read_journal
from core.excel_writer import WorkbookWriter
from core.mapper import map_expense, map_income
from core.splitter import split_income_expense


class SyncService:
    def __init__(self, journal, income_file, expense_file, logger):
        self.journal = journal
        self.income_file = income_file
        self.expense_file = expense_file
        self.logger = logger

    def run(self):
        # 目标账本较大。一次同步内仅加载一次，最后统一保存，避免每个
        # 来源 Sheet 都触发一次耗时的 Excel 解压和压缩。
        income_writer = WorkbookWriter(self.income_file)
        expense_writer = WorkbookWriter(self.expense_file)

        try:
            sheets = get_sheet_names(self.journal)
            self.logger.info(f"发现日记账Sheet数量:{len(sheets)}")

            for sheet in sheets:
                self.logger.info(f"开始处理:{sheet}")

                try:
                    df = read_journal(self.journal, sheet)
                    if df is None:
                        self.logger.warning(f"{sheet}不是有效日记账，跳过")
                        continue

                    income_rows = []
                    expense_rows = []
                    for category, row in split_income_expense(df):
                        if category == "income":
                            income_rows.append(map_income(row))
                        elif category == "expense":
                            expense_rows.append(map_expense(row))

                    income_sheet = sheet + "收入"
                    expense_sheet = sheet + "支出"

                    if income_rows:
                        result = income_writer.write_sheet(
                            income_sheet, income_rows, start_row=4
                        )
                        if result is False:
                            self.logger.warning(f"收入账缺少Sheet:{income_sheet}")
                        else:
                            self.logger.info(
                                f"{income_sheet}写入:{len(income_rows)}条"
                            )

                    if expense_rows:
                        result = expense_writer.write_sheet(
                            expense_sheet, expense_rows, start_row=4
                        )
                        if result is False:
                            self.logger.warning(f"支出账缺少Sheet:{expense_sheet}")
                        else:
                            self.logger.info(
                                f"{expense_sheet}写入:{len(expense_rows)}条"
                            )

                except Exception as error:
                    self.logger.exception(f"{sheet}处理异常:{error}")

            income_writer.save()
            expense_writer.save()
            self.logger.info("全部同步完成")
        finally:
            income_writer.close()
            expense_writer.close()
