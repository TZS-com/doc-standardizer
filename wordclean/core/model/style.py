class Style:
    """
    Word样式对象
    """



    def __init__(
        self,
        style_id=None
    ):


        self.style_id = style_id



        # 名称

        self.name = None



        # 字体

        self.font_name = None


        # 字号

        self.font_size = None



        # 加粗

        self.bold = False



        # 斜体

        self.italic = False



        # 颜色

        self.color = None



        # 段落格式

        self.paragraph_format = {}



    def update(
        self,
        key,
        value
    ):

        setattr(
            self,
            key,
            value
        )