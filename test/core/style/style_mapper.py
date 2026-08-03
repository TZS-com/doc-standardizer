import json

from core.style.style_reader import read_styles



class StyleMapper:


    def __init__(
            self,
            template_docx,
            mapping_file
    ):


        self.styles = read_styles(
            template_docx
        )



        with open(
            mapping_file,
            "r",
            encoding="utf-8"
        ) as f:


            config = json.load(
                f
            )


        self.level_mapping = (
            config["level_style"]
        )



    def get_style(
            self,
            level
    ):


        key = str(level)



        style_name = (
            self.level_mapping.get(
                key,
                self.level_mapping["0"]
            )
        )



        style = self.styles.get(
            style_name
        )



        if style is None:

            raise Exception(
                f"模板不存在样式:{style_name}"
            )



        return {


            "level":level,


            "style_name":
                style_name,


            "style_id":
                style["style_id"]

        }



if __name__ == "__main__":


    mapper = StyleMapper(

        "template/standard.docx",

        "config/outline_style_mapping.json"

    )


    for i in range(5):

        print(
            mapper.get_style(i)
        )


