//================================
//  KYLE RUSSELL
//	AUT University 2015
//================================

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class Indexer 
{
    public static final String DEFAULT_DATABASE     =   "data/example.db"; //Database file used when config DB not found or not specified
    public static final String IND_TABLE_NAME       =   "CrawlerIndexes"; //Index table name
    public static final String IMAGE_TABLE_NAME     =   "Images"; //Images table name
    private final String CONN_STR; //The indexers connection string
    public static String[] COLUMNS; //Index table column names
    public static String[] IMAGE_COLUMNS; //Image table column names
    
    //Create Indexer with default database
    public Indexer() throws SQLException
    {
        this(DEFAULT_DATABASE);
    }
    
    //Create Indexer with specified database
    //Should attempt to use default if passed database is not found
    public Indexer(String database) throws SQLException
    {
        CONN_STR        =   MessageFormat.format("jdbc:sqlite:{0}", database);
        COLUMNS         =   new String[] { "url", "parent", "keywords", "lastUpdated", "description", "title" };
        IMAGE_COLUMNS   =   new String[] { "image_url", "page" };
        
        DriverManager.registerDriver(new org.sqlite.JDBC()); 
        getConnection(); 
        
        initTables();
        initDBIndexes();
    }
    
    //Creates the tables to be used in the index
    //This includes the IND_TABLE_NAME for holding IndexNode properties
    //And IMAGE_TABLE_NAME for the images of those IndexNodes
    //There is a 1:M relationship and unique indexes on both tables URL's fields
    //Including indexes on index tables parent and image tables page columns 
    private void initTables()
    {
        //CREATE INDEX TABLE QUERY
        String createIndexTable    =   MessageFormat.format
        (
            "CREATE TABLE IF NOT EXISTS {0}"
            + "( "
                + "{1} VARCHAR(255) NOT NULL PRIMARY KEY, "
                + "{2} VARCHAR(255) NULL, "
                + "{3} VARCHAR(255) NULL, "
                + "{4} DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "{5} VARCHAR(255) NULL, "
                + "{6} VARCHAR(255) NULL "
            + ");"
        , IND_TABLE_NAME, COLUMNS[0], COLUMNS[1], COLUMNS[2], COLUMNS[3], COLUMNS[4], COLUMNS[5]);
        
        
        //CREATE IMAGE TABLE QUERY
        String createImageTable     =   MessageFormat.format
        (
                "CREATE TABLE IF NOT EXISTS {0}"
                + "( "
                    + "{1} VARCHAR(255) NOT NULL PRIMARY KEY, "
                    + "{2} VARCHAR(255) NOT NULL, "
                    + "FOREIGN KEY({3}) REFERENCES {4}({5}) ON DELETE CASCADE"
                + ");"
        , IMAGE_TABLE_NAME, IMAGE_COLUMNS[0], IMAGE_COLUMNS[1], IMAGE_COLUMNS[1], IND_TABLE_NAME, COLUMNS[0]);
        
        
        try(Connection conn =   getConnection())
        {
            Statement querySt   =   conn.createStatement();
            querySt.executeUpdate(createIndexTable);
            querySt.executeUpdate(createImageTable);
        }
        
        catch(SQLException e)
        {
            System.out.println("[SQLException] Failed to initialize tables: " + e.getMessage());
        }
    }
    
    //Adds parent index to the IND_TABLE_NAME table
    private void initDBIndexes()
    {
        try(Connection conn =   getConnection())
        {
            String query    =   MessageFormat.format
            (
                "CREATE INDEX IF NOT EXISTS url_parent_ind "
                + "ON {0} ({1});"
            , IND_TABLE_NAME, COLUMNS[1]);
            
            Statement querySt = conn.createStatement();
            querySt.executeUpdate(query);
        }
        
        catch(SQLException e)
        {
            System.out.println("[SQLException] Failed to initialize indexes: " + e.getMessage());
        }
    }

    //Drops the tables in the database 
    //Call initTables() and initDBIndexes() to re-create them
    public void resetTables()
    {
        String dropIndexTable       =   MessageFormat.format("DROP TABLE IF EXISTS {0}", IND_TABLE_NAME);
        String dropImageTable       =   MessageFormat.format("DROP TABLE IF EXISTS {0}", IMAGE_TABLE_NAME);
        try(Connection conn =   getConnection())
        {
            Statement querySt   =   conn.createStatement();
            querySt.executeUpdate(dropIndexTable);
            querySt.executeUpdate(dropImageTable);
        }
        
        catch(SQLException e)
        {
            System.out.println("[SQLException] Failed to reset tables: " + e.getMessage());
        }
    }
    
    //Executes an update query in the database
    public void update(String query)
    {
        try(Connection conn = getConnection())
        {
            Statement querySt   =   conn.createStatement();
            querySt.executeUpdate(query);
        }
        
        catch(SQLException e)
        {
            System.out.println("[SQLException] Failed to update: " + e.getMessage());
        }
    }
    
    //Returns the search results from querying the database
    //conditional: the WHERE clause to append
    //joinImages: pass true if you want to perform a join to the images table
    //Return map will be empty if the query returned no rows
    //Similarly the number of rows returned is comparable to the maps size
    public Map<String, IndexNode> searchIndex(String conditional, boolean joinImages)
    {
        //Found Indexes: search results
        //key: page URL
        //value: the IndexNode with stored data added
        Map<String, IndexNode> indexes      =   new HashMap<>();
        String query                        =   MessageFormat.format
        (
            "SELECT * FROM {0}{1} {2}",
            IND_TABLE_NAME, (joinImages)? ", " + IMAGE_TABLE_NAME : "", (conditional != null)? conditional : "");
        
        try(Connection conn     =   getConnection())
        {
            Statement querySt   =   conn.createStatement();
            ResultSet results   =   querySt.executeQuery(query);
            
            //Add all IndexNodes for each row into indexes
            while(results.next())
            {
                String url              =   results.getString(1);
                String parentUrl        =   results.getString(2);
                String lastUpdatedStr   =   results.getString(4);
                String desc             =   results.getString(5);
                String title            =   results.getString(6);
                IndexNode currentNode   =   (indexes.containsKey(url))? indexes.get(url) : new IndexNode(url, parentUrl, desc, title);
                
                //Joining images, add the image found
                if(joinImages)
                {
                    String image_url    =   results.getString(7);
                    currentNode.addImage(image_url);
                }
                
                //Parse the lastUpdatedStr and set as nodes lastUpdated date
                if(lastUpdatedStr != null)
                {
                    SimpleDateFormat sdf    =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    currentNode.setLastUpdated(sdf.parse(lastUpdatedStr));
                }
                
                if(!indexes.containsKey(url))
                    indexes.put(url, currentNode);
            }
        }
        
        catch(SQLException e) {}
        
        finally
        {
            return indexes;
        }
    }
    
    //Returns the results of querying for the passed keywords
    //Query is without join on image table, use searchImageKeywords()
    public Map<String, IndexNode> searchKeywords(String keywords)
    {
        String conditional  =   MessageFormat.format("WHERE {0} LIKE \''%{1}%\''", COLUMNS[2], keywords);
        return searchIndex(conditional, false);
    }
    
    //Returns the results of querying for the passed keywords
    //Uses a join on image table and results will include images of nodes
    public Map<String, IndexNode> searchImageKeywords(String keywords)
    {
        String conditional  =   MessageFormat.format("WHERE {0} LIKE \''%{1}%\'' AND {2}.{3} = {4}.{5}", 
                                COLUMNS[2], keywords, IND_TABLE_NAME, COLUMNS[0], IMAGE_TABLE_NAME, IMAGE_COLUMNS[1]);
        return searchIndex(conditional, true);
    }
    
    //Cleans the string of quotes, new lines, back slashes 
    public static String clean(String text)
    {
        return (text != null)? text.replaceAll("\'?\"?\\\\?\n?", "") : "";
    }
    
    //Attempts to establish and returns a connection 
    //with the connection string CONN_STR
    private Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(CONN_STR);
    }
    
    //Factory method to create indexer with passed database 
    public static Indexer createIndexer(String database)
    {
        try { return new Indexer(database); }
        
        //Indexer failed to initialize, index was not found or other exception
        //Attempt to use default database otherwise return null
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, "Failed to load index, attempting to use default index..");
            try { return new Indexer(); }
            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Failed to load default index");
                return null;
            }
        }
    }
    
    //Factory method to create an indexer with default database
    //Returns null if the database fails to initialize
    public static Indexer createIndexer()
    {
        try { return new Indexer(); }
        
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, "Failed to load default index");
            return null;
        }
    }
}
