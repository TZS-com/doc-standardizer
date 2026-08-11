import pandas as pd


def split_income_expense(df):
    """
    将日记账拆分为收入和支出数据流

    返回:
        ("income", row)
        ("expense", row)
    """

    for _, row in df.iterrows():

        income = row.get("收入")

        expense = row.get("支出")


        if (
            pd.notna(income)
            and income != 0
        ):

            yield "income", row


        elif (
            pd.notna(expense)
            and expense != 0
        ):

            yield "expense", row