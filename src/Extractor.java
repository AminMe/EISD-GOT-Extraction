import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
public class Extractor {

    public static String URL_WIKIA = "http://gameofthrones.wikia.com/wiki/";
    public static String extractNotStructured(String url) throws IOException
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

    public static String extractStructured(String url) throws IOException {
        System.out.println("Extracting ... "+url);
        Document doc = Jsoup.connect(url).get();
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        doc.select("br").append("\\n");
        doc.select("p").prepend("\\n\\n");
        Elements editarea = doc.select("div#editarea");

        if(editarea.html().isEmpty())
        {
            return extractStructuredEditNotSource(doc);
        }
        else
        {
            return extractStructuredEditSource(doc);
        }

    }

    /**
     * Cas ou le source n'est pas en clair mais avec du code %16
     * ex : view-source:http://gameofthrones.wikia.com/wiki/Bran_Stark?action=edit
     * @param doc
     * @return
     */
    private static String extractStructuredEditSource(Document doc)
    {
        System.out.println("Je suis dans le cas Bran");
        Elements editarea = doc.select("div#editarea");

        return editarea.text();
    }

    /**
     * Cas ou le source est deja pret
     * ex : view-source:http://gameofthrones.wikia.com/wiki/Daenerys_Targaryen?action=edit
     * @param doc
     * @return
     */
    private static String extractStructuredEditNotSource(Document doc)
    {
        //TODO eux juste retirer HTML ??
        System.out.println("Je suis dans le cas Daenerys");
        Elements textarea = doc.select("textarea#wpTextbox1");
        return textarea.text();
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

    public static void relationshipsRecursif(Element ulli, int profondeur, ArrayList<String> relationships)
    {
        if(ulli.children().size()==0)
        {
            //System.out.println("Profondeur fin " + profondeur + " - " + ulli.text());
            relationships.add(generateStars(profondeur)+" "+ulli.text());
            return;
        }
        if(ulli.tagName().equals("ul"))
        {
            profondeur++;
            for(int i = 0; i<ulli.children().size(); i++)
            {
                relationshipsRecursif(ulli.child(i),profondeur,relationships);
            }
        }
        else if(ulli.tagName().equals("li"))
        {
            String myContent = "";
            for(int i = 0; i<ulli.children().size(); i++)
            {
                if(ulli.child(i).tagName().equals("a"))
                {
                    Element tmp = ulli.clone();
                    tmp.select("ul").remove();

                    if(myContent.isEmpty())
                    {
                        relationships.add(generateStars(profondeur)+" "+tmp.select("li").first().text());
                        myContent += tmp.select("li").first().text();
                    }
                }
                else {
                    relationshipsRecursif(ulli.child(i),profondeur,relationships);
                    break;
                }
            }
            //System.out.println(generateStars(profondeur)+" "+myContent);
        }

        //System.out.println("\n");
    }

    public static String generateStars(int howMuch)
    {
        if(howMuch<=0)
            return "";
        String stars = "";

        for(int i = 0; i<howMuch; i++)
        {
            stars+="*";
        }

        return stars;
    }

    /**
     * Gere le cas des fichiers ou le source est deja en bon format
     * @return
     */
    public static String handleFileSourceEasy(Document doc)
    {
        return Jsoup.clean(doc.toString().replaceAll("\\\\n", "\n")
            , "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    /**
     * Gere le cas des fichiers ou le sources n'est pas dans le bon
     * format
     * @param doc
     * @return
     */
    public static String handleFileSourceHard(Document doc)
    {
        Pattern pattern;
        Matcher matcher;
        String regex = "\\|\\s*([t|T]itle.*?)\\s*data";
        //String regex = "\\|";


        if(!doc.select("p").isEmpty())
        {
            //System.out.println("============> [1]");
            //System.out.println(doc.select("p").get(1));
            pattern = Pattern.compile(regex);
            //System.out.println(doc.select("p").toString());


            //System.out.println(doc.select("p").get(1).toString());
            matcher = pattern.matcher(doc.select("p").toString());
            //System.out.println("Matcher.find() : "+matcher.find());
            if(matcher.find())
            {
                // System.out.println("Group " +matcher.group(1)); // Prints String I want to extract

                Elements h2 = doc.select("h2");

                Element h2RelationShips = null;

                for(Element elem : h2)
                {
                    if(elem.text().equals("Relationships"))
                    {
                        h2RelationShips = elem;
                    }
                }

                String relationshipsS = "";
                if(h2RelationShips!=null)
                {
                    //System.out.println("h2 : "+h2RelationShips);
                    Element ul = h2RelationShips.nextElementSibling();
                    while(!ul.tagName().equals("ul"))
                    {
                        ul = ul.nextElementSibling();
                    }
                    //System.out.println(ul);
                    ArrayList<String> relationships = new ArrayList<>();
                    relationshipsRecursif(ul,0,relationships);


                    for (String r : relationships)
                    {
                        relationshipsS+=r+"\n";
                    }
                }



                doc.select("br").append("\\n");
                doc.select("p").prepend("\\n\\n");
                String docString = Jsoup.clean(doc.text().replaceAll("\\\\n", "\n")
                        , "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
                docString = docString
                        .replaceAll("\\&nbsp;", "")
                        .replaceAll("\\{*\\[\\[", "")
                        .replaceAll("\\]\\]\\}*", "");
                String update = matcher.group(1) + relationshipsS +  docString;
                //System.out.println(update);

                return update;


                //TODO : nettoyer HTML, prendre le contenu fichier + match (regex) => ecraser le contenu du fichier
            }
            else
            {
                //TODO : rm cest fichiers???
                //System.out.println("NTM : "+filename);
                return handleFileSourceEasy(doc);
            }

        }

        else
        {
            return handleFileSourceEasy(doc);
        }
    }

    /**
     * Nettoie le contenu d'un fichier et retourne le nouveau contenu
     * @param filename
     * @return
     * @throws IOException
     */
    public static String cleanFile(String filename) throws IOException {
        //System.out.println("Cleaning "+filename);

        File html = new File(filename);

        Document doc = Jsoup.parse(html, "UTF-8");
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        //doc.select("br").append("\\n");
        //doc.select("p").prepend("\\n\\n");

        if(doc.select("p").isEmpty())
        {
            return handleFileSourceEasy(doc);
        }
        //System.out.println("Opening ... "+html.getName());

        return handleFileSourceHard(doc);
    }

    public static void cleanDir(String dir) throws IOException {
        Iterator it = FileUtils.iterateFiles(new File(dir), null, false);
        while(it.hasNext()){
            //System.out.println(((File) it.next()).getName());
            String filename = ((File) it.next()).getName();
            /*if((filename.equals("House_Targaryen.txt")))
            {
                String content = cleanFile(dir+"/"+filename);
                System.out.println(content);
            }*/

            String content = cleanFile(dir+"/"+filename);
        }
    }

    public static void main(String[] args) throws IOException {

        cleanDir("corpus/structured/Characters");
        cleanDir("corpus/structured/Locations");
        cleanDir("corpus/structured/Noble_houses");


        System.exit(0);

        //TODO faire un menu

        ArrayList<String> categories = new ArrayList<>(Arrays.asList("Characters","Locations","Noble_houses"));
        ArrayList<Integer> limits = new ArrayList<>(Arrays.asList(58,21,10));
        try {
            HashMap<String, ArrayList<String>> links = Extractor.getLinks(categories,limits);
            for(Map.Entry<String, ArrayList<String>> entry : links.entrySet())
            {
                //System.out.println("Category:"+entry.getKey());

                //Partie Non Structure
                /*for(String s : entry.getValue())
                {
                    String res = Extractor.extractNotStructured(s);
                    String fileName = s.replace(URL_WIKIA,"");
                    FileUtils.writeStringToFile(new File("corpus/not_structured/"+entry.getKey()+"/"+fileName+".txt"), res);
                }*/

                //Partie Structure
                for(String url : entry.getValue())
                {
                    String res = Extractor.extractStructured(url+"?action=edit");
                    String fileName = url.replace(URL_WIKIA,"");;
                    FileUtils.writeStringToFile(new File("corpus/structured/"+entry.getKey()+"/"+fileName+".txt"), res);
                }

                System.out.println(entry.getValue());
                System.out.println("______");
            }

            /*
            String res = Extractor.extractStructured("http://gameofthrones.wikia.com/wiki/House_Stark?action=edit");
            String fileName = "House_Stark";
            System.out.println("Res : "+res);
            FileUtils.writeStringToFile(new File("corpus/structured/"+"Noble_houses"+"/"+fileName+".txt"), res);
            */

            //
            /*
            String res = Extractor.extractStructured("?action=edit");
                String fileName = "Arya_Stark";
                System.out.println("Res : "+res);
                FileUtils.writeStringToFile(new File("corpus/structured/"+"Characters"+"/"+fileName+".txt"), res);
             */


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