import os

from config.settings import (
    INPUT_DIR,
    OUTPUT_DIR
)

from core.processor import DocumentProcessor

from utils.logger import logger


def get_word_files():
    """
    获取input目录中的有效Word文件

    过滤:
    1. Word临时文件(~$xxx.docx)
    2. 非docx文件

    返回:
        list[str]
    """

    files = []

    for file in os.listdir(INPUT_DIR):

        # 跳过Word临时文件
        if file.startswith("~$"):
            logger.warning(
                f"跳过Word临时文件:{file}"
            )
            continue


        # 只处理docx
        if not file.lower().endswith(".docx"):
            continue


        files.append(
            os.path.join(
                INPUT_DIR,
                file
            )
        )


    return files



def main():

    logger.info(
        "Word标准化程序启动"
    )


    if not os.path.exists(OUTPUT_DIR):

        os.makedirs(
            OUTPUT_DIR
        )


    documents = get_word_files()


    if not documents:

        logger.warning(
            "没有发现待处理Word文件"
        )

        return


    processor = DocumentProcessor()


    for doc in documents:

        try:

            logger.info(
                f"开始处理:{doc}"
            )


            processor.process(
                doc
            )


            logger.info(
                f"处理完成:{doc}"
            )


        except Exception as e:

            logger.exception(
                f"处理失败:{doc},{e}"
            )


if __name__ == "__main__":

    main()