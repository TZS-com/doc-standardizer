package com.company.doc.rule;


import com.company.doc.common.model.ParagraphNode;


public interface HeadingRule {


    boolean match(
            ParagraphNode node
    );


    int level();


}