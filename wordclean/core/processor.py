import os
import shutil


from core.package.unpacker import (
    DocxUnpacker
)

from core.package.packer import (
    DocxPacker
)


from core.style.style_apply import (
    StyleApply
)


from core.numbering.numbering_apply import (
    NumberingApply
)


from config.settings import (
    TEMPLATE_FILE,
    OUTPUT_DIR
)


from utils.logger import logger





class DocumentProcessor:
    """
    Word格式标准化处理器

    基于OOXML修改
    """



    def __init__(self):


        self.unpacker = DocxUnpacker()


        self.packer = DocxPacker()


        self.style_apply = StyleApply()


        self.numbering_apply = NumberingApply()





    def process(
        self,
        input_file
    ):


        filename=os.path.basename(
            input_file
        )



        temp_dir=os.path.join(

            "temp",

            filename.replace(
                ".docx",
                ""
            )

        )



        logger.info(
            "开始解压docx"
        )



        self.unpacker.unpack(

            input_file,

            temp_dir

        )



        logger.info(
            "开始格式处理"
        )



        #
        # 1.
        # 修改document.xml
        #

        self.style_apply.apply(

            temp_dir,

            TEMPLATE_FILE

        )



        #
        # 2.
        # 编号处理
        #

        self.numbering_apply.apply(

            temp_dir,

            TEMPLATE_FILE

        )



        logger.info(
            "重新打包"
        )



        output=os.path.join(

            OUTPUT_DIR,

            filename

        )



        self.packer.pack(

            temp_dir,

            output

        )



        shutil.rmtree(

            temp_dir,

            ignore_errors=True

        )



        logger.info(

            f"输出完成:{output}"

        )


        return output