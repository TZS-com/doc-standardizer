"""
大模型调用接口

当前版本:
关闭

后续支持:
OpenAI
通义千问
DeepSeek
本地模型
"""


from config.settings import ENABLE_AI




class LLMClient:


    def __init__(
            self,
            api_key=None,
            model=None
    ):


        self.api_key = api_key

        self.model = model




    def chat(
            self,
            prompt
    ):

        """
        调用模型

        当前关闭
        """


        if not ENABLE_AI:


            return None



        # 后续接入

        # requests调用API

        return None