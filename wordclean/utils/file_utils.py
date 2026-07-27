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
    获取有效Word文件

    自动过滤:
    1. Word临时文件 ~$xxx.docx
    2. 非docx文件
    """



    result = []



    for file in os.listdir(directory):


        # 跳过Word临时文件

        if file.startswith(
            "~$"
        ):

            continue



        # 后缀判断

        if not file.lower().endswith(
            extension
        ):

            continue



        filepath = os.path.join(
            directory,
            file
        )


        result.append(
            filepath
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