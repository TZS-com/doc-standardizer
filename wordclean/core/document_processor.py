"""
Word标准化核心流程
"""


import os



from core.xml.xml_reader import (

    extract_docx,

    get_document_xml

)



from core.xml.xml_writer import (

    save_xml

)



from core.exporter import (

    package_docx

)




from config.settings import TEMP_DIR




class DocumentProcessor:



    def __init__(

            self,

            input_file,

            output_file

    ):


        self.input_file=input_file

        self.output_file=output_file



        self.work_dir=os.path.join(

            TEMP_DIR,

            os.path.basename(

                input_file

            )

            .replace(

                ".docx",

                ""

            )

        )




    def process(self):


        # 1 解压


        extract_docx(

            self.input_file,

            self.work_dir

        )



        # 2读取document.xml


        document_tree=get_document_xml(

            self.work_dir

        )



        # 后续：

        # 段落处理

        # 表格处理

        # 样式处理



        # 3保存XML


        xml_path=os.path.join(

            self.work_dir,

            "word",

            "document.xml"

        )



        save_xml(

            document_tree,

            xml_path

        )



        # 4重新打包


        package_docx(

            self.work_dir,

            self.output_file

        )