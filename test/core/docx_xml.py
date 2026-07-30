import zipfile
import os
import tempfile


class DocxXML:


    def __init__(self, docx_path):

        self.docx_path = docx_path

        self.temp_dir = tempfile.mkdtemp()


    def extract(self):

        with zipfile.ZipFile(
            self.docx_path,
            "r"
        ) as z:

            z.extractall(
                self.temp_dir
            )


        return self.temp_dir