from core.sync_service import SyncService

from utils.logger import init_logger

from config import *


def main():


    logger = init_logger()



    logger.info(
        "开始同步"
    )


    service = SyncService(

        JOURNAL_FILE,

        OUTPUT_INCOME,

        OUTPUT_EXPENSE,

        logger

    )


    service.run()



    logger.info(
        "同步完成"
    )



if __name__=="__main__":

    main()