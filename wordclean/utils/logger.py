import os

from loguru import logger as _logger


from config.settings import LOG_DIR



if not os.path.exists(
    LOG_DIR
):

    os.makedirs(
        LOG_DIR
    )



_logger.add(

    os.path.join(
        LOG_DIR,
        "standardization.log"
    ),

    rotation="10 MB",

    encoding="utf-8",

    retention="30 days"

)



logger = _logger