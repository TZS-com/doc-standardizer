class StyleApplier:
    """
    样式应用器
    """



    def apply(
        self,
        document,
        style_mapping
    ):

        """
        应用样式
        """



        for paragraph in document.paragraphs:


            source = paragraph.style



            if source in style_mapping:


                paragraph.target_style = (
                    style_mapping[source]
                )



        return document