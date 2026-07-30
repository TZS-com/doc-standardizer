import os

from core.docx_loader import DocxLoader

from core.document_rewriter import (
    rewrite_document
)

from core.docx_rebuilder import (
    rewrite_numbering
)



class RebuildEngine:


    def run(
        self,
        input_file,
        plan,
        output_file
    ):


        loader=DocxLoader()


        folder=loader.extract(
            input_file
        )


        document_xml=os.path.join(
            folder,
            "word/document.xml"
        )


        numbering_xml=os.path.join(
            folder,
            "word/numbering.xml"
        )


        count=rewrite_document(
            document_xml,
            plan
        )


        rewrite_numbering(
            numbering_xml
        )


        loader.pack(
            folder,
            output_file
        )


        return count