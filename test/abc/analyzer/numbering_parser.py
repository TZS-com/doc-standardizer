from zipfile import ZipFile
from lxml import etree


NS = {
    "w":
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"
}



def read_xml(docx, filename):

    with ZipFile(docx,"r") as z:

        data=z.read(filename)

    return etree.fromstring(data)





def parse_numbering(docx):

    """
    返回:

    {
       numId:
          {
             ilvl: level
          }
    }

    """

    root = read_xml(
        docx,
        "word/numbering.xml"
    )


    abstract_map={}


    # 解析abstractNum

    for abstract in root.xpath(
        "//w:abstractNum",
        namespaces=NS
    ):


        aid = abstract.get(
            "{%s}abstractNumId"
            %
            NS["w"]
        )


        levels={}


        for lvl in abstract.xpath(
            "./w:lvl",
            namespaces=NS
        ):


            ilvl=lvl.get(
                "{%s}ilvl"
                %
                NS["w"]
            )


            levels[int(ilvl)] = int(ilvl)



        abstract_map[aid]=levels




    result={}


    # numId关联abstractNum

    for num in root.xpath(
        "//w:num",
        namespaces=NS
    ):


        num_id=num.get(
            "{%s}numId"
            %
            NS["w"]
        )


        abstract=num.xpath(
            "./w:abstractNumId/@w:val",
            namespaces=NS
        )


        if abstract:

            result[int(num_id)] = abstract_map.get(
                abstract[0],
                {}
            )



    return result