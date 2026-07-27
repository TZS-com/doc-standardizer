import os

import shutil




def create_dir(
    path
):

    """
    创建目录
    """


    if not os.path.exists(path):

        os.makedirs(
            path
        )





def copy_file(
    src,
    dst
):

    """
    文件复制
    """


    shutil.copy(
        src,
        dst
    )





def get_files(
    directory,
    extension=".docx"
):

    """
    获取指定目录文件
    """



    result = []



    for file in os.listdir(
        directory
    ):



        if file.endswith(
            extension
        ):


            result.append(
                os.path.join(
                    directory,
                    file
                )
            )



    return result





def remove_file(
    path
):

    """
    删除文件
    """

    if os.path.exists(path):

        os.remove(
            path
        )