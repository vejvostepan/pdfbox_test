import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.Main;
import org.example.PdfBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class PdfBuilderTest {

    @Test
    public void testPdf() throws IOException {

        ClassLoader cl = Main.class.getClassLoader();
        InputStream inputText = cl.getResourceAsStream("text.txt");
        InputStream inputImg = cl.getResourceAsStream("obrazek.png");

        PDDocument doc = new PdfBuilder()
                .addTextFormat(2.0f, 12, PDType1Font.HELVETICA)
                .addPaths(inputText, inputImg)
                .build();

        assertTrue( "Created PDF is empty.",doc.getNumberOfPages() > 0 && doc.getPage(0).getContents() != null);
    }
}
