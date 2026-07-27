import os
import zipfile



class DocxPacker:
    """
    docx重新打包
    """



    def pack(
        self,
        source_dir,
        output_file
    ):

        """
        source_dir:

            解压后的docx目录


        output_file:

            输出docx

        """



        if os.path.exists(
            output_file
        ):

            os.remove(
                output_file
            )



        with zipfile.ZipFile(
            output_file,
            "w",
            zipfile.ZIP_DEFLATED
        ) as zip_file:



            for root, dirs, files in os.walk(
                source_dir
            ):



                for file in files:


                    filepath=os.path.join(
                        root,
                        file
                    )



                    arcname=os.path.relpath(
                        filepath,
                        source_dir
                    )



                    zip_file.write(
                        filepath,
                        arcname
                    )



        return output_file