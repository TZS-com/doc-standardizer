import os
import re
from docx import Document
from openpyxl import Workbook

def clean_text(text):
    """清理文本中的换行和多余空格"""
    if not text:
        return ""
    return text.strip().replace('\n', ' ').replace('\r', ' ').replace('  ', ' ').strip()

def extract_resume(file_path, file_name):
    """精准提取简历信息（修复背景简介、语言能力的提取）"""
    doc = Document(file_path)
    result = {
        '姓名': '',
        '背景简介': '',
        '教育经历': '',
        '语言能力': '',
        '项目经历': []  # 每个元素: {'公司': '', '职位': '', '时间': '', '职责': ''}
    }

    # ----- 第一步：优先从文件名提取姓名（最稳妥） -----
    # 文件名示例：CV-C-黄冰洋202603.docx 或 CV-张三.docx
    name_from_file = re.sub(r'^CV[-C]+-', '', file_name)  # 去掉 CV-C- 或 CV-
    name_from_file = re.sub(r'20\d{4}\.docx$', '', name_from_file)  # 去掉 202603.docx
    name_from_file = re.sub(r'\.docx$', '', name_from_file)  # 去掉 .docx
    if name_from_file:
        result['姓名'] = name_from_file

    # 如果文件名没截取到（以防万一），再从段落里找
    if not result['姓名'] or len(result['姓名']) > 6:
        for para in doc.paragraphs:
            text = para.text.strip()
            if text and len(text) <= 6 and re.match(r'^[\u4e00-\u9fa5]{2,4}$', text):
                result['姓名'] = text
                break

    # ----- 第二步：提取背景简介（关键词"背景简介"，取整个单元格的全部文本） -----
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                cell_text = clean_text(cell.text)
                if '背景简介' in cell_text:
                    # 直接取整个单元格内容，并去掉"背景简介"标题
                    full_content = cell_text.replace('背景简介', '').strip()
                    # 如果内容为空，可能内容在相邻单元格（兼容其他样式）
                    if not full_content:
                        # 尝试取同一行的其他单元格
                        for other_cell in row.cells:
                            if other_cell != cell:
                                full_content = clean_text(other_cell.text)
                                if full_content:
                                    break
                    if full_content:
                        result['背景简介'] = full_content
                    break
            if result['背景简介']:
                break
        if result['背景简介']:
            break

    # ----- 第三步：提取教育经历（关键词"教育和培训"，取下一行） -----
    for table in doc.tables:
        rows = table.rows
        for i, row in enumerate(rows):
            row_text = ' '.join([clean_text(cell.text) for cell in row.cells])
            if '教育和培训' in row_text or ('教育' in row_text and '培训' in row_text):
                # 取下一行（内容通常在下一行）
                if i + 1 < len(rows):
                    next_row = rows[i + 1]
                    edu_text = ' '.join([clean_text(cell.text) for cell in next_row.cells])
                    if edu_text:
                        result['教育经历'] = edu_text
                        break
                # 如果下一行没有，尝试取当前行除标题外的其他单元格
                if not result['教育经历']:
                    for cell in row.cells:
                        cell_text = clean_text(cell.text)
                        if '教育' not in cell_text and '培训' not in cell_text and len(cell_text) > 5:
                            result['教育经历'] = cell_text
                            break
                break
        if result['教育经历']:
            break

    # ----- 第四步：提取语言能力（关键词"语言"，取当前行+下一行全部文本）-----
    for table in doc.tables:
        rows = table.rows
        for i, row in enumerate(rows):
            row_text = ' '.join([clean_text(cell.text) for cell in row.cells])
            if '语言' in row_text:
                lang_parts = []
                # 取当前行所有文本
                for cell in row.cells:
                    txt = clean_text(cell.text)
                    if txt and '语言' not in txt:
                        lang_parts.append(txt)
                # 取下一行所有文本（语言详情通常在下一行）
                if i + 1 < len(rows):
                    for cell in rows[i + 1].cells:
                        txt = clean_text(cell.text)
                        if txt:
                            lang_parts.append(txt)
                full_lang = ' '.join(lang_parts).strip()
                # 过滤掉纯粹的符号或空内容
                if full_lang and len(full_lang) > 1:
                    result['语言能力'] = full_lang
                    break
        if result['语言能力']:
            break

    # ----- 第五步：提取项目经历（精准匹配公司、职位、时间、职责） -----
    for table in doc.tables:
        rows = table.rows
        for row in rows:
            if len(row.cells) < 2:
                continue
            left = clean_text(row.cells[0].text)
            right = clean_text(row.cells[1].text)
            row_text = left + ' ' + right

            # 判断是否为项目经历行
            is_project = False
            if '项目经理' in row_text or '验证工程师' in row_text or '现场经理' in row_text or 'QC验证' in row_text:
                is_project = True
            elif '有限公司' in left or '研究所' in left or '生物' in left or '制药' in left:
                if re.search(r'\d{4}\.\d{2}', left):  # 左边包含时间
                    is_project = True

            if is_project:
                # 提取时间
                time_match = re.search(r'(\d{4}\.\d{2}\s*[-–]\s*\d{4}\.\d{2}|\d{4}\.\d{2}\s*[-–]\s*今)', left)
                time = time_match.group(1) if time_match else ''

                # 提取公司名（去掉时间）
                company = left.replace(time, '').strip()
                company = re.sub(r'[＊*]', '', company).strip()

                # 提取职位（从右侧文本中取第一个出现的职位词）
                position = ''
                pos_match = re.search(r'(项目经理|验证工程师|现场经理|QC验证)', right)
                if pos_match:
                    position = pos_match.group(1)

                # 提取职责（去掉职位词，并清理开头的冒号/空格）
                duties = right
                if position and duties.startswith(position):
                    duties = duties[len(position):].strip()
                # 如果职责以冒号或破折号开头，去掉
                duties = re.sub(r'^[:：\-–]\s*', '', duties)

                # 过滤掉无效行（如只有公司名没有职责，或内容是"2012.6~2017.2"这种纯时间）
                if company and len(company) > 1 and duties and len(duties) > 2:
                    result['项目经历'].append({
                        '公司': company,
                        '职位': position,
                        '时间': time,
                        '职责': duties
                    })

    # 如果项目经历提取得太少，试试放宽条件（针对纯文本列表）
    if len(result['项目经历']) < 3:
        for table in doc.tables:
            for row in table.rows:
                if len(row.cells) < 2:
                    continue
                left = clean_text(row.cells[0].text)
                right = clean_text(row.cells[1].text)
                # 如果左边有明确的时间和公司特征，右边描述较长
                if re.search(r'\d{4}\.\d{2}', left) and len(right) > 20 and '负责' in right:
                    time_match = re.search(r'(\d{4}\.\d{2}\s*[-–]\s*\d{4}\.\d{2}|\d{4}\.\d{2}\s*[-–]\s*今)', left)
                    time = time_match.group(1) if time_match else ''
                    company = left.replace(time, '').strip()
                    company = re.sub(r'[＊*]', '', company).strip()
                    if company and len(company) > 1:
                        result['项目经历'].append({
                            '公司': company,
                            '职位': '',
                            '时间': time,
                            '职责': right
                        })

    return result


def batch_extract(folder_path, output_excel):
    """批量提取文件夹内所有docx"""
    wb = Workbook()
    ws = wb.active
    ws.title = "简历数据"

    # 表头（教育经历排在最前面）
    headers = ['姓名', '教育经历', '背景简介', '语言能力',
               '项目经历_公司', '项目经历_职位', '项目经历_时间', '项目经历_职责']
    ws.append(headers)

    files = [f for f in os.listdir(folder_path) if f.endswith('.docx')]

    if not files:
        print("❌ 文件夹中没有找到 .docx 文件，请检查路径！")
        return

    print(f"📁 找到 {len(files)} 个简历文件，开始提取...")

    for file in files:
        print(f"  处理中: {file}")
        file_path = os.path.join(folder_path, file)
        try:
            data = extract_resume(file_path, file)

            # 如果姓名还是没提取到，用文件名兜底
            if not data['姓名']:
                data['姓名'] = file.replace('.docx', '')

            # 每个项目经历占一行（方便后续筛选）
            if data['项目经历']:
                for proj in data['项目经历']:
                    row = [
                        data['姓名'],
                        data['教育经历'],
                        data['背景简介'],
                        data['语言能力'],
                        proj['公司'],
                        proj['职位'],
                        proj['时间'],
                        proj['职责']
                    ]
                    ws.append(row)
            else:
                # 完全没有项目经历的，也保留一行基本信息
                row = [data['姓名'], data['教育经历'], data['背景简介'], data['语言能力'], '', '', '', '']
                ws.append(row)

        except Exception as e:
            print(f"  ⚠️ 处理 {file} 时出错: {e}")
            continue

    wb.save(output_excel)
    print(f"\n✅ 提取完成！共处理 {len(files)} 个文件")
    print(f"📄 Excel保存至: {output_excel}")


if __name__ == '__main__':
    # =============================================
    # 请务必修改下方两个路径为你的实际路径！
    # =============================================
    folder = r"D:\效率提升自动化\jianli\a"   # 存放30份docx的文件夹
    output = r"D:\效率提升自动化\jianli\简历数据汇总.xlsx"
    # =============================================

    batch_extract(folder, output)