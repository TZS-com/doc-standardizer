package com.company.doc.common.model;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * Word文档模型
 */
@Data
public class DocumentModel {


    /**
     * 文档所有段落
     */
    private List<ParagraphNode> paragraphs =
            new ArrayList<>();


}