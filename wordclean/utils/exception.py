class WordStandardizationError(
    Exception
):
    """
    基础异常
    """

    pass





class DocumentReadError(
    WordStandardizationError
):
    """
    Word读取异常
    """

    pass





class XMLParseError(
    WordStandardizationError
):
    """
    XML解析异常
    """

    pass





class StyleApplyError(
    WordStandardizationError
):
    """
    样式应用异常
    """

    pass