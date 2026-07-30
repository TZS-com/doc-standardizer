from lxml import etree


W_NS = (
    "http://schemas.openxmlformats.org/"
    "wordprocessingml/2006/main"
)


def w(tag):

    return "{%s}%s" % (
        W_NS,
        tag
    )



def build_numbering():


    abstract = etree.Element(
        w("abstractNum"),
        {
            w("abstractNumId"): "999"
        }
    )


    # 声明多级编号

    multi = etree.SubElement(
        abstract,
        w("multiLevelType")
    )

    multi.set(
        w("val"),
        "multilevel"
    )



    for level in range(4):


        lvl = etree.SubElement(
            abstract,
            w("lvl"),
            {
                w("ilvl"): str(level)
            }
        )


        start = etree.SubElement(
            lvl,
            w("start")
        )

        start.set(
            w("val"),
            "1"
        )


        numFmt = etree.SubElement(
            lvl,
            w("numFmt")
        )

        numFmt.set(
            w("val"),
            "decimal"
        )


        lvlText = etree.SubElement(
            lvl,
            w("lvlText")
        )


        lvlText.set(
            w("val"),
            ".".join(
                [
                    "%" + str(i+1)
                    for i in range(level+1)
                ]
            )
        )


        lvlJc = etree.SubElement(
            lvl,
            w("lvlJc")
        )

        lvlJc.set(
            w("val"),
            "left"
        )


        # 段落格式

        pPr = etree.SubElement(
            lvl,
            w("pPr")
        )


        ind = etree.SubElement(
            pPr,
            w("ind")
        )

        ind.set(
            w("left"),
            str(
                720*(level+1)
            )
        )

        ind.set(
            w("hanging"),
            "360"
        )



    num = etree.Element(
        w("num"),
        {
            w("numId"): "999"
        }
    )


    abstractNumId = etree.SubElement(
        num,
        w("abstractNumId")
    )

    abstractNumId.set(
        w("val"),
        "999"
    )


    return abstract, num