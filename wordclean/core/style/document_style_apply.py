import os


from core.xml.xml_reader import XMLReader
from core.xml.xml_writer import XMLWriter
from core.style.style_mapper import (
    StyleMapper
)
from core.xml.xml_utils import (
    xpath,
    get_text
)


from core.classifier.heading_classifier import (
    HeadingClassifier
)



class DocumentStyleApply:
    """
    document.xml样式处理


    功能:

    1.读取所有段落

    2.识别标题级别

    3.修改pStyle


    """


    def __init__(self):


        self.reader = XMLReader()


        self.writer = XMLWriter()

        self.heading_classifier = HeadingClassifier()

        self.style_mapper = StyleMapper()




    def apply(
        self,
        docx_dir
    ):


        document_xml = os.path.join(

            docx_dir,

            "word",

            "document.xml"

        )


        root = self.reader.read(

            document_xml

        )

        template_dir = "template/extracted"

        self.style_mapper.load(
            template_dir
        )
        print(
            "模板样式映射:",
            self.style_mapper.style_map
        )



        paragraphs = xpath(

            root,

            ".//w:body/w:p"

        )



        for paragraph in paragraphs:


            text = get_text(

                paragraph

            )



            if not text.strip():

                continue



            old_style = (
                self.get_paragraph_style(
                    paragraph
                )
            )



            style = (
                self.heading_classifier.classify(
                    text,
                    old_style
                )
            )



            # 调试输出

            print(
                text[:50],
                "=>",
                style
            )

            if style:

                real_style_id = (
                    self.style_mapper.get_style_id(
                        style
                    )
                )

                if real_style_id:
                    self.set_style(

                        paragraph,

                        real_style_id

                    )



        self.writer.write(

            root,

            document_xml

        )





    def get_paragraph_style(
        self,
        paragraph
    ):

        """
        获取原段落样式ID

        """

        nodes = xpath(

            paragraph,

            "./w:pPr/w:pStyle"

        )


        if nodes:


            value = nodes[0].get(

                "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}val"

            )


            return value



        return None





    def set_style(
        self,
        paragraph,
        style_id
    ):

        """
        设置新的段落样式

        """



        pPr_nodes = xpath(

            paragraph,

            "./w:pPr"

        )


        if not pPr_nodes:


            return



        pPr = pPr_nodes[0]



        style_nodes = xpath(

            paragraph,

            "./w:pPr/w:pStyle"

        )



        if style_nodes:


            style_nodes[0].set(

                "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}val",

                style_id

            )


        else:


            # 如果原文没有pStyle

            # 创建一个


            from lxml import etree



            pStyle = etree.Element(

                "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}pStyle"

            )


            pStyle.set(

                "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}val",

                style_id

            )


            pPr.insert(

                0,

                pStyle

            )