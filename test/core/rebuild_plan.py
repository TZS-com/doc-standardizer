from core.models import Heading



class NumberEngine:


    def __init__(self):

        self.counter=[
            0,
            0,
            0,
            0
        ]



    def generate(
        self,
        level
    ):


        self.counter[level]+=1


        for i in range(
            level+1,
            4
        ):

            self.counter[i]=0



        return ".".join(

            str(x)

            for x in self.counter[
                :level+1
            ]

        )





def create_plan(headings):


    engine=NumberEngine()


    plan=[]


    for item in headings:


        number=engine.generate(

            item["level"]

        )


        item["new_number"]=number


        plan.append(
            item
        )


    return plan