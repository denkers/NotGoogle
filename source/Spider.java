//================================
//  KYLE RUSSELL
//	AUT University 2015
//================================

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class Spider
{
    //The queue of unvisited IndexNodes
    //Uses BlockingQueue to properly sync workers
    private BlockingQueue<IndexNode> unvisited;

    //Newely crawled nodes (unvisited) are added to the buffer
    //We save IndexNodes to the index in batches
    private List<IndexNode> buffer;
    
    //LRU cache to hold most accessed URL's
    public Map<String, Integer> cache;
    
    //The spiders indexer used to save batches and query
    private Indexer indexer;
    
    //The keywords to be used when crawling
    //Should be specified to provide more specific crawls
    private String keywords;
    
    //The workers being used
    //key: worker name
    //value: worker
    private Map<String, SpiderWorker> workers;
    
    //The spiders active crawling config
    private SpiderConfig config;
    
    //Worker condition - enable/disable to resume/disable crawling
    private volatile boolean crawling   =   false;
    
    //Create spider with no seeds or keywords
    //These should be added before crawling
    public Spider()
    {
        this(new String[] {}, new String[] {});
    }
    
    //Create spider with seed URL's and keywords
    public Spider(String[] seeds, String[] keywords)
    {
        loadConfig();
        unvisited       =   new LinkedBlockingQueue<>();
        buffer          =   new ArrayList<>(config.getBufferSize());
        workers         =   new HashMap<>();
        
        
        //Initializes the indexer with the configs database file path
        //If failing to inialize, use default config and update config with default
        try { indexer         =   new Indexer(config.getDatabaseFile()); }
        
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, "Failed to load index, attempting to use default index..");
            try 
            { 
                //Use default index
                //Update config with the default index
                indexer         =   new Indexer(); 
                config.setDatabaseFile(Indexer.DEFAULT_DATABASE);
                updateConfig();
            }
            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Failed to load default index");
                indexer  =   null;
            }
        }
        
        initCache();
        initKeywords(keywords);
        initSeeds(seeds);
    }
    
    //Checks if a node is visited  and if so adds them to queue
    //Updates/adds to cache where appropriate
    private synchronized void add(IndexNode node)
    {
        //Node not visited
        //Add to unvisited and send update to GUI
        if(!isVisited(node.getURL()))
        {
            unvisited.add(node);
            SpiderGUI.sendUrlQueue(node.getURL());    
            
            //Add new entry to not yet full cache
            if(cache.size() < config.getCacheSize())
                cache.put(node.getURL(), 1);
        }
        
        else
        {
           //Update cache with node
           if(cache.containsKey(node.getURL()))
               cache.replace(node.getURL(), cache.get(node.getURL()) + 1);
           
           //Add node to cache
           else cache.put(node.getURL(), 1);
        }
    } 
    
    //Initializes the spiders LRU cache 
    //Cache size is specified in config
    private void initCache()
    {
        cache   =   new LinkedHashMap<String, Integer>(config.getCacheSize(), 0.75f, true)
        {
            @Override
            public boolean removeEldestEntry(Map.Entry last)
            {
                return size() > config.getCacheSize();
            }
        };
    }
    
    //Adds the seed URL's to the unvisited queue
    private void initSeeds(String[] seeds)
    {
        for(String seed : seeds)
            unvisited.add(new IndexNode(seed, 0, null));
    }
    
    //Adds a single seed URL to the unvisited queue
    public void addSeedURL(String seed)
    {
        unvisited.add(new IndexNode(seed, 0, null));
    }
    
    //Removes a url from the unvisited queue
    public boolean removeSeedUrl(String seed)
    {
        return unvisited.remove(new IndexNode(seed, 0, null));
    }
    
    //Adds keywords in regex format to keywords
    public void initKeywords(String[] keys)
    {
        String expr =   "";
        for(int i = 0; i < keys.length; i++)
            expr += keys[i] + ((i < keys.length - 1)? "|" : "");
        
        keywords = expr;
    }
    
    //Adds a keyword to keywords
    public void addKeyword(String keyword)
    {
        if(keywords == null) keywords = keyword;
        else keywords += MessageFormat.format("|{0}", keyword);
    }
    
    //Adds a new worker with name workerName
    //Uses default colouring 
    public void addWorker(String workerName)
    {
        if(workers.containsKey(workerName)) return;
        else
        {
            SpiderWorker worker =   new SpiderWorker(workerName);
            workers.put(workerName, worker);
        }
    }
    
    //Adds a new worker with colour
    public void addWorker(String name, Color workerColour)
    {
        addWorker(new SpiderWorker(name, workerColour));
    }
    
    //Adds a new worker
    public void addWorker(SpiderWorker worker)
    {
        if(worker == null || workers.containsKey(worker.getName())) return;
        else workers.put(worker.getName(), worker);
    }
    
    //Removes a worker with workerName
    public boolean removeWorker(String workerName)
    {
        return workers.remove(workerName) != null;
    }
    
    //Returns the spiders workers
    public Collection<SpiderWorker> getWorkers()
    {
        return workers.values();
    }
    
    //Returns a worker that has name
    public SpiderWorker getWorker(String name)
    {
        return workers.get(name);
    }
    
    //Returns the workers colour if found
    //Default returns white if not found
    public Color getWorkersColour(String name)
    {
        SpiderWorker worker =   workers.get(name);
        
        if(worker == null) return Color.WHITE;
        else return worker.getColour();
    }
    
    //Creates a new default worker
    public SpiderWorker createWorker()
    {
        return new SpiderWorker();
    }
    
    //Begins crawling 
    //Indexer must be initailized 
    //Starts each worker who crawls
    public void crawl()
    {
        if(indexer == null)
        {
            JOptionPane.showMessageDialog(null, "No index selected, please create or open one");
            return;
        }
        
        crawling = true;
        workers.values().stream().forEach((worker) -> 
        {
            worker.start();
        });
    }
    
    //Stops crawling
    //Pauses each of the spiders workers
    //To resume again call resumeCrawling()
    public void stopCrawling()
    {
        synchronized(this)
        {
            crawling    =   false;
            notifyAll();
        }
    }
    
    //Resumes crawling 
    //Resumes all spider workers 
    //Does nothing if crawling hasn't started (crawl())
    public void resumeCrawling()
    {
        synchronized(this)
        {
            crawling    =   true;
            notifyAll();
        }
    }
    
    //Returns the number of workers the spider has
    public int numWorkers()
    {
        return workers.size();
    }
    
    //Returns the number of unvisited URL's
    public int numSeeds()
    {
        return unvisited.size();
    }
    
    //Validates the HTML for keywords
    //Ensures each page has some relevance to the keywords
    //Page needs to contain the keyword(s) in atleast one of the tested areas
    //Returns true if the HTML is relevant (contains keywords)
    private boolean validate(SpiderLeg html)
    {
        Map<String, String> metaData    =   html.getMeta();
        Pattern keyPattern              =   Pattern.compile(keywords, Pattern.CASE_INSENSITIVE);
        String keys                     =   metaData.get(SpiderLeg.META_KEYWORDS);
        String desc                     =   metaData.get(SpiderLeg.META_DESCRIPTION);
        String content                  =   html.getDocument().html();
        String title                    =   html.getTitle();
        Matcher matcher;
        
        //Check keys
        if(keys != null)
        {
            matcher =   keyPattern.matcher(keys);
            if(matcher.find()) return true;
        }
        
        //Check meta description
        if(desc != null)
        {
            matcher =   keyPattern.matcher(desc);
            if(matcher.find()) return true;
        }
        
        //Check title
        if(title != null)
        {
            matcher = keyPattern.matcher(title);
            if(matcher.find()) return true;
        }
       
        //Check content
        matcher =   keyPattern.matcher(content);
        return matcher.find();
    }
    
    //Checks that a URL is valid
    //Catches out any malformed/bad URL's
    public static boolean validateURL(String inputURL)
    {
        try
        {
            URL url =   new URL(inputURL);
            url.toURI();
            return true;
        }
        
        catch(MalformedURLException | URISyntaxException e)
        {
            return false;
        }
    }
    
    //Checks if the crawler is crawling
    //If not then workers are paused
    //To pause crawling call stopCrawling()
    //To resume crawling call resumeCrawling()
    private synchronized void checkCrawling()
    {
        try
        {
            //Crawler has stopped
            //Make workers wait
            if(!crawling)
                wait();
        }
        
        catch(InterruptedException e) {}
    }
    
    //-----------------------------------------------------------------------
    //          SPIDER WORKER
    //-----------------------------------------------------------------------
    //- Spider worker is the spiders crawler
    //- Multiple workers can be used for best results
    //- Crawling and indexing operations are all synced and safe for workers
    //- For crawl process see comments above the workers run()
    //-----------------------------------------------------------------------
    
    private class SpiderWorker extends Thread
    {
        private Color guiColour; //The spider workers output colour
        
        //Create worker with no name and default colour
        //Should be added later
        public SpiderWorker()
        {
            this(null);
        }
        
        //Create worker with name and default colour
        public SpiderWorker(String name)
        {
            this(name, Color.WHITE);
        }
        
        //Create worker with name and specified colour
        public SpiderWorker(String name, Color guiColour)
        {
            super(name);
            this.guiColour  =   guiColour;
        }
        
        //---------------------------------------------------------------------------------
        //          WORKER CRAWL PROCESS
        //---------------------------------------------------------------------------------
        //- Workers take an item, next from the unvisited queue to parse
        //- next is validated for relavency appropriatly
        //- links found in the document of next are validated and checked if visited
        //- valid and unvisited links are added into the unvisited queue 
        //- if config is fetchingImages then images will be parsed and added to next
        //- next will be added to the buffer and if the buffer is full it will be saved
        //- Workers can be paused by calling stopCrawling() and resumed by resumeCrawling()
        //---------------------------------------------------------------------------------
        
        @Override
        public void run()
        {
            try
            {
                //SpiderWorker works until no more unvisited URL's left to be crawled
                //unvisited queue synced by BlockingQueue with 10sec timeout 
                IndexNode next;
                while((next = unvisited.poll(10, TimeUnit.SECONDS)) != null)
                {
                     try
                     {
                         //if crawling continue, otherwise wait
                         checkCrawling();
                         
                         //Update GUI's URL list by removing next's url
                         SpiderGUI.sendUrlQueueRemove(next.getURL());
                         
                         //Parse next document
                         SpiderLeg parser                =   new SpiderLeg(next.getURL());                        
                         
                         //Validate document for relavency 
                         if(!validate(parser)) continue;
                         
                         List<String> fetchedUrls        =   new ArrayList<>(parser.getHyperLinks()); //Fetch all links found in document
                         List<String> usingUrls          =   new ArrayList<>(); //URL's that will be used (all if config.maxAdjUrls = -1)
                         Random rGen                     =   new Random(); //Used if not fetching using all links
                         
                         //Update GUI's worker output with message 
                         String workerOutput             =   MessageFormat.format("[{0}] Crawled {1}", getName(), next.getURL());
                         SpiderGUI.sendOutput(workerOutput);

                         //Fetching and using all URL's 
                         if(fetchedUrls.size() < config.getMaxAdjUrls() || config.getMaxAdjUrls() == -1)
                             usingUrls  =   fetchedUrls;
                         
                         //Using limited number of URL's select random URL's in document
                         else
                         {
                            int bound   =   Math.min(fetchedUrls.size(), config.getMaxAdjUrls());
                            while(usingUrls.size() < bound)
                            {
                                int randIndex   =   rGen.nextInt(fetchedUrls.size());
                                String found    =   fetchedUrls.get(randIndex);
                                usingUrls.add(found);
                            }
                         }
                         
                         next.setTitle(parser.getTitle()); //Set nodes title
                         next.setDescription(parser.getMeta(SpiderLeg.META_DESCRIPTION)); //Set nodes meta description
                         
                         //Fetching images, add images found to next's images
                         //Adds all images if config.maxImages = -1
                         if(config.isFetchingImages())
                         {
                             List<String> fetchedImages      =   new ArrayList<>(parser.getImages()); //Found images in document
                             List<String> usingImages        =   new ArrayList<>(); //Images to be used 
                             
                             //Adding all images
                             //Still iterate through to check for invalid image URL's 
                             if(fetchedImages.size() < config.getMaxImages() || config.getMaxImages() == -1)
                             {
                                 //Add all images found to usingImages
                                 for(String imageURL : fetchedImages)
                                 {
                                     //Validate and add image URL
                                     if(!imageURL.equals("") && validateURL(imageURL))
                                         usingImages.add(imageURL);
                                 }
                             }
                             
                             //Not adding all images, select random images in document
                             else
                             {
                                int bound           =   Math.min(fetchedImages.size(), config.getMaxImages());
                                while(usingImages.size() < bound)
                                {
                                    int randIndex   =   rGen.nextInt(fetchedImages.size());
                                    String found    =   fetchedImages.get(randIndex);
                                    
                                    if(!found.equals("") && validateURL(found)) 
                                        usingImages.add(found);

                                    else bound--;
                                }
                             }
                             
                             //Add images to next node
                             next.setImages(new HashSet<>(usingImages));
                         }
                         
                         saveBatch(); //Save and clear buffer if it is full
                         buffer.add(next); //Add next node to buffer

                         //Ndex adjacent nodes distance will be over max crawl distance
                         //Max crawl distance can be increased for much more broad crawls
                         if(next.getDistance() + 1 > config.getMaxCrawlDistance()) continue;
                         
                         //Add all usingUrls found
                         //add()  will check if they are visited and handle appropriatly
                         Iterator<String> urlIter        =   usingUrls.iterator();
                         while(urlIter.hasNext())
                         {
                             //Validate URL found 
                             String nextURL      =   urlIter.next();
                             if(!validateURL(nextURL)) continue;

                             //url = nextURL, distance = next.distance + 1, parent url = next.url
                             IndexNode adjNode  =   new IndexNode(nextURL, next.getDistance() + 1, next.getURL());
                             add(adjNode);
                         }
                     }

                     catch(IOException e) {}
                 }
            }
            
            catch(InterruptedException e) {}
        } 
        
        //Returns the workers name
        @Override
        public String toString()
        {
            return getName();
        }
        
        //Returns true if the other worker has the same name
        @Override
        public boolean equals(Object other)
        {
            if(other instanceof SpiderWorker)
            {
                SpiderWorker otherWorker    =   (SpiderWorker) other;
                return this.getName().equalsIgnoreCase(otherWorker.getName());
            }
            
            else return false;
        }
        
        //Returns the workers name hashcode
        @Override
        public int hashCode()
        {
            return getName().hashCode();
        }
        
        //Returns the workers output colour
        public Color getColour()
        {
            return guiColour;
        }
        
        //Set the workers output colour
        public void setColour(Color colour)
        {
            guiColour = colour;
        }
    }
    
    //Returns true if the spider is crawling
    public boolean isCrawling()
    {
        return crawling;
    }

    //Saves a batch of IndexNodes in the buffer to the index
    //Only saves when the buffer is full
    //Buffer size should be chosen carefully
    //-large buffer will cause large memory footprint
    //-small buffer will result in much more frequent DB access
    //For table names and columns see constants in Indexer
    private synchronized void saveBatch()
    {
        //Only save when buffer is full
        if(buffer.size() < config.getBufferSize()) return;

        //Query to insert nodes into IND_TABLE_NAME
        //Query is a batch insert
        String indexQuery    =   MessageFormat.format("INSERT OR REPLACE INTO {0} ({1}, {2}, {3}, {4}, {5}) VALUES ",
                Indexer.IND_TABLE_NAME, Indexer.COLUMNS[0], Indexer.COLUMNS[1], Indexer.COLUMNS[2], Indexer.COLUMNS[4], Indexer.COLUMNS[5]);
        
        //Query to insert images of nodes into IMAGE_TABLE_NAME
        //Query is a batch insert
        String imageQuery    =   MessageFormat.format("INSERT OR REPLACE INTO {0} ({1}, {2}) VALUES ", 
                Indexer.IMAGE_TABLE_NAME, Indexer.IMAGE_COLUMNS[0], Indexer.IMAGE_COLUMNS[1]);
        
        //Prepares query rows for all nodes in buffer
        for(int i = 0; i < buffer.size(); i++)
        {
            //---------------------------------------------------------------------------------------
            //      INDEX QUERY
            //---------------------------------------------------------------------------------------
            IndexNode current  =   buffer.get(i);
            
            String url          =   current.getURL();
            String parentUrl    =   current.getParent();
            String description  =   Indexer.clean(current.getDescription());
            String title        =   Indexer.clean(current.getTitle());
            
            String indexEntry        =   MessageFormat.format
            (
                "(\''{0}\'', \''{1}\'', \''{2}\'', \''{3}\'', \''{4}\''){5}",
                url, parentUrl, keywords, description, title, (i != buffer.size() - 1)? ", " : " ");
            
            indexQuery += indexEntry;
            //---------------------------------------------------------------------------------------
            
            
            
            //---------------------------------------------------------------------------------------
            //      IMAGE QUERY
            //---------------------------------------------------------------------------------------
            
            //Images should only be saved if user is fetching images
            if(config.isFetchingImages())
            {
                //Get nodes images and iteratively add rows to image query
                Set<String> images          =   current.getImages();
                Iterator<String> imageIter  =   images.iterator();
                
                while(imageIter.hasNext())
                {
                    String entryVal     =   imageIter.next();
                    String imageEntry   =   MessageFormat.format
                    (
                        "(\''{0}\'', \''{1}\''){2}",
                        entryVal, url, (imageIter.hasNext() || i < buffer.size() - 1)? "," : ""
                    );
                    
                    imageQuery += imageEntry;
                }
            }
            //---------------------------------------------------------------------------------------
        }
        
        //Remove residual delimiters in image query
        if(imageQuery.charAt(imageQuery.length() - 1) == ',')
            imageQuery  =   imageQuery.substring(0, imageQuery.length() - 1);
        
        //Update indexer with index query
        indexer.update(indexQuery);
        
        //If saving images then update indexer with image query
        if(config.isFetchingImages()) indexer.update(imageQuery);
        
        //Nodes in buffer saved, clear buffer
        buffer.clear();
    }
    
    //Converts date to date string with sdfs format
    private boolean needsUpdating(Date date)
    {
        SimpleDateFormat sdf  =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return needsUpdating(sdf.format(date));
    }
    
    //Returns true if the page has not been updated recently
    //Workers crawling will visit a visited page IF their lastUpdated
    //date + number of days before update is older than current date
    //Ensures pages are updated frequently
    //Change configs updateDays to make updates more/less frequent
    private boolean needsUpdating(String dateStr)
    {
        try
        {
            SimpleDateFormat sdf    =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date               =   sdf.parse(dateStr);
            Date current            =   new Date();
            int numDaysBeforeUpdate =   config.getUpdateDays();
            Date expired;
            
            Calendar calendar       =   Calendar.getInstance();
            calendar.setTime(date);
            
            //Add # days before update to the passed date
            
            calendar.add(Calendar.DATE, numDaysBeforeUpdate);
            expired =   calendar.getTime();
            
            //If this page has not been updated recently 
            //then the expired date will be older than current date 
             return expired.before(current);
        }
        
        catch(ParseException e)
        {
            System.out.println("[ParseException] " + e.getMessage());
            return true;
        }
    }
    
    //Checks if the passed URL has been visited yet
    //The method queries both the cache and buffer first
    //If neither contain the URL then checks the index
    //If the URL is found in index (and likely hasn't been visited recently)
    //then it is checked if it needs updating and if so returns false
    //otherwise Returns if the url has been visited
    public synchronized boolean isVisited(String url)
    {
        //Check if cache or buffer contain the URL
        if(cache.containsKey(url) || buffer.contains(new IndexNode(url))) return true;
        else
        {
            String conditional              =   MessageFormat.format("WHERE {0} = \''{1}\'';", Indexer.COLUMNS[0], url);
            Map<String, IndexNode> found    =   indexer.searchIndex(conditional, false);
            
            //URL found in index
            if(found.size() > 0)
            {
                //Check if page is outdated and needs updating
                //Returns false (even though the page is visited) if needs updating
                IndexNode foundNode =  found.get(url);
                return !needsUpdating(foundNode.getLastUpdated());
            }
            
            return false;
        }
    } 
    
    //Set the spiders indexer
    public void setIndexer(Indexer indexer)
    {
        this.indexer    =   indexer;
    }
    
    //Returns the spiders indexer
    public Indexer getIndexer()
    {
        return indexer;
    }
    
    //Returns teh spiders config
    public SpiderConfig getConfig()
    {
        return config;
    }
    
    //Loads the spiders config from CONFIG_FILE
    //If config is not found then default config is used
    public void loadConfig()
    {
        //Get config from file
        try(ObjectInputStream ois =   new ObjectInputStream(new FileInputStream(SpiderConfig.CONFIG_FILE)))
        {
            config = (SpiderConfig) ois.readObject();
        }
        
        //Config not found, use default
        catch(IOException | ClassNotFoundException e)
        {
            config = new SpiderConfig();
            updateConfig();
        }
        
    }
    
    //Updates the config stored with the current spiders config
    public void updateConfig()
    {
        //Save the spiders config 
        try(ObjectOutputStream oos  =   new ObjectOutputStream(new FileOutputStream(SpiderConfig.CONFIG_FILE)))
        {
            if(config == null) config = new SpiderConfig();
            oos.writeObject(config);
        }
        
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, "Failed to save config");
        }
    }
}
