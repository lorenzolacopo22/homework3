import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Indexer {

    public static final String INDEX_DIR = "target/lucene-index";
    public static final String DOCS_DIR = "data/txt_files";

    public static PerFieldAnalyzerWrapper getCustomAnalyzer() {
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        perFieldAnalyzers.put("filename", new WhitespaceAnalyzer());
        Analyzer defaultAnalyzer = new ItalianAnalyzer();
        return new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
    }

    public static void main(String[] args) {
        Path indexPath = Paths.get(INDEX_DIR);
        Path docsPath = Paths.get(DOCS_DIR).toAbsolutePath().normalize();

        System.out.println("Cerco i file nella cartella: " + docsPath);

        File dir = docsPath.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Cartella non trovata!");
            return;
        }

        File[] allFiles = dir.listFiles();
        System.out.println("Numero elementi trovati dentro la cartella: " + (allFiles != null ? allFiles.length : "null"));
        if (allFiles != null) {
            for (File f : allFiles) {
                System.out.println(" -> Elemento presente: [" + f.getName() + "] (e' file? " + f.isFile() + ")");
            }
        }

        try (Directory directory = FSDirectory.open(indexPath);
             PerFieldAnalyzerWrapper analyzer = getCustomAnalyzer()) {

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            long startTime = System.currentTimeMillis();
            int fileCount = 0;

            try (IndexWriter writer = new IndexWriter(directory, config)) {
                if (allFiles != null) {
                    for (File f : allFiles) {
                        if (f.isFile()) {
                            System.out.println("Indicizzo: " + f.getName());
                            Document doc = new Document();
                            doc.add(new TextField("filename", f.getName(), Field.Store.YES));
                            doc.add(new StringField("path", f.getAbsolutePath(), Field.Store.YES));

                            String textContent = Files.readString(f.toPath());
                            doc.add(new TextField("content", textContent, Field.Store.NO));

                            writer.addDocument(doc);
                            fileCount++;
                        }
                    }
                }
                writer.commit();
            }

            long endTime = System.currentTimeMillis();
            System.out.println("=== INDICIZZAZIONE COMPLETATA ===");
            System.out.println("File indicizzati: " + fileCount);
            System.out.println("Tempo impiegato: " + (endTime - startTime) + " ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}