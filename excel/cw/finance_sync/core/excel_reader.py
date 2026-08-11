import pandas as pd



def get_sheet_names(file):

    excel = pd.ExcelFile(file)

    return excel.sheet_names

JOURNAL_REQUIRED_FIELDS = [
    "年",
    "月",
    "日",
    "摘要",
    "明细摘要",
    "收入",
    "支出"
]


def check_journal_sheet(df):

    columns = [
        str(col).strip()
        for col in df.columns
    ]

    missing = set(JOURNAL_REQUIRED_FIELDS) - set(columns)

    if missing:
        return False

    return True


def read_journal(
        file,
        sheet
):

    df = pd.read_excel(
        file,
        sheet_name=sheet,
        header=3
    )


    df.columns = (
        df.columns
        .astype(str)
        .str.strip()
    )


    if not check_journal_sheet(df):

        return None


    df.dropna(
        how="all",
        inplace=True
    )


    return df



def read_account_template(
        file
):

    return pd.ExcelFile(file)