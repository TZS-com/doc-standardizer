from config.title_rules import get_title_level



class HeadingClassifier:
    """
    标题识别器
    """



    def classify(
        self,
        paragraph
    ):


        level = get_title_level(
            paragraph.text
        )



        if level:


            paragraph.set_heading_level(
                level
            )



        return level