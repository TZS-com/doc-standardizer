import json
import os

from core.analyzer import analyze_docx


def main():

    os.makedirs(
        "output",
        exist_ok=True
    )


    result = analyze_docx(
        "input/test.docx"
    )


    with open(
        "output/result.json",
        "w",
        encoding="utf-8"
    ) as f:

        json.dump(
            result,
            f,
            ensure_ascii=False,
            indent=4
        )


if __name__ == "__main__":
    main()