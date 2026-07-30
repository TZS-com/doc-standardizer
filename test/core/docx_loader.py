import zipfile
import tempfile
import os


class DocxLoader:


    def extract(self, docx):

        folder=tempfile.mkdtemp()

        with zipfile.ZipFile(docx) as z:

            z.extractall(folder)


        return folder



    def pack(self, folder, output):


        with zipfile.ZipFile(
            output,
            "w",
            zipfile.ZIP_DEFLATED
        ) as z:


            for root,dirs,files in os.walk(folder):

                for file in files:

                    path=os.path.join(
                        root,
                        file
                    )


                    arc=path.replace(
                        folder,
                        ""
                    )


                    z.write(
                        path,
                        arc
                    )