from core.cashflow_service import CashflowSyncService, select_month
from utils.logger import init_logger
from config import CASHFLOW_FILE, CASHFLOW_SHEET, JOURNAL_FILE


def main():
    logger = init_logger()
    logger.info("开始同步现金流量基础表")

    if not CASHFLOW_FILE.exists():
        logger.error(f"未找到现金流量基础表:{CASHFLOW_FILE}")
        return

    month = select_month()
    service = CashflowSyncService(
        JOURNAL_FILE,
        CASHFLOW_FILE,
        CASHFLOW_SHEET,
        month,
        logger,
    )
    service.run()
    logger.info("现金流量基础表同步完成")


if __name__ == "__main__":
    main()
