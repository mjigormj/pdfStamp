package com.nice.assinatura;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PDFTextAndImageReplacer extends PDFTextStripper {
    private String searchText;
    private PDDocument document;
    private String imagePath;

    public PDFTextAndImageReplacer(String searchText, PDDocument document, String imagePath) throws IOException {
        this.searchText = searchText;
        this.document = document;
        this.imagePath = imagePath;
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        if (string.contains(searchText)) {
            var text = textPositions;
            float x = text.get(0).getXDirAdj();
            float y = text.get(0).getEndY();
            int pageIndex = getCurrentPageNo() - 1;

            addImageToPDF(pageIndex, x, y);
        }
    }

    private void addImageToPDF(int pageIndex, float x, float y) throws IOException {
        PDPage page = document.getPage(pageIndex);
        PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
            contentStream.drawImage(pdImage, x, y);
        }
    }

    public static void findTextAndAddImage(String pdfPath, String searchText, String imagePath) {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFTextAndImageReplacer replacer = new PDFTextAndImageReplacer(searchText, document, imagePath);
            replacer.setSortByPosition(true);
            replacer.setStartPage(0);
            replacer.setEndPage(document.getNumberOfPages());
            replacer.getText(document);
            document.save("output.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void findTextAndAddImageMultipleFiles(String directoryPath, String searchText, String imagePath) {
        var fileNames = listarArquivos(directoryPath);

        for (String file: fileNames) {
            try (PDDocument document = PDDocument.load(new File(directoryPath + file))) {
                PDFTextAndImageReplacer replacer = new PDFTextAndImageReplacer(searchText, document, imagePath);
                replacer.setSortByPosition(true);
                replacer.setStartPage(0);
                replacer.setEndPage(document.getNumberOfPages());
                replacer.getText(document);
                document.save(directoryPath + "\\saida\\" + file.replace(".pdf", "_assinado.pdf"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static List<String> listarArquivos(String directoryPath){
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        List<String> fileNames = new ArrayList<>();

        if(files != null){
            for(File file : files){
                if(file.isFile() && file.getName().toLowerCase().endsWith(".pdf")){
                    fileNames.add(file.getName());
                    log.info("..::Arquivo {} encontrado::..", file.getName());
                }
            }
        }

        return fileNames;
    }

    public static void main(String[] args) {
        String directoryPath = "";
        String searchText = "Buyer’s";
        String imagePath = "stamp.png";
        findTextAndAddImageMultipleFiles(directoryPath, searchText, imagePath);
    }

}