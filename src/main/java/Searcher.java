import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Searcher {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Directory directory = FSDirectory.open(Paths.get(Indexer.INDEX_DIR));
             IndexReader reader = DirectoryReader.open(directory);
             PerFieldAnalyzerWrapper analyzer = Indexer.getCustomAnalyzer()) {

            IndexSearcher searcher = new IndexSearcher(reader);
            StoredFields storedFields = reader.storedFields();

            System.out.println("==================================================");
            System.out.println("Motore di Ricerca Avviato.");
            System.out.println("Sintassi query supportata:");
            System.out.println("  nome: termine_o_\"frase esatta\"");
            System.out.println("  contenuto: termine_o_\"frase esatta\"");
            System.out.println("Digita 'exit' per uscire.");
            System.out.println("==================================================");

            while (true) {
                System.out.print("\nInserisci query > ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Chiusura programma.");
                    break;
                }
                if (input.isEmpty()) {
                    continue;
                }

                String field;
                String queryText;

                if (input.startsWith("nome:")) {
                    field = "filename";
                    queryText = input.substring("nome:".length()).trim();
                } else if (input.startsWith("contenuto:")) {
                    field = "content";
                    queryText = input.substring("contenuto:".length()).trim();
                } else {
                    System.out.println("Sintassi non valida! La query deve iniziare con 'nome:' oppure 'contenuto:'.");
                    continue;
                }

                if (queryText.isEmpty()) {
                    System.out.println("Specifica almeno un termine dopo il prefisso.");
                    continue;
                }

                try {
                    QueryParser parser = new QueryParser(field, analyzer);
                    Query query = parser.parse(queryText);

                    long startSearch = System.currentTimeMillis();
                    TopDocs hits = searcher.search(query, 10);
                    long searchTime = System.currentTimeMillis() - startSearch;

                    System.out.println("Risultati trovati: " + hits.totalHits.value + " in " + searchTime + " ms");
                    int rank = 1;
                    for (ScoreDoc scoreDoc : hits.scoreDocs) {
                        Document doc = storedFields.document(scoreDoc.doc);
                        System.out.printf("  [%d] Score: %.4f | File: %s\n", rank++, scoreDoc.score, doc.get("filename"));
                    }

                } catch (ParseException pe) {
                    System.out.println("Errore di sintassi nella query: " + pe.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Errore nell'apertura dell'indice: " + e.getMessage());
        }
    }
}