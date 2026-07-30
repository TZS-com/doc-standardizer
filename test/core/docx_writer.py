import zipfile
import os


def write_docx(
        source_dir,
        output
):


    with zipfile.ZipFile(
        output,
        "w",
        zipfile.ZIP_DEFLATED
    ) as z:


        for root,dirs,files in os.walk(
            source_dir
        ):

            for file in files:

                path=os.path.join(
                    root,
                    file
                )


                arc=path.replace(
                    source_dir,
                    ""
                )


                z.write(
                    path,
                    arc
                )