import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;



public class FileWritter {
    public static void save(BufferedImage i1){
        JFrame pf = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Microsoft Word", "doc"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Portable Document Format", "pdf"));
        fileChooser.setSelectedFile(new File("Graph.doc"));
        fileChooser.setDialogTitle("Выберите путь");
        int userSelection = fileChooser.showSaveDialog(pf);
        //fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.pdf","pdf"));
        //fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.doc","doc"));
        //FileNameExtensionFilter filter = new FileNameExtensionFilter("??????","pdf","doc");
        // fileChooser.setFileFilter(filter);
        if (userSelection == JFileChooser.APPROVE_OPTION){
            File fileToSave = fileChooser.getSelectedFile();
            System.out.println("save as " + fileToSave);
            int last = fileToSave.toString().split("\\.").length;
            if (fileToSave.toString().split("\\.")[last - 1].equals("doc")){
                String currentDir = System.getProperty("user.dir");
                String dest = fileToSave.getAbsolutePath();
                XWPFDocument doc = new XWPFDocument();
                XWPFParagraph paragraph = doc.createParagraph();
                XWPFRun run = paragraph.createRun();
                try {
                    FileOutputStream out = new FileOutputStream(new File(fileToSave.getAbsolutePath()));
                    run.setFontFamily("Times New Roman");
                    run.setFontSize(12);

                    ImageIO.write(i1,"png", new File(currentDir + "\\1.png"));
                    InputStream pic1 = new FileInputStream(currentDir +"\\1.png");
                    System.out.println(pic1);
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                    run.addPicture(pic1,XWPFDocument.PICTURE_TYPE_PNG,currentDir + "\\1.png", Units.toEMU(400),Units.toEMU(350));
                    pic1.close();
                    run.addBreak();

                    doc.write(out);
                    out.flush();
                    out.close();
                } catch (IOException | InvalidFormatException e) {
                }

            }

            if (fileToSave.toString().split("\\.")[last - 1].equals("pdf")){
                try {
                    String currentDir = System.getProperty("user.dir");

                    File out1 = new File( currentDir + "\\1.png");
                    ImageIO.write(i1,"png",new File(out1.toString()));

                    String dest = fileToSave.getAbsolutePath();
                    System.out.println(fileToSave.getAbsolutePath());

                    com.itextpdf.text.Document  document = new com.itextpdf.text.Document(new com.itextpdf.text.Rectangle(595,842));
                    PdfWriter.getInstance(document,new FileOutputStream(dest));
                    System.out.println(currentDir + "\\fonts\\times.ttf");
                    BaseFont bf = BaseFont.createFont(currentDir + "\\fonts\\times.ttf",BaseFont.IDENTITY_H,BaseFont.EMBEDDED);

                    Font font = new Font(bf,14,Font.NORMAL);

                    document.open();

                    ImageData data1 = ImageDataFactory.create(out1.toString());
                    com.itextpdf.text.Image image1 = com.itextpdf.text.Image.getInstance(out1.toString());
                    image1.scaleToFit(500,700);
                    document.add(image1);
                    document.close();
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}