# Homework 3 - Ingegneria dei Dati
Motore di ricerca su documenti testuali implementato con Apache Lucene 9.10.0 in Java 17.

## Struttura del Progetto
- `src/main/java/Indexer.java`: indicizza i file `.txt` presenti in `data/txt_files/`.
- `src/main/java/Searcher.java`: interfaccia a riga di comando per interrogare l'indice.
- `data/txt_files/`: cartella contenente i documenti da indicizzare.
- `pom.xml`: configurazione di Apache Maven con dipendenze Lucene (core, analysis-common, queryparser).

## Istruzioni per l'esecuzione
1. **Compilazione**: `mvn clean compile`
2. **Indicizzazione**: Eseguire la classe `Indexer` per creare l'indice in `target/lucene-index`.
3. **Ricerca**: Eseguire la classe `Searcher` ed eseguire query usando i prefissi `nome:` o `contenuto:`.