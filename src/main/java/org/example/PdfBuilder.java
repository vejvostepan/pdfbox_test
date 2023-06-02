package org.example;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class PdfBuilder {

    private TextFormat textFormat;
    private InputStream inputText;
    private InputStream inputImg;

    /* PUBLIC METHODS ----------------------------*/
    public PdfBuilder addTextFormat(float margin, int fontSize, PDFont font){
        textFormat = new TextFormat(margin,fontSize, font);
        return this;
    }

    public PdfBuilder addPaths(InputStream inputText, InputStream inputImg){
        this.inputText = inputText;
        this.inputImg = inputImg;
        return this;
    }

    public PDDocument build(){
        try {
            //Reading text
            String text =  new String(inputText.readAllBytes(), StandardCharsets.UTF_8);

            //Creating a document
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);

            //Reading image
            PDImageXObject image = PDImageXObject.createFromByteArray(doc, inputImg.readAllBytes(),null);

            //Setting formatting
            PDFont pdfFont = textFormat.getFont();
            float fontSize = textFormat.getFontSize();
            float leading = fontSize;

            //Setting margin of the document
            PDRectangle mb = page.getMediaBox();
            float margin = (textFormat.getMargin() / 2.54f) * 72;
            float width = mb.getWidth() - 2 * margin;
            float startX = mb.getLowerLeftX() + margin;
            float startY = mb.getUpperRightY() - margin - textFormat.getFontSize();

            List<String> paragraphs = seperateParagraphs(text, Optional.empty());

            //Setting font
            contentStream.setFont(pdfFont, fontSize);

            //Setting color
            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
            graphicsState.setNonStrokingAlphaConstant(0.3f);
            contentStream.setGraphicsStateParameters(graphicsState);

            Color highlightColor = Color.YELLOW;
            Color textColor = Color.BLUE;

            for (String paragraph : paragraphs) {

                //Indentation
                int indentation = 8;
                boolean isFirstLine = true;
                String intendationStr = StringUtils.repeat(" ", indentation);
                float intendationWidth = pdfFont.getStringWidth(intendationStr) / 1000 * fontSize;
                String indentedParagraph = intendationStr + paragraph;

                List<String> lines = seperateLines(indentedParagraph, width);


                for (String line : lines) {
                    // Calculate the width of the line
                    float lineWidth = pdfFont.getStringWidth(line) / 1000 * fontSize;

                    // Draw the highlighted background
                    contentStream.setNonStrokingColor(highlightColor);
                    if(isFirstLine){
                        contentStream.addRect(startX + intendationWidth, startY, lineWidth - intendationWidth, fontSize-2);
                        isFirstLine = false;
                    }else{
                        contentStream.addRect(startX, startY, lineWidth, fontSize-2);
                    }
                    contentStream.fill();

                    // Draw the text
                    contentStream.setNonStrokingColor(textColor);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(startX, startY);
                    contentStream.showText(line);
                    contentStream.endText();

                    // Move to the next line
                    startY -= leading;
                }

                // Move to the next paragraph
                startY -= leading;

                //Change color for next paragraph
                if(highlightColor == Color.YELLOW){
                    highlightColor = Color.GREEN;
                    textColor = Color.RED;
                }
                else if( highlightColor == Color.GREEN){
                    highlightColor = Color.YELLOW;
                    textColor = Color.BLUE;
                }
            }
            contentStream.close();

            //Draw images
            PDPageContentStream contentStreamImg = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
            PDExtendedGraphicsState graphicsStateImg = new PDExtendedGraphicsState();
            graphicsStateImg.setNonStrokingAlphaConstant(1.0f);
            contentStreamImg.setGraphicsStateParameters(graphicsStateImg);

            float dimension = (5.0f / 2.54f) * 72;
            contentStreamImg.drawImage(image, 0, 0, dimension, dimension);
            contentStreamImg.drawImage(image, mb.getWidth() - dimension, 0, dimension, dimension);


            contentStreamImg.close();
            return doc;

        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /* PRIVATE METHODS ---------------------------*/

    /**
     * This method splits string into a list of paragraphs based on given delimiter.
     * @param text Text to be split into paragraphs.
     * @param optionalDelimiter Delimiter for splitting text. Default value is "\n".
     * @return List of paragraphs.
     * */
    private List<String> seperateParagraphs(String text, Optional<String> optionalDelimiter){

        List<String> paragraphs = new ArrayList<>();
        String delimiter = optionalDelimiter.orElse("\n");

        String[] parts = text.split(delimiter);

        for (String part : parts) {
            part = part.trim();

            if(!part.isEmpty()){
                paragraphs.add(part);
            }
        }
        return paragraphs;
    }

    /**
     * This method splits paragraph into separate lines based on given width of document.
     * @param paragraph text string to be split
     * @param width width of the document (margins excluded)
     * @return list of lines
     * */
    private List<String> seperateLines(String paragraph, float width) throws IOException{

        String text = paragraph;
        List<String> lines = new ArrayList<String>();

        /*Iterates through paragraph string, finds next space and if the substring from start of string to the space
        * is smaller than wanted width, it continues to next space. If it is bigger then a new line is added to the list.*/
        int lastSpace = -1;
        while (text.length() > 0)
        {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);

            float size = textFormat.getFontSize() * textFormat.getFont().getStringWidth(subString) / 1000;

            if (size > width)
            {
                if (lastSpace < 0)
                    lastSpace = spaceIndex;
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            }
            else if (spaceIndex == text.length())
            {
                lines.add(text);
                text = "";
            }
            else
            {
                lastSpace = spaceIndex;
            }
        }

        return lines;
    }

}
