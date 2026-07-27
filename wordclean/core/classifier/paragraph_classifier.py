class ParagraphClassifier:
    """
    段落分类器
    """



    def classify(
        self,
        paragraph
    ):

        """
        判断段落类型
        """



        text = paragraph.text.strip()



        if not text:


            paragraph.type = "empty"



        else:


            paragraph.type = "text"



        return paragraph.type