class NumberingApply:
    """
    编号应用器
    """



    def apply(
        self,
        document,
        numbering
    ):


        document.numbering = numbering


        return document