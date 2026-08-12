package com.company.doc.common.enums;


public enum DocumentLevel {


    TITLE_ONE(1),

    TITLE_TWO(2),

    TITLE_THREE(3),

    /** Unordered (bullet) first-level item; intentionally distinct from an ordered title. */
    NONE_TITLE_ONE(1),

    NORMAL(0);


    private final int level;


    DocumentLevel(int level){

        this.level = level;

    }


    public int getLevel(){

        return level;

    }

}
