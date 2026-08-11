from pathlib import Path
from loguru import logger

from config import LOG_DIR


def init_logger():

    LOG_DIR.mkdir(exist_ok=True)


    logger.add(
        LOG_DIR / "run.log",
        rotation="10 MB",
        encoding="utf-8"
    )


    return logger