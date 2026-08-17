"""Add the company-standard table style to a Word template."""

from __future__ import annotations

import argparse
import re
import zipfile
from pathlib import Path


STYLE_ID = "CompanyStandardTable"
STYLE_XML = f'''<w:style w:type="table" w:customStyle="1" w:styleId="{STYLE_ID}">
  <w:name w:val="公司标准表格"/>
  <w:basedOn w:val="a3"/>
  <w:uiPriority w:val="99"/>
  <w:qFormat/>
  <w:tblPr>
    <w:tblW w:w="5000" w:type="pct"/>
    <w:jc w:val="center"/>
    <w:tblBorders>
      <w:top w:val="single" w:sz="4" w:space="0" w:color="000000"/>
      <w:left w:val="single" w:sz="4" w:space="0" w:color="000000"/>
      <w:bottom w:val="single" w:sz="4" w:space="0" w:color="000000"/>
      <w:right w:val="single" w:sz="4" w:space="0" w:color="000000"/>
      <w:insideH w:val="single" w:sz="4" w:space="0" w:color="000000"/>
      <w:insideV w:val="single" w:sz="4" w:space="0" w:color="000000"/>
    </w:tblBorders>
  </w:tblPr>
  <w:trPr>
    <w:trHeight w:val="567" w:hRule="atLeast"/>
  </w:trPr>
  <w:tcPr>
    <w:vAlign w:val="center"/>
  </w:tcPr>
  <w:tblStylePr w:type="firstRow">
    <w:trPr><w:tblHeader/></w:trPr>
    <w:tcPr>
      <w:shd w:val="clear" w:color="auto" w:themeFill="background1" w:themeFillShade="D9"/>
    </w:tcPr>
  </w:tblStylePr>
</w:style>'''


def update_styles(styles_xml: str) -> str:
    pattern = re.compile(
        rf'(?s)<w:style\b(?=[^>]*\bw:styleId="{STYLE_ID}")[^>]*>.*?</w:style>\s*'
    )
    styles_xml = pattern.sub("", styles_xml)
    if "</w:styles>" not in styles_xml:
        raise ValueError("word/styles.xml has no closing w:styles element")
    return styles_xml.replace("</w:styles>", STYLE_XML + "\n</w:styles>", 1)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("input", type=Path)
    parser.add_argument("--out", required=True, type=Path)
    args = parser.parse_args()

    with zipfile.ZipFile(args.input) as source, zipfile.ZipFile(args.out, "w") as target:
        found_styles = False
        for entry in source.infolist():
            data = source.read(entry.filename)
            if entry.filename == "word/styles.xml":
                data = update_styles(data.decode("utf-8")).encode("utf-8")
                found_styles = True
            target.writestr(entry, data)
    if not found_styles:
        raise ValueError("template contains no word/styles.xml")


if __name__ == "__main__":
    main()
