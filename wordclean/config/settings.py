import os


BASE_DIR = os.path.dirname(
    os.path.dirname(
        os.path.abspath(__file__)
    )
)


# 输入文件目录

INPUT_DIR = os.path.join(
    BASE_DIR,
    "input"
)


# 输出目录

OUTPUT_DIR = os.path.join(
    BASE_DIR,
    "output"
)


# 标准模板

TEMPLATE_FILE = os.path.join(
    BASE_DIR,
    "template",
    "standard.docx"
)


# 模板解析目录

TEMPLATE_EXTRACT_DIR = os.path.join(
    BASE_DIR,
    "template",
    "extracted"
)


# 日志目录

LOG_DIR = os.path.join(
    BASE_DIR,
    "logs"
)



# 是否保留中间XML

KEEP_XML = True


# 是否覆盖输出文件

OVERWRITE_OUTPUT = True


# 支持文件类型

SUPPORTED_EXTENSIONS = [

    ".docx"

]