//================================
//  KYLE RUSSELL
//	AUT University 2015
//================================

import java.io.Serializable;


public class SpiderConfig implements Serializable
{
    public static final String CONFIG_FILE  =   "data/crawler_config"; //Default path to save/open config
    private String databaseFile; //the database/index file path
    private int maxCrawlDistance; //max distance nodes when crawling
    private int maxAdjUrls; //max number of links to be fetched when crawling (-1 for unlimited)
    private int bufferSize; //the spiders buffer size 
    private int cacheSize; //the spiders cache size
    private int updateDays; //number of days before updating old pages
    private boolean fetchImages; //true/false if you are wanting to fetch and store images found
    private int maxImages; //max number of images to fetched (ignored if fetchImages is false)
    
    //Default conifg
    public SpiderConfig()
    {
        this(4, 10, 30, 30, 2, true, Indexer.DEFAULT_DATABASE, 10);
    }
    
    public SpiderConfig(int maxCrawlDistance, int maxAdjUrls, int bufferSize, int cacheSize, int updateDays, boolean fetchImages, String databaseFile, int maxImages)
    {
        this.maxCrawlDistance   =   maxCrawlDistance;
        this.maxAdjUrls         =   maxAdjUrls;
        this.bufferSize         =   bufferSize;
        this.cacheSize          =   cacheSize;
        this.updateDays         =   updateDays;
        this.fetchImages        =   fetchImages;
        this.databaseFile       =   databaseFile;
        this.maxImages          =   maxImages;
    }
    
    //Returns the max crawling distance
    public int getMaxCrawlDistance()
    {
        return maxCrawlDistance;
    }
    
    //Returns the max number of links being fetched
    public int getMaxAdjUrls()
    {
        return maxAdjUrls;
    }
    
    //Returns the configs buffer size
    public int getBufferSize()
    {
        return bufferSize;
    }
    
    //Returns the configs cache size
    public int getCacheSize()
    {
        return cacheSize;
    }
    
    //Rurns the number of days before updating old pages
    public int getUpdateDays()
    {
        return updateDays;
    }
    
    //Returns the index/database file being used
    public String getDatabaseFile()
    {
        return databaseFile;
    }
    
    //Returns true if user wants to fetch images 
    public boolean isFetchingImages()
    {
        return fetchImages;
    }
    
    //Returns the max number of images to be fetched
    public int getMaxImages()
    {
        return maxImages;
    }
    
    //Set the max crawling distance
    public void setMaxCrawlDistance(int maxCrawlDistance)
    {
        this.maxCrawlDistance   =   maxCrawlDistance;
    }
    
    //Set the max number of links to be fetched
    public void setMaxAdjUrls(int maxAdjUrls)
    {
        this.maxAdjUrls =   maxAdjUrls;
    }
    
    //Set the configs buffersize
    public void setBufferSize(int bufferSize)
    {
        this.bufferSize =   bufferSize;
    }
    
    //Set the configs cache size
    public void setCacheSize(int cacheSize)
    {
        this.cacheSize  =   cacheSize;
    }
    
    //Set the number of days before updating old pages
    public void setUpdateDays(int updateDays)
    {
        this.updateDays =   updateDays;
    }
    
    //Set if to fetch images
    public void setFetchImages(boolean fetchImages)
    {
        this.fetchImages    =   fetchImages;
    }
    
    //Set the configs index/database file
    public void setDatabaseFile(String databaseFile)
    {
        this.databaseFile   =       databaseFile;
    }
    
    //Set the max number of images to fetch
    public void setMaxImages(int maxImages)
    {
        this.maxImages  =   maxImages;
    }
}
