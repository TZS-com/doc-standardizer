from pathlib import Path

from core.cleaner.blank_paragraph_cleaner import (
    remove_blank_paragraphs
)



def preprocess_document(
        document_xml
):
    """
    Word文档预处理

    执行:
    1. 删除正文空行

    后续可扩展:
    2. 清理多余格式
    3. 清理无效标签
    4. 清理隐藏内容

    参数:
        document_xml:
            word/document.xml路径
    """


    document_xml = Path(
        document_xml
    )


    if not document_xml.exists():
        raise FileNotFoundError(
            f"文件不存在: {document_xml}"
        )


    print(
        "开始文档预处理..."
    )


    # 删除空段落
    remove_blank_paragraphs(
        document_xml
    )


    print(
        "文档预处理完成"
    )