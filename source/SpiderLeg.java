//================================
//  KYLE RUSSELL
//	AUT University 2015
//================================

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class SpiderLeg
{
    
    private Document document; //Active parsing document
    private final String AGENT; //User agent client string
    private final String URL; //URL used by parser
    
    //-----------------------------------------------------------
    //                      META DATA KEYS
    //-----------------------------------------------------------
    public static final String META_DESCRIPTION =   "description";
    public static final String META_KEYWORDS    =   "keywords";
    //-----------------------------------------------------------
    
    
    public SpiderLeg(String url) throws IOException
    {
        this.URL    =   url;
        AGENT       =   "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36";
        initDocument();
    }
    
    //Parses and initializes the documents
    //Throws exception if parsing fails
    //Suppress non-critical parse exceptions
    private void initDocument() throws IOException
    {
        document    =   Jsoup.connect(URL)
                        .userAgent(AGENT)
                        .get();
    }
    
    //Returns the parsers document
    //Null if parsing failed
    public Document getDocument()
    {
        return document;
    }
    
    //Returns the title of the parsed document
    //Scans for <title> tag in head
    public String getTitle()
    {
        if(document == null) return null;
        else return document.getElementsByTag("title").text();
    }
    
    //Returns the links found in the document
    //Scans for all <a> tags
    public Set<String> getHyperLinks()
    {
        if(document == null) return null;
        else
        {
            Set<String> urls =   new HashSet<>();
            Elements linkTags       =   document.getElementsByTag("a");
            
            Iterator<Element> linkIter  =   linkTags.iterator();
            while(linkIter.hasNext())
            {
                Element link        =   linkIter.next();
                Attributes attrs    =   link.attributes();
                String url          =   attrs.get("href");
                
                urls.add(url);
            }
            
            return urls;
        }
    }
    
    //Returns all images found in the document
    //Scans for all <img> tags and stores their URL
    public Set<String> getImages()
    {
        if(document == null) return null;
        else
        {
            Set<String> images   =   new HashSet<>();
            Elements imgTags            =   document.getElementsByTag("img");
            
            Iterator<Element> imageIter =   imgTags.iterator();
            while(imageIter.hasNext())
            {
                Element image           =   imageIter.next();
                Attributes imageAttrs   =   image.attributes();
                String imageURL         =   imageAttrs.get("src");
                
                images.add(imageURL);
            }

            return images;
        }
    }
    
    //Returns meta tags found in the document
    //Tags are limited to those keys found above
    public Map<String, String> getMeta()
    {
        if(document == null) return null;
        else
        {
            Map<String, String> metaData    =   new HashMap<>();
            Elements metaTags               =   document.getElementsByTag("meta");
            
            Iterator<Element> metaIterator  =   metaTags.iterator();
            while(metaIterator.hasNext())
            {
                Element metaTag     =   metaIterator.next();
                Attributes attrs    =   metaTag.attributes();
                
                String name         =   attrs.get("name");
                String content      =   attrs.get("content");
                
                if(name.equalsIgnoreCase(META_DESCRIPTION))
                    metaData.put(META_DESCRIPTION, content);
                
                else if(name.equalsIgnoreCase(META_KEYWORDS))
                    metaData.put(META_KEYWORDS, content);
            }
            
            return metaData;
        }
    }
    
    //Returns a meta tag value based on the tag name
    //Fetches meta tags before returning value
    //If need more than one tag: keep reference to tags 
    public String getMeta(String name)
    {
        Map<String, String> metaTags    =   getMeta();
        if(metaTags == null) return null;
        else return metaTags.get(name);
    }
}
