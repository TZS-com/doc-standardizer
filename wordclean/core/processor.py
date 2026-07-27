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


from core.style.document_style_apply import (
    DocumentStyleApply
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

    基于OOXML直接修改docx结构
    """



    def __init__(self):


        # docx解压

        self.unpacker = DocxUnpacker()



        # docx重新打包

        self.packer = DocxPacker()



        # styles.xml处理

        self.style_apply = StyleApply()



        # document.xml段落样式处理

        self.document_style_apply = (
            DocumentStyleApply()
        )



        # numbering.xml处理

        self.numbering_apply = (
            NumberingApply()
        )





    def process(
        self,
        input_file
    ):

        """
        执行Word标准化流程


        input_file:

            原始docx文件


        return:

            输出文件路径

        """



        filename = os.path.basename(
            input_file
        )



        temp_dir = os.path.join(

            "temp",

            filename.replace(
                ".docx",
                ""
            )

        )



        try:


            logger.info(
                f"开始处理:{filename}"
            )



            # ==========================
            # 1. 解压docx
            # ==========================


            logger.info(
                "开始解压docx"
            )


            self.unpacker.unpack(

                input_file,

                temp_dir

            )



            # ==========================
            # 2. 替换模板styles.xml
            # ==========================


            logger.info(
                "开始应用模板样式"
            )


            self.style_apply.apply(

                temp_dir,

                TEMPLATE_FILE

            )



            # ==========================
            # 3. 修改document.xml
            #    标题/正文样式
            # ==========================


            logger.info(
                "开始处理段落样式"
            )


            self.document_style_apply.apply(

                temp_dir

            )



            # ==========================
            # 4. 替换编号体系
            # ==========================


            logger.info(
                "开始处理编号"
            )


            self.numbering_apply.apply(

                temp_dir,

                TEMPLATE_FILE

            )



            # ==========================
            # 5. 输出目录
            # ==========================


            if not os.path.exists(
                OUTPUT_DIR
            ):


                os.makedirs(
                    OUTPUT_DIR
                )



            output_file = os.path.join(

                OUTPUT_DIR,

                filename

            )



            # ==========================
            # 6. 重新打包docx
            # ==========================


            logger.info(
                "开始重新打包docx"
            )


            self.packer.pack(

                temp_dir,

                output_file

            )



            logger.info(

                f"处理完成:{output_file}"

            )



            return output_file



        except Exception as e:


            logger.error(

                f"处理失败:{input_file},{e}"

            )


            raise



        finally:


            # ==========================
            # 7. 清理临时目录
            # ==========================


            if os.path.exists(
                temp_dir
            ):


                shutil.rmtree(

                    temp_dir,

                    ignore_errors=True

                )