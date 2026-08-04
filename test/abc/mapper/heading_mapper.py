import json
from pathlib import Path



class HeadingMapper:


    def __init__(
            self,
            mapping_file
    ):

        self.mapping_file = Path(
            mapping_file
        )

        self.mapping = self.load_mapping()



    def load_mapping(self):

        with open(
            self.mapping_file,
            "r",
            encoding="utf-8"
        ) as f:

            return json.load(f)



    def get_style(
            self,
            level
    ):

        if level is None:

            return None


        return self.mapping.get(
            str(level)
        )



    def map_outline(
            self,
            outline
    ):

        result=[]


        for item in outline:


            level=item.get(
                "level"
            )


            style=self.get_style(
                level
            )


            result.append({

                "index":
                    item.get("index"),


                "text":
                    item.get("text"),


                "level":
                    level,


                "source":
                    item.get("source"),


                "style":
                    style

            })


        return result