from lxml import etree

from .numbering_reader import (
    NS,
    NumberingReader
)



class NumberingInspector:


    def __init__(
            self,
            docx_dir
    ):

        self.document = etree.parse(

            docx_dir
            +
            "/word/document.xml"

        )


        self.numbering = NumberingReader(

            docx_dir
            +
            "/word/numbering.xml"

        )


    def analyze(self):


        result=[]


        num_map = (
            self.numbering
            .get_num_map()
        )


        paragraphs = self.document.xpath(

            "//w:body/w:p",

            namespaces=NS

        )


        for index,p in enumerate(paragraphs):


            texts = p.xpath(

                ".//w:t/text()",

                namespaces=NS

            )


            text="".join(texts).strip()


            if not text:

                continue



            num_id_nodes=p.xpath(

                "./w:pPr/w:numPr/w:numId/@w:val",

                namespaces=NS

            )


            ilvl_nodes=p.xpath(

                "./w:pPr/w:numPr/w:ilvl/@w:val",

                namespaces=NS

            )


            item={

                "index":index,

                "text":text,

                "numId":None,

                "level":None,

                "format":None

            }



            if num_id_nodes:


                num_id=num_id_nodes[0]

                level=int(
                    ilvl_nodes[0]
                ) if ilvl_nodes else 0


                item["numId"]=num_id

                item["level"]=level



                abstract_id=num_map.get(
                    num_id
                )


                if abstract_id:


                    item["format"]=(
                        self.numbering
                        .get_level_format(
                            abstract_id,
                            level
                        )
                    )



            result.append(item)



        return result