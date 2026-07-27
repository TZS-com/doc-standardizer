import re


class HeadingClassifier:
    """
    标题识别器

    作用：

    根据段落信息判断：

    Heading1
    Heading2
    Heading3

    支持：

    1. 原文已有标题样式
    2. 原文纯文本编号标题
    3. 验证文件常见章节格式

    """


    def classify(
        self,
        text,
        old_style=None
    ):
        """
        判断标题等级


        参数:

        text:
            段落文本


        old_style:
            原Word中的样式ID


        返回:

            Heading1
            Heading2
            Heading3
            None

        """

        if not text:

            return None


        text = text.strip()


        if len(text) == 0:

            return None



        # =================================
        # 第一优先级
        # 判断原文已有标题样式
        # =================================

        if old_style:


            style = old_style.lower()


            if (
                "heading1" in style
                or
                "heading 1" in style
                or
                "标题1" in style
                or
                "标题 1" in style
            ):

                return "Heading1"



            if (
                "heading2" in style
                or
                "heading 2" in style
                or
                "标题2" in style
                or
                "标题 2" in style
            ):

                return "Heading2"



            if (
                "heading3" in style
                or
                "heading 3" in style
                or
                "标题3" in style
                or
                "标题 3" in style
            ):

                return "Heading3"



        # =================================
        # 第二优先级
        # 根据文本编号判断
        # =================================


        # -----------------------------
        # 一级标题
        #
        # 示例:
        #
        # 1 目的
        # 2 范围
        #
        # 一、目的
        # 二、范围
        #
        # 1. 目的
        #
        # -----------------------------


        level1_patterns = [

            r"^[一二三四五六七八九十]+、.+",

            r"^\d+\s+.+",

            r"^\d+\.\s*[^0-9].+"

        ]


        for pattern in level1_patterns:


            if re.match(
                pattern,
                text
            ):

                return "Heading1"




        # -----------------------------
        # 二级标题
        #
        # 示例:
        #
        # 1.1 验证范围
        #
        # -----------------------------


        if re.match(

            r"^\d+\.\d+\s*.+",

            text

        ):

            return "Heading2"



        # -----------------------------
        # 三级标题
        #
        # 示例:
        #
        # 1.1.1 验证方法
        #
        # -----------------------------


        if re.match(

            r"^\d+\.\d+\.\d+\s*.+",

            text

        ):

            return "Heading3"



        return None