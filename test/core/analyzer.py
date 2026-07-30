from core.docx_reader import read_docx
from core.number_parser import parse_manual_number


def analyze_docx(path):

    paragraphs = read_docx(path)

    result = []


    for item in paragraphs:

        parsed = parse_manual_number(
            item["text"]
        )


        if parsed:

            result.append(
                {
                    "index": item["index"],
                    "text": parsed["title"],
                    "number": parsed["number"],
                    "level": parsed["level"],
                    "source": "manual"
                }
            )


    return result