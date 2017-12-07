/* 
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */









package gr.forth.ics.isl.utilities.index;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 * Simple command-line based search demo.
 */
public class SearchFiles {

    private SearchFiles() {
    }

    /**
     * 139 * This demonstrates a typical paging search scenario, where the
     * search engine presents 140 * pages of size n to the user. The user can
     * then go to the next page if interested in 141 * the next hits. 142 * 143
     * * When the query is executed for the first time, then only enough results
     * are collected 144 * to fill 5 result pages. If the user wants to page
     * beyond this limit, then the query 145 * is executed another time and all
     * hits are collected. 146 * 147
     */
    //public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query,
    //      int hitsPerPage, boolean raw, boolean interactive) throws IOException {
    public static String getTopKhits(String query_text, int k) throws IOException, ParseException {
        String index = "src/main/resources/index";
        String field = "contents";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(query_text);

        TopDocs results = searcher.search(query, k);
        ScoreDoc[] hits = results.scoreDocs;

        if (hits.length == 0) {
            return null;
        }

        Document doc = searcher.doc(hits[0].doc);
        String filepath = doc.get("path");
        if (filepath != null) {
            System.out.println(doc.toString());
            System.out.println(filepath);
            String title = doc.get("title");
            if (title != null) {
                System.out.println("Title: " + doc.get("title"));
            }
        } else {
            System.out.println("No path for this document");
        }
        
        InputStream fis = new FileInputStream(filepath);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);

        String line;
        StringBuilder document = new StringBuilder();
        while((line=br.readLine())!=null){
            document.append(line).append("\n");
        }
        br.close();
        reader.close();

        return document.toString();

    }
}
