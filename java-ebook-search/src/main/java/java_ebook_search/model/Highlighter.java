package java_ebook_search.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
* Class: Highlighter.java
* Credit: http://stackoverflow.com/questions/6544678/highlight-specific-word-in-the-html-page-using-jsoup
* 
* Edited by 
* @Author Kevin Paton
* @Since 17 Dec 2015
* 
*/

public class Highlighter {

    private String regex;
    private String htmlContent;
    private Pattern pat;
    private Matcher mat;
    
    /**
     * Counter for term frequency
     */
    private int termFrequency;


    public Highlighter(String searchString, String htmlString) {
        regex = buildRegexFromQuery(searchString);
        htmlContent = htmlString;
        pat = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public String getHighlightedHtml() {

        Document doc = Jsoup.parse(htmlContent);

        final List<TextNode> nodesToChange = new ArrayList<TextNode>();

        NodeTraversor nd  = new NodeTraversor(new NodeVisitor() {

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    String text = textNode.getWholeText();

                    mat = pat.matcher(text);

                    if(mat.find()) {
                        nodesToChange.add(textNode);
                    }
                }
            }

            @Override
            public void head(Node node, int depth) {        
            }
        });

        nd.traverse(doc.body());

        for (TextNode textNode : nodesToChange) {
            Node newNode = buildElementForText(textNode);
            textNode.replaceWith(newNode);
        }
        
        //set freuency of term
        termFrequency = nodesToChange.size();
        
        return doc.toString();
    }

    /**
     * Get how many words were highlighted.
     * @return
     */
    public int getTermFrequency(){
    	return termFrequency;
    }
    
    
    private static String buildRegexFromQuery(String queryString) {
        String regex = "";
        String queryToConvert = queryString;

        /* Clean up query */

        queryToConvert = queryToConvert.replaceAll("[\\p{Punct}]*", " ");
        queryToConvert = queryToConvert.replaceAll("[\\s]*", " ");

        String[] regexArray = queryString.split(" ");

        regex = "(";
        for(int i = 0; i < regexArray.length - 1; i++) {
            String item = regexArray[i];
            regex += "(\\b)" + item + "(\\b)|";
        }

        regex += "(\\b)" + regexArray[regexArray.length - 1] + "[a-zA-Z0-9]*?(\\b))";
        return regex;
    }

    private Node buildElementForText(TextNode textNode) {
        String text = textNode.getWholeText().trim();

        ArrayList<MatchedWord> matchedWordSet = new ArrayList<MatchedWord>();

        mat = pat.matcher(text);

        while(mat.find()) {
            matchedWordSet.add(new MatchedWord(mat.start(), mat.end()));
        }

        StringBuffer newText = new StringBuffer(text);

        for(int i = matchedWordSet.size() - 1; i >= 0; i-- ) {
            String wordToReplace = newText.substring(matchedWordSet.get(i).start, matchedWordSet.get(i).end);
            wordToReplace = "<mark>" + wordToReplace+ "</mark>";
            newText = newText.replace(matchedWordSet.get(i).start, matchedWordSet.get(i).end, wordToReplace);       
        }
        return new DataNode(newText.toString(), textNode.baseUri());
    }

    class MatchedWord {
        public int start;
        public int end;

        public MatchedWord(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}