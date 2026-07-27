class Paragraph:
    """
    Word段落对象
    """



    def __init__(
        self,
        text=""
    ):


        self.text = text


        # 原始样式

        self.style = None


        # 标准样式

        self.target_style = None



        # 标题级别

        self.heading_level = None



        # 章节

        self.section = None



        # 字体信息

        self.font = {}



        # 段落属性

        self.format = {}



    def set_style(
        self,
        style
    ):

        self.style = style



    def set_heading_level(
        self,
        level
    ):

        self.heading_level = level



    def set_section(
        self,
        section
    ):

        self.section = section



    def is_heading(self):

        return (
            self.heading_level
            is not None
        )