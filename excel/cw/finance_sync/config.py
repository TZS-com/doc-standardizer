from pathlib import Path


BASE_DIR = Path(__file__).parent


DATA_DIR = BASE_DIR / "data"


OUTPUT_DIR = BASE_DIR / "output"


LOG_DIR = BASE_DIR / "logs"


# 输入文件

JOURNAL_FILE = DATA_DIR / "日记账.xlsx"


INCOME_FILE = DATA_DIR / "收入账.xlsx"


EXPENSE_FILE = DATA_DIR / "支出账.xlsx"



# 输出文件

OUTPUT_INCOME = OUTPUT_DIR / "收入账_同步.xlsx"


OUTPUT_EXPENSE = OUTPUT_DIR / "支出账_同步.xlsx"



# 表头位置

JOURNAL_HEADER = 3

ACCOUNT_HEADER = 2