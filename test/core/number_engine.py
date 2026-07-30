class NumberEngine:


    def __init__(self):

        self.count=[
            0,
            0,
            0,
            0
        ]



    def generate(self, level):


        self.count[level]+=1


        for i in range(
            level+1,
            4
        ):

            self.count[i]=0



        return ".".join(

            str(x)

            for x in self.count[:level+1]

        )