//================================
//  KYLE RUSSELL
//	AUT University 2015
//================================

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class SearchEngine
{
    public static final double DAMP_FACTOR  =   0.15; //PageRanks dampening factor
    private Indexer indexer; //The search engines indexer 
    
    //Create search engine using default database
    public SearchEngine()
    {
        this(Indexer.createIndexer());
    }
    
    //Create search engine using passed database
    public SearchEngine(Indexer indexer)
    {
        this.indexer    =   indexer;
    }
    
    //Performs a search using the passed keywords and returns the result as list of IndexNodes
    //keywords: keywords you want to search for
    //includeImages: search for images
    public List<IndexNode> search(String keywords, boolean includeImages)
    {
        //Perform query using keywords as conditional
        Map<String, IndexNode> stored  =   (includeImages)? indexer.searchImageKeywords(keywords) : indexer.searchKeywords(keywords);
        List<IndexNode> results        =   new ArrayList<>();
        
        //No results, return empty list
        if(stored.isEmpty()) return results;
        else
        {
            double[][] transitionMatrix         =   computeWeights(stored);
            double[][] ranksMatrix              =   MatrixTools.pageRankPI(transitionMatrix);
            PriorityQueue<IndexNode> ranks      =   new PriorityQueue<>(ranksMatrix.length, Collections.reverseOrder());
            
            //Set the ranks from the ranksMatrix to the nodes
            Iterator<IndexNode> nodeIter        =   stored.values().iterator();
            for(int i = 0; nodeIter.hasNext(); i++)
            {
                IndexNode current               =   nodeIter.next();
                double currentRank              =   ranksMatrix[i][0];
                
                current.setRank(currentRank);
                ranks.add(current);
            }
            
            //Add the nodes to results in decreasing order of their rank 
            while(!ranks.isEmpty())
            {
                IndexNode node = ranks.poll();
                results.add(node);
            }
            
            return results;
        }
    }
    
    //Computes the edge weights for the index results 
    //Returns the transition adjacency matrix 
    private double[][] computeWeights(Map<String, IndexNode> fetched)
    {
        int n                           =   fetched.size();
        double[][] transitionMatrix     =   new double[n][n];   
        
        //Indexer not found, return empty transition matrix
        if(indexer == null) return transitionMatrix;
        
        //Compute edge weights and add to transitionMatrix
        Iterator<IndexNode> urlIter     =   fetched.values().iterator();
        for(int row = 0; urlIter.hasNext(); row++)
        {
            IndexNode currentURL               =   urlIter.next();
            Map<String, IndexNode> outEdges    =   getOutgoingEdges(currentURL.getURL());    
            int degree                         =   outEdges.size();
            
            //Node has degree 0: weight = 1/n
            if(degree == 0)
            {
                for(int col = 0; col < n; col++)
                {
                    double val  =   1.0 / n;
                    transitionMatrix[row][col] = val;
                }
            }
            
            else
            {
                //Iterate over all nodes
                //If a node is adjacent, then weight =  (1 - DAMP_FACTOR) / n + (DAMP_FACTOR / degree)
                //Otherwise weight = (1 - DAMP_FACTOR) /  n
                Iterator<IndexNode> innerUrlIter   =   fetched.values().iterator();
                for(int col = 0; innerUrlIter.hasNext(); col++)
                {
                    IndexNode currentInnerNode     =   innerUrlIter.next();
                    double val;
                    
                    //Node is adjacent, weight = first case
                    if(outEdges.containsKey(currentInnerNode.getURL()))
                        val = ((1 - DAMP_FACTOR) / n) + (DAMP_FACTOR / degree);
                    
                    //Node is not adjacent, weight = second case
                    else
                        val = ((1 - DAMP_FACTOR) / n);
                    
                    //Assign the weight to transitionMatrix
                    transitionMatrix[row][col] = val;
                }
            }

        }
        
        return transitionMatrix;
    }
    
    //Searches for all outgoing edges in the index
    //Outgoing edges in the index are rows with parent = URL
    private Map<String, IndexNode> getOutgoingEdges(String URL)
    {
        String conditional    =   MessageFormat.format("WHERE {0} = \''{1}\''", Indexer.COLUMNS[1], URL);
        return indexer.searchIndex(conditional, false);
    }
    
    //Returns the search engines indexer
    public Indexer getIndexer()
    {
        return indexer;
    }
    
    //Set the search engines indexer
    public void setIndexer(Indexer indexer)
    {
        this.indexer    =   indexer;
    }
}
