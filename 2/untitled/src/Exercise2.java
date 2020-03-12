import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.PhoneExtractingContentHandler;
import org.xml.sax.SAXException;

public class Exercise2
{

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private OptimaizeLangDetector langDetector;

    public static void main(String[] args)
    {
        Exercise2 exercise = new Exercise2();
        exercise.run();
    }

    private void run()
    {
        try
        {
            if (!new File("./outputDocuments").exists())
            {
                Files.createDirectory(Paths.get("./outputDocuments"));
            }

            initLangDetector();

            File directory = new File("./documents");
            File[] files = directory.listFiles();
            for (File file : files)
            {
                processFile(file);
            }
        } catch (IOException | SAXException | TikaException e)
        {
            e.printStackTrace();
        }

    }

    private void initLangDetector() throws IOException
    {
        langDetector = (OptimaizeLangDetector) new OptimaizeLangDetector().loadModels();
    }

    private void processFile(File file) throws IOException, SAXException, TikaException
    {
        // call saveResult method to save the data
        String name = file.getName();
        System.out.println("Processing file " + name);
        LinkedList <String> results = new LinkedList <>();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        InputStream stream = new FileInputStream("./documents/" + name);
        //Thanks to "-1" we can parse as big documents as possible
        BodyContentHandler handler = new BodyContentHandler(-1);
        parser.parse(stream, handler, metadata);

        String creator = metadata.get("meta:author");
        Date lastModified = metadata.getDate(TikaCoreProperties.MODIFIED);
        Date creationDate = metadata.getDate(TikaCoreProperties.CREATED);
        String mimeType = metadata.get("Content-Type");
        String content = handler.toString();

        LanguageResult result = langDetector.detect(content);
        String lang = result.getLanguage();

        saveResult(name, lang, creator, creationDate, lastModified, mimeType, content);
    }


    private void saveResult(String fileName, String language, String creatorName, Date creationDate,
                            Date lastModification, String mimeType, String content)
    {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        int index = fileName.lastIndexOf(".");
        String outName = fileName.substring(0, index) + ".txt";
        try
        {
            PrintWriter printWriter = new PrintWriter("./outputDocuments/" + outName);
            printWriter.write("Name: " + fileName + "\n");
            printWriter.write("Language: " + (language != null ? language : "") + "\n");
            printWriter.write("Creator: " + (creatorName != null ? creatorName : "") + "\n");
            String creationDateStr = creationDate == null ? "" : dateFormat.format(creationDate);
            printWriter.write("Creation date: " + creationDateStr + "\n");
            String lastModificationStr = lastModification == null ? "" : dateFormat.format(lastModification);
            printWriter.write("Last modification: " + lastModificationStr + "\n");
            printWriter.write("MIME type: " + (mimeType != null ? mimeType : "") + "\n");
            printWriter.write("\n");
            printWriter.write(content + "\n");
            printWriter.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

}