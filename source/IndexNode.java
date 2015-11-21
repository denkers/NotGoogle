//================================
//  KYLE RUSSELL
//	AUT University 2015
//================================

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class IndexNode implements Comparable<IndexNode>
{
    private int distance; //Distance from the starting node
    private String url; //URL used
    private String parent; //Parent nodes URL
    private String description; //meta description
    private String title; //Title of page
    private double rank; //PageRank of node
    private Date lastUpdated; //Date since node was last crawled
    private Set<String> images; //Images found from the page
    
    public IndexNode(String url)
    {
        this(url, null);
    }
    
    public IndexNode(String url, String parent)
    {
        this(url, Integer.MAX_VALUE, parent);
    }
    
    public IndexNode(String url, String parent, String description, String title)
    {
        this(url, Integer.MAX_VALUE, parent, description, title, 0.0);
    }
    
    public IndexNode(String url, String parent, String description, String title, double rank)
    {
        this(url, Integer.MAX_VALUE, parent, description, title, rank);
    }
    
    public IndexNode(String url, int distance, String parent)
    {
        this(url, distance, parent, "", "", 0.0);
    }
    
    public IndexNode(String url, int distance, String parent, String description, String title, double rank)
    {
         this.url            =   url;
         this.distance       =   distance;
         this.parent         =   parent;
         this.description    =   description;
         this.title          =   title;
         this.rank           =   rank;
         images              =   new HashSet<>();
    }
   
    //Returns the distance of the node
    public int getDistance()
    {
        return distance;
    }

    //Returns the URL of the node
    public String getURL()
    {
        return url;
    }

    //Returns the parent nodes URL
    //null if seed URL
    public String getParent()
    {
        return parent;
    }

    //Returns the meta description of the page
    public String getDescription()
    {
        return description;
    }

    //Returns the title of the page
    public String getTitle()
    {
        return title;
    }

    //Returns the nodes PageRank
    //Not set until search
    public double getRank()
    {
        return rank;
    }

    //Returns the images of the node
    public Set<String> getImages()
    {
        return images;
    }

    //Returns the date the node was last crawled
    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    //Set the nodes distance
    public void setDistance(int distance)
    {
        this.distance   =   distance;
    }

    //Set the parent URL
    public void setParent(String parent)
    {
        this.parent =   parent;
    }

    //Set the meta description
    public void setDescription(String description)
    {
        this.description    =   description;
    }

    //Set the title
    public void setTitle(String title)
    {
        this.title  =   title;
    }

    //Set the PageRank
    public void setRank(double rank)
    {
        this.rank   =   rank;
    }

    //Set the images of the node
    public void setImages(Set<String> images)
    {
        this.images =   images;
    }

    //Set the date last crawled
    public void setLastUpdated(Date lastUpdated)
    {
        this.lastUpdated    =   lastUpdated;
    }

    //Add an image to the nodes images
    public void addImage(String image)
    {
        images.add(image);
    }
    
    //Nodes are the same if they have the same URL
    //Nodes are indexed on their URL 
    @Override
    public boolean equals(Object other)
    {
        if(other instanceof IndexNode)
        {
            IndexNode otherNode    =   (IndexNode) other;
            return this.url.equalsIgnoreCase(otherNode.getURL());
        }

        else return false;
    }

    //Returns hashcode of URL
    @Override
    public int hashCode()
    {
        return url.hashCode();
    }

    //Compares two nodes on their PageRank
    @Override
    public int compareTo(IndexNode other) 
    {
        return Double.compare(rank, other.getRank());
    }
}
