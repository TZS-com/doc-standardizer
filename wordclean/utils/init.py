"""
Word标准化工具入口
"""


from utils.file_utils import (
    get_docx_files,
    create_dir
)


from utils.logger import (
    get_logger
)


from config.settings import (
    INPUT_DIR,
    OUTPUT_DIR
)



logger=get_logger()



def main():


    logger.info(
        "Word标准化程序启动"
    )


    create_dir(
        OUTPUT_DIR
    )


    files=get_docx_files(
        INPUT_DIR
    )


    logger.info(
        f"发现文件数量:{len(files)}"
    )


    for file in files:


        logger.info(
            f"待处理:{file}"
        )


        # 后续接入处理流程

        # process_document(file)



    logger.info(
        "处理结束"
    )




if __name__=="__main__":

    main()