"""
AI辅助结构分析
"""


from core.ai.llm_client import LLMClient




class StructureParser:



    def __init__(self):


        self.client = LLMClient()



    def analyze(
            self,
            text
    ):


        prompt = f"""

分析下面文档内容结构:

{text}

返回标题级别和类型

"""


        result = self.client.chat(

            prompt

        )


        return result