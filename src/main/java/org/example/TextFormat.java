package org.example;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class TextFormat {

    // Margin of the document in cm
    private float margin;
    private int fontSize;
    private PDFont font;

    /* GETTERS -------------------------------------------*/
    public float getMargin() {
        return margin;
    }

    public int getFontSize() {
        return fontSize;
    }

    public PDFont getFont() {
        return font;
    }

    /*CONSTRUCTORS ---------------------------------------*/
    TextFormat(float margin, int fontSize, PDFont font){
        this.margin = margin;
        this.fontSize = fontSize;
        this.font = font;
    }
}
