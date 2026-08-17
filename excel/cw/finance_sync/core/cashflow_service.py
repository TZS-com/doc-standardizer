from datetime import datetime

import pandas as pd

from core.excel_reader import get_sheet_names, read_journal
from core.excel_writer import WorkbookWriter


CASHFLOW_FIELDS = [
    "单位名称",
    "月",
    "日",
    "摘要",
    "明细摘要",
    "对方科目",
    "现金流备注",
    "收入",
    "支出",
    "业务类别",
]


def select_month():
    """读取用户输入；空输入时使用本机当前月份。"""
    default_month = datetime.now().month

    while True:
        value = input(f"请输入同步月份（1-12，直接回车同步本月 {default_month} 月）：").strip()
        if not value:
            return default_month

        if value.isdigit() and 1 <= int(value) <= 12:
            return int(value)

        print("月份请输入 1 到 12 之间的整数。")


class CashflowSyncService:
    def __init__(self, journal, cashflow_file, cashflow_sheet, month, logger):
        self.journal = journal
        self.cashflow_file = cashflow_file
        self.cashflow_sheet = cashflow_sheet
        self.month = month
        self.logger = logger

    def run(self):
        writer = WorkbookWriter(self.cashflow_file)

        try:
            header_row = writer.find_header_row(self.cashflow_sheet, CASHFLOW_FIELDS)
            if header_row is None:
                raise ValueError(
                    f"现金流量基础表缺少Sheet或表头不匹配:{self.cashflow_sheet}"
                )

            for sheet in get_sheet_names(self.journal):
                try:
                    df = read_journal(self.journal, sheet)
                    if df is None:
                        continue

                    month_values = pd.to_numeric(df["月"], errors="coerce")
                    month_rows = df.loc[month_values == self.month]
                    if month_rows.empty:
                        continue

                    rows = [self._map_row(sheet, row) for _, row in month_rows.iterrows()]
                    writer.write_sheet(
                        self.cashflow_sheet, rows, start_row=header_row + 1
                    )
                    self.logger.info(
                        f"现金流量基础表 {sheet} 写入:{len(rows)}条（月:{self.month}）"
                    )
                except Exception as error:
                    self.logger.exception(f"现金流量基础表 {sheet} 处理异常:{error}")

            writer.save()
            self.logger.info(f"现金流量基础表同步完成（月:{self.month}）")
        finally:
            writer.close()

    @staticmethod
    def _map_row(unit_name, row):
        # 对来源中暂未维护的现金流字段保留空值，而不是中断整张 Sheet 的同步。
        return {
            "单位名称": unit_name,
            "月": row.get("月"),
            "日": row.get("日"),
            "摘要": row.get("摘要"),
            "明细摘要": row.get("明细摘要"),
            "对方科目": row.get("对方科目"),
            "现金流备注": row.get("现金流备注"),
            "收入": row.get("收入"),
            "支出": row.get("支出"),
            "业务类别": row.get("业务类别"),
        }
