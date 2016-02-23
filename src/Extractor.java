import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
public class Extractor {

    public static String URL_WIKIA = "http://gameofthrones.wikia.com/wiki/";
    public static String clean(String url) throws IOException
    {
        System.out.println("Extracting ... "+url);
        Document doc = Jsoup.connect(url).get();
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        doc.select("br").append("\\n");
        doc.select("p").prepend("\\n\\n");
        Elements wikiaArticle = doc.select("div#WikiaArticle");
        Elements relationShips = wikiaArticle.select("span#Relationships");
        //System.out.println(relationShips.html());

        relationShips.prepend("[[");
        relationShips.append("]]");

        //String s = doc.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(wikiaArticle.text().replaceAll("\\\\n", "\n")
                        .replaceAll("&nbsp;", "")
                        .replaceAll("\\[\\d+\\]", "")
                , "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    /**
     * Retourne les url par categories
     * @param categories : la liste des categories souhaite
     * @param limits la limite par categorie => limit = 2 <==> je veux les 2 premiere page de cette categorie
     * @return contient par categorie la liste des url vers les pages necessaire au corpus
     */
    public static HashMap<String, ArrayList<String>> getLinks(ArrayList<String> categories, ArrayList<Integer> limits) throws IOException {

        HashMap<String, ArrayList<String>> links = new HashMap<>();

        int cat = 0;
        for(String category : categories) {
            links.put(category,new ArrayList<>());
            for(int i = 1; i <= limits.get(cat); i++)
            {
                File html = new File("resources/"+"Category:"+category+"?page="+i);
                System.out.println("Opening ... "+html.getName());
                Document doc = Jsoup.parse(html, "UTF-8");
                doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
                doc.select("br").append("\\n");
                doc.select("p").prepend("\\n\\n");

                doc.select("div#mw-subcategories").remove(); // contient des category-gallery-item donc on supprime

                Elements div = doc.select("div.category-gallery");
                Elements galleryItem = div.select("div.category-gallery-item");

                for(Element elem : galleryItem)
                {
                    links.get(category).add(elem.children().get(0).attr("href"));
                }
            }
            cat++;
        }
        return links;
    }

    public static void main(String[] args) {

        ArrayList<String> categories = new ArrayList<>(Arrays.asList("Characters","Locations","Noble_houses"));
        ArrayList<Integer> limits = new ArrayList<>(Arrays.asList(58,21,10));
        try {
            HashMap<String, ArrayList<String>> links = Extractor.getLinks(categories,limits);
            for(Map.Entry<String, ArrayList<String>> entry : links.entrySet())
            {
                System.out.println("Category:"+entry.getKey());

                for(String s : entry.getValue())
                {
                    String res = Extractor.clean(s);
                    String fileName = s.replace(URL_WIKIA,"");
                    FileUtils.writeStringToFile(new File("corpus/not_structured/"+entry.getKey()+"/"+fileName+".txt"), res);
                }

                System.out.println(entry.getValue());
                System.out.println("______");
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


/*
String url = "lannister.html";
            String text = Extractor.clean("http://gameofthrones.wikia.com/wiki/House_Stark");
            //System.out.println(text);

            FileUtils.writeStringToFile(new File("test.txt"), text);

            System.out.println("End");
 */