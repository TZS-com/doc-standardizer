from config.section_rules import match_section



class SectionClassifier:
    """
    章节识别器
    """



    def classify(
        self,
        paragraph
    ):


        if not paragraph.is_heading():

            return None



        section = match_section(
            paragraph.text
        )



        paragraph.set_section(
            section
        )



        return section