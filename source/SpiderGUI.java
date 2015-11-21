//================================
//  KYLE RUSSELL
//	AUT University 2015
//================================

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.xswingx.PromptSupport;


public class SpiderGUI extends JPanel implements ActionListener
{
    //------------------------------------------
    //                  LAYOUT
    //------------------------------------------
    private final JFrame frame;
    private static final int WINDOW_WIDTH           =   750;
    private static final int WINDOW_HEIGHT          =   630;
    private static final String SEARCH_PANE_VIEW    =   "search_p";
    private static final String TRANS_PANE_VIEW     =   "trans_p";
    private static final String RESULTS_PANE_VIEW   =   "results_p";
    private static final String VIEW_PANE           =   "view_p";
    private static final String IMAGES_DIR          =   "data/res/images/";
    private JTabbedPane viewPane;
    private JPanel viewPaneWrapper;
    private JPanel searchPane;
    private JPanel crawlPane;
    private JPanel transitionPane;
    private JPanel resultsPane;
    private String currentLayoutView;
    
    //------------------------------------------
    //                RESOURCES
    //------------------------------------------
    private BufferedImage searchBackgroundImage;
    private BufferedImage miniMenuAddImage;
    private BufferedImage miniMenuRemoveImage;
    private BufferedImage openFileImage;
    private BufferedImage newFileImage;
    private BufferedImage playButtonImage;
    private BufferedImage stopButtonImage;
    private BufferedImage updateMiniImage;
    private BufferedImage spiderImage;
    private BufferedImage searchIcon;
    private BufferedImage searchIconDark;
    private BufferedImage settingsImage;
    private BufferedImage webIconImage;
    private BufferedImage clipboardImage;
    private BufferedImage colorIconImage;
    private String transitionSpinnerImage;
    private String processingImage;
    private String processingMiniImage;
    
    
    //------------------------------------------
    //              SEARCH ENGINE
    //------------------------------------------
    private final String HAS_RESULTS_VIEW       =   "has_results";
    private final String NO_RESULTS_VIEW        =   "no_results";
    private final String IMAGE_RESULTS_PANE     =   "image_results";
    private final String WEB_RESULTS_PANE       =   "web_results";
    private final int NUM_IMAGE_COLS            =   4;
    private final SearchEngine searchEngine;
    private JLabel logoLabel;
    private JTextField searchBar;
    private JPanel resultsRotatePane;
    private JButton webSearchButton, imageSearchButton;
    private JButton searchBackButton;
    private JLabel pageInfoLabel;
    private JPanel searchPaneWrapper;
    private JPanel resultsPaneWrapper;
    private JPanel noResultsPane;
    private JPanel resultsPageControls;
    private JPanel resultsCardWrapper;
    private JTable resultsImagesTable;
    private ResultTableModel resultImageModel;
    private JButton prevPageButton, nextPageButton;
    private JTextField resultsSearchBar;
    private JButton resultsSearchButton;
    private JPanel resultsSearchPane;
    private JTable resultsTable;
    private ResultTableModel resultModel;
    private JPanel resultControls;
    private JLabel resultInfo;
    private JComboBox filterNumResults;
    private String currentSearchView;
    private int currentPage, maxPage;
    private JLabel workingLabel;
    private boolean modalOpen;
    private boolean searchWeb;
    private List<ImageNode> currentSearchImages;
    
    //------------------------------------------
    //                  MENU BAR
    //------------------------------------------
    private JMenuBar bar;
    private JMenu fileMenu, aboutMenu, crawlerMenu;
    private JMenuItem exit, openIndexItem, newIndexItem, preferences, author;
    private JMenuItem startCrawlerItem, stopCrawlerItem;
    //------------------------------------------
    
    
    //------------------------------------------
    //             CRAWLER CONTROLS
    //------------------------------------------
    private JPanel crawlControls, leftPane, rightPane, rightPaneWrapper;
    private JButton newIndexButton, openIndexButton, playButton, stopButton, crawlSettingsButton;
    private JList outputView;
    private static DefaultListModel outputModel;
    private JPanel keywordPanel;
    private JTextField keywordBar;
    private JButton keywordUpdate;
    private static JScrollPane outputScroll;
    private boolean canResume;
    //------------------------------------------
    
    
    //------------------------------------------
    //            WORKER LIST
    //------------------------------------------
    private JPanel workerPanel;
    private JPanel workerControls;
    private JButton addWorkerButton, removeWorkerButton;
    private JList workerList;
    private static DefaultListModel workerModel;
    private static JScrollPane workerScroll;
    
    
    //------------------------------------------
    //             URL LIST
    //------------------------------------------
    private JPanel urlPanel;
    private JPanel urlControls;
    private JButton addUrlButton, removeUrlButton;
    private JList urlList;
    private static DefaultListModel urlModel;
    private static JScrollPane urlScroll;
    
    private final Spider spider;
    
    public SpiderGUI(JFrame frame)
    {
        this.frame      =   frame;
        spider          =   new Spider();
        searchEngine    =   new SearchEngine(spider.getIndexer());
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        
        initLookAndFeel();
        initResources();
        initMenu();
        initComponents();
    }
    
    //Initializes the applications look and feel package
    //For package see lib\SyntheticaLib\*
    private static void initLookAndFeel()
    {
        try 
        {
            UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaAluOxideLookAndFeel");
        }
        
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            JOptionPane.showMessageDialog(null, "Failed to load resource package");
        }
    }
    
    //Initializes all the applications resources
    //Includes all static images used consistently
    //IMAGES_DIR: res\images
    //Folder must be accompanied with executable
    private void initResources()
    {
        try
        {
            searchBackgroundImage   =   ImageIO.read(new File(IMAGES_DIR + "logo.jpg"));
            searchIcon              =   ImageIO.read(new File(IMAGES_DIR + "search_icon.png"));
            miniMenuAddImage        =   ImageIO.read(new File(IMAGES_DIR + "addSmallIcon.png"));
            miniMenuRemoveImage     =   ImageIO.read(new File(IMAGES_DIR + "removeSmallIcon.png"));
            updateMiniImage         =   ImageIO.read(new File(IMAGES_DIR + "up.png"));
            playButtonImage         =   ImageIO.read(new File(IMAGES_DIR + "play_arrow.png"));
            stopButtonImage         =   ImageIO.read(new File(IMAGES_DIR + "stop_square.png"));
            openFileImage           =   ImageIO.read(new File(IMAGES_DIR + "open_icon.png"));
            newFileImage            =   ImageIO.read(new File(IMAGES_DIR + "new_file.png"));
            settingsImage           =   ImageIO.read(new File(IMAGES_DIR + "settings.png"));
            spiderImage             =   ImageIO.read(new File(IMAGES_DIR + "spider_icon.png"));
            searchIconDark          =   ImageIO.read(new File(IMAGES_DIR + "search_icon_dark.png"));
            webIconImage            =   ImageIO.read(new File(IMAGES_DIR + "web_icon.png"));
            clipboardImage          =   ImageIO.read(new File(IMAGES_DIR + "clipboard.png"));
            colorIconImage          =   ImageIO.read(new File(IMAGES_DIR + "color_icon.png"));
            transitionSpinnerImage  =   IMAGES_DIR + "loading.gif";
            processingImage         =   IMAGES_DIR + "processing.gif";
            processingMiniImage     =   IMAGES_DIR + "spinner.gif";
        }
        
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, "[Error] Failed to load resource(s)");
        }
    }
    
    //Initializes the apps menu bar
    //Attached to passed frame
    private void initMenu()
    {
        bar         =   new JMenuBar();
        fileMenu    =   new JMenu("File");
        crawlerMenu =   new JMenu("Crawler");   
        aboutMenu   =   new JMenu("About");
        
        bar.add(fileMenu);
        bar.add(crawlerMenu);
        bar.add(aboutMenu);
        
        exit                =   new JMenuItem("Exit");
        preferences         =   new JMenuItem("Preferences");
        author              =   new JMenuItem("Author");
        startCrawlerItem    =   new JMenuItem("Start");
        stopCrawlerItem     =   new JMenuItem("Stop");
        newIndexItem        =   new JMenuItem("New index");
        openIndexItem       =   new JMenuItem("Open index");
        
        stopCrawlerItem.setEnabled(false);
        
        fileMenu.add(newIndexItem);
        fileMenu.add(openIndexItem);
        fileMenu.add(exit);
        aboutMenu.add(author);
        crawlerMenu.add(startCrawlerItem);
        crawlerMenu.add(stopCrawlerItem);
        crawlerMenu.add(preferences);
        
        author.addActionListener(this);
        preferences.addActionListener(this);
        exit.addActionListener(this);
        startCrawlerItem.addActionListener(this);
        stopCrawlerItem.addActionListener(this);
        newIndexItem.addActionListener(this);
        openIndexItem.addActionListener(this);
        
        frame.setJMenuBar(bar);
    }
    
    //Initializes all components in the application
    //Expand modules to reduce overhead at startup
    private void initComponents()
    {
        //LAYOUT
        initLayoutComponents();
        
        //TRANSITION COMPONENTS
        initTransitionComponents();
        
        //SEARCH PANE COMPONENTS
        initSearchComponents();
        
        //RESULTS PANE COMPONENTS
        initResultComponents();
        
        // CRAWLER CONTROLS
        initCrawlerControlsComponents();
        
        // KEYWORD BAR
        initKeywordBarComponents();
        
        //WORKER CONTROLS
        initWorkerComponents();
        
        //URL COMPONENTS
        initURLMenuComponents();

        //BASE LAYOUT COMPONENTS
        initBaseLayout();
    }
    
    //Adds the panels used to the parent panel
    //Initializes starting views
    private void initBaseLayout()
    {
        add(viewPaneWrapper, BorderLayout.CENTER);
        
        currentLayoutView   =   VIEW_PANE;
        currentSearchView   =   SEARCH_PANE_VIEW;
        showMainView(VIEW_PANE);
        showSearchView(currentSearchView);
    }
    
    //Initializes various layout components 
    //Includes all high level parent panels used
    private void initLayoutComponents()
    {
        viewPaneWrapper     =   new JPanel(new CardLayout());
        viewPane            =   new JTabbedPane();
        crawlPane           =   new JPanel(new BorderLayout());
        transitionPane      =   new JPanel();
        searchPane          =   new BGRenderer(Color.WHITE, WINDOW_WIDTH, WINDOW_HEIGHT);
        resultsPane         =   new BGRenderer(Color.WHITE, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    //Initializes the transition pane
    //Transition pane is shown in-between changes/processing
    private void initTransitionComponents()
    {
        transitionPane.setBackground(Color.WHITE);
        JLabel transitionLogo   =   new JLabel(new ImageIcon(transitionSpinnerImage));
        transitionLogo.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
        transitionPane.add(transitionLogo);
    }
    
    //Initializes the search engines search pane
    //This is the pane in second view tab
    //Additionally initializes tabs 
    private void initSearchComponents()
    {
        searchPaneWrapper       =   new JPanel(new CardLayout());
        logoLabel               =   new JLabel(new ImageIcon(searchBackgroundImage));
        searchBar               =   new JTextField();
        webSearchButton         =   new JButton("Search web");
        imageSearchButton       =   new JButton("Search images");
        
        logoLabel.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 0));
        PromptSupport.setPrompt(" Search...", searchBar);
        searchBar.setPreferredSize(new Dimension(400, 35));
        searchBar.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        
        webSearchButton.addActionListener(this);
        imageSearchButton.addActionListener(this);
        
        searchPane.add(logoLabel);
        searchPane.add(Box.createRigidArea(new Dimension(searchPane.getPreferredSize().width, 20)));
        searchPane.add(searchBar);
        searchPane.add(Box.createRigidArea(new Dimension(searchPane.getPreferredSize().width, 10)));
        searchPane.add(webSearchButton);
        searchPane.add(imageSearchButton);
        searchPane.setBackground(Color.WHITE);
        searchPaneWrapper.add(searchPane, SEARCH_PANE_VIEW);
        
        viewPane.add(crawlPane);
        viewPane.add(searchPaneWrapper);
        
        viewPaneWrapper.add(viewPane, VIEW_PANE);
        viewPaneWrapper.add(transitionPane, TRANS_PANE_VIEW);
        
        JLabel crawlerTab   =   new JLabel("Crawler", JLabel.CENTER);
        JLabel searchTab    =   new JLabel("Search", JLabel.CENTER);
        crawlerTab.setIcon(new ImageIcon(spiderImage));
        searchTab.setIcon(new ImageIcon(searchIconDark));
        viewPane.setTabComponentAt(0, crawlerTab);
        viewPane.setTabComponentAt(1, searchTab); 
    }
    
    //Initializes the components for the multiple result panes
    //Includes components for result view (after search)
    private void initResultComponents()
    {
        resultsPaneWrapper                  =   new JPanel(new BorderLayout());
        resultsSearchPane                   =   new JPanel(new BorderLayout());
        resultsPageControls                 =   new JPanel();
        resultsCardWrapper                  =   new JPanel(new CardLayout());
        resultsRotatePane                   =   new JPanel(new CardLayout());
        currentSearchImages                 =   new ArrayList<>();
        prevPageButton                      =   new JButton("Prev");
        nextPageButton                      =   new JButton("Next");
        resultsSearchBar                    =   new JTextField();
        resultsSearchButton                 =   new JButton("Search");
        resultImageModel                    =   new ResultTableModel();
        resultModel                         =   new ResultTableModel();
        resultsTable                        =   new JTable(resultModel);
        resultsImagesTable                  =   new JTable(resultImageModel);
        resultControls                      =   new JPanel();
        noResultsPane                       =   new JPanel();
        filterNumResults                    =   new JComboBox();
        pageInfoLabel                       =   new JLabel();
        resultInfo                          =   new JLabel();
        modalOpen                           =   false;
        workingLabel                        =   new JLabel("Processing...");
        JPanel resultsSearchPaneWrapper     =   new JPanel();
        JPanel resultsInnerPaneWrapper      =   new JPanel();
        JPanel resultsCardInnerWrapper      =   new JPanel(new BorderLayout());
        final String COL_NAME               =   "Results";
        currentPage                         =   0;
        maxPage                             =   0;

        resultsCardWrapper.setBackground(Color.WHITE);
        resultsCardInnerWrapper.setBackground(Color.WHITE);
        
        //NO RESULTS PANE
        JLabel noResultsLabel    =   new JLabel("<html><center>No results were found</center><br>Try crawling or provide more specific search terms</html>");
        noResultsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        noResultsLabel.setForeground(Color.GRAY);
        noResultsPane.add(noResultsLabel);
        resultsCardWrapper.add(noResultsPane, NO_RESULTS_VIEW);
        
        workingLabel.setIcon(new ImageIcon(processingMiniImage));
        workingLabel.setVisible(false);
        prevPageButton.setEnabled(false);
        
        //PAGE INFO COMPONENTS
        //Prev/Next buttons and current & next page
        JPanel pageWrapper              =   new JPanel(new GridLayout(2, 1));
        JPanel pageInfoWrapper          =   new JPanel();
        pageWrapper.setBackground(Color.WHITE);
        pageInfoWrapper.setBackground(Color.WHITE);
        pageInfoWrapper.add(prevPageButton);
        pageInfoWrapper.add(pageInfoLabel);
        pageInfoWrapper.add(nextPageButton);
        pageWrapper.add(pageInfoWrapper);
        pageWrapper.add(workingLabel);
        
        resultsSearchButton.setIcon(new ImageIcon(searchIcon));
        resultsSearchButton.addActionListener(this);
        makeTransparent(resultsSearchButton);
        
        //Results per page combobox
        filterNumResults.addItem("16 Results");
        filterNumResults.addItem("32 Results");
        filterNumResults.addItem("46 Results");
        filterNumResults.addItemListener(new NumRowsChangeListener());
        
        //Page result info
        resultControls.setBackground(Color.WHITE);
        resultControls.add(resultInfo);
        resultControls.add(Box.createRigidArea(new Dimension(70, 1)));
        resultControls.add(new JLabel("Show results: "));
        resultControls.add(filterNumResults);
        
        //Results  table list selection listeners
        ImagesSelectionListener imageListener   =   new ImagesSelectionListener();
        resultsImagesTable.setCellSelectionEnabled(true);
        resultsTable.getSelectionModel().addListSelectionListener(new WebSelectionListener());
        resultsImagesTable.getSelectionModel().addListSelectionListener(imageListener);
        resultsImagesTable.getColumnModel().getSelectionModel().addListSelectionListener(imageListener);

        //Results Table components
        //Components used in web search
        JPanel resultsTableWrapper  =   new JPanel(new BorderLayout());
        resultsTableWrapper.setPreferredSize(new Dimension(400, 350));
        resultsTableWrapper.setBackground(Color.WHITE);
        
        resultsTable.setRowHeight(60);
        resultsTable.setBackground(Color.WHITE);
        
        resultsTableWrapper.add(new JScrollPane(resultsTable));
        resultsInnerPaneWrapper.setBackground(Color.WHITE);
        resultsInnerPaneWrapper.add(resultsTableWrapper);
        resultsRotatePane.add(resultsInnerPaneWrapper, WEB_RESULTS_PANE);
        
        resultModel.addColumn(COL_NAME);
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(new ResultCellRenderer());
        
        resultsSearchPane.setPreferredSize(new Dimension(WINDOW_WIDTH, 50));
        resultsSearchPaneWrapper.setPreferredSize(new Dimension(380, 35));
        resultsSearchBar.setPreferredSize(new Dimension(200, 35));
        
        //Result image table
        //Components used in image search
        JPanel resultsImageTableWrapper         =   new JPanel(new BorderLayout());
        JPanel resultsImageTableInnerWrapper    =   new JPanel();   
        resultsImageTableWrapper.setBackground(Color.WHITE);
        resultsImagesTable.setRowHeight(80);
        resultsImagesTable.setBackground(Color.WHITE);
        resultsImageTableInnerWrapper.setBackground(Color.WHITE);
        resultsImageTableWrapper.setBackground(Color.WHITE);
        
        resultsImageTableInnerWrapper.setPreferredSize(new Dimension(600, 350));
        resultsImagesTable.setPreferredScrollableViewportSize(new Dimension(600, 300));
        resultsImageTableInnerWrapper.add(new JScrollPane(resultsImagesTable));
        resultsImageTableWrapper.add(resultsImageTableInnerWrapper);
        resultsRotatePane.add(resultsImageTableWrapper, IMAGE_RESULTS_PANE);
        resultImageModel.addColumn("Results1");
        resultImageModel.addColumn("Results2");
        resultImageModel.addColumn("Results3");
        resultImageModel.addColumn("Results4");
        
        for(int i = 0; i < NUM_IMAGE_COLS; i++)
            resultsImagesTable.getColumnModel().getColumn(i).setCellRenderer(new ResultImageCellRenderer());
        
        resultsImagesTable.getTableHeader().setVisible(false);
        
        resultControls.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        resultsPaneWrapper.add(resultControls, BorderLayout.NORTH);
        resultsPaneWrapper.add(resultsRotatePane, BorderLayout.CENTER);
        
        searchBackButton    =   new JButton();
        searchBackButton.setIcon(new ImageIcon(processingImage));
        searchBackButton.addActionListener(this);
        makeTransparent(searchBackButton);
        
        resultsSearchPaneWrapper.add(searchBackButton);
        resultsSearchPaneWrapper.add(resultsSearchBar);
        resultsSearchPaneWrapper.add(resultsSearchButton);
        resultsSearchPane.add(resultsSearchPaneWrapper, BorderLayout.WEST);
        
        resultsSearchPaneWrapper.setBackground(Color.WHITE);
        resultsSearchPane.setBackground(Color.WHITE);
        resultsSearchPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        resultsPaneWrapper.setBackground(Color.WHITE);
        resultsPane.setBackground(Color.WHITE);
       
        resultsPageControls.setPreferredSize(new Dimension(WINDOW_WIDTH, 100));
        resultsPageControls.setBackground(Color.WHITE);
        resultsPageControls.add(pageWrapper);
        
        prevPageButton.addActionListener(this);
        nextPageButton.addActionListener(this);
        
        resultsCardInnerWrapper.add(resultsPaneWrapper, BorderLayout.CENTER);
        resultsCardInnerWrapper.add(resultsPageControls, BorderLayout.SOUTH);
        resultsCardWrapper.add(resultsCardInnerWrapper, HAS_RESULTS_VIEW);
        
        resultsPane.add(resultsSearchPane, BorderLayout.NORTH);
        resultsPane.add(resultsCardWrapper, BorderLayout.CENTER);
        searchPaneWrapper.add(resultsPane, RESULTS_PANE_VIEW);
    }
    
    //Initializes the crawler components in crawler view
    //These are components for start, stop, indexe controls etc.
    private void initCrawlerControlsComponents()
    {
        canResume                   =   false;
        rightPane                   =   new JPanel(new BorderLayout());
        crawlControls               =   new JPanel(new GridLayout(1, 4));
        rightPaneWrapper            =   new JPanel(new BorderLayout());
        crawlControls.setPreferredSize(new Dimension(580, (int) (WINDOW_HEIGHT * 0.1)));
        
        newIndexButton              =   new JButton("New index");
        openIndexButton             =   new JButton("Open index");
        playButton                  =   new JButton();
        stopButton                  =   new JButton();
        crawlSettingsButton         =   new JButton("Settings");
        JPanel filePanelWrapper     =   new JPanel(new GridLayout(2, 1));
        stopButton.setEnabled(false);
        
        //Crawler control icons
        //See resources for images
        newIndexButton.setIcon(new ImageIcon(newFileImage));
        openIndexButton.setIcon(new ImageIcon(openFileImage));
        playButton.setIcon(new ImageIcon(playButtonImage));
        stopButton.setIcon(new ImageIcon(stopButtonImage));
        crawlSettingsButton.setIcon(new ImageIcon(settingsImage));
        
        filePanelWrapper.add(newIndexButton);
        filePanelWrapper.add(openIndexButton);
        
        outputModel         =   new DefaultListModel();
        outputView          =   new JList(outputModel);
        outputView.setCellRenderer(new OuputCellRenderer());
        outputView.setForeground(Color.WHITE);
        outputView.setBackground(new Color(50, 50, 62));
        
        outputScroll    =   new JScrollPane(outputView);
        outputScroll.setBorder(null);

        rightPaneWrapper.add(outputScroll, BorderLayout.CENTER);
        
        crawlControls.add(filePanelWrapper);
        crawlControls.add(playButton);
        crawlControls.add(stopButton);
        crawlControls.add(crawlSettingsButton);
        
        rightPane.add(crawlControls, BorderLayout.NORTH);
        rightPane.add(rightPaneWrapper, BorderLayout.CENTER);
    }
    
    //Initializes keyword components
    //Keyword bar is used in crawler controls
    //Keywords used to filter crawl results
    private void initKeywordBarComponents()
    {
        keywordPanel    =   new BGRenderer(Color.WHITE, WINDOW_WIDTH, 100);
        keywordBar      =   new JTextField();
        keywordUpdate   =   new JButton();
        
        keywordPanel.setLayout(new BorderLayout());
        keywordBar.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        
        keywordUpdate.addActionListener(this);
        keywordUpdate.setIcon(new ImageIcon(updateMiniImage));
        
        keywordPanel.setBackground(Color.WHITE);
        
        keywordBar.setBackground(Color.WHITE);
        PromptSupport.setPrompt(" Enter keywords to search for deliminated by commas", keywordBar);
        
        keywordPanel.add(keywordBar, BorderLayout.CENTER);
        keywordPanel.add(keywordUpdate, BorderLayout.EAST);
        rightPaneWrapper.add(keywordPanel, BorderLayout.NORTH);
    }
    
    //Initializes the crawlers worker components
    //Components including add/remove workers and worker list
    private void initWorkerComponents()
    {
        leftPane            =   new JPanel(new GridLayout(2, 1));
        workerPanel         =   new JPanel(new BorderLayout());
        workerControls      =   new JPanel(new GridLayout(1, 2));
        
        
        leftPane.setBorder(new EmptyBorder(10, 0, 0, 0));
        leftPane.setPreferredSize(new Dimension(220, WINDOW_HEIGHT));
        workerPanel.setBorder(BorderFactory.createTitledBorder("Workers"));
        
        //Worker controls
        addWorkerButton     =   new JButton("Add");
        removeWorkerButton  =   new JButton("Remove");
        addWorkerButton.setIcon(new ImageIcon(miniMenuAddImage));
        removeWorkerButton.setIcon(new ImageIcon(miniMenuRemoveImage));
        
        addWorkerButton.addActionListener(this);
        removeWorkerButton.addActionListener(this);
        playButton.addActionListener(this);
        stopButton.addActionListener(this);
        newIndexButton.addActionListener(this);
        openIndexButton.addActionListener(this);
        crawlSettingsButton.addActionListener(this);
        
        //Worker list
        workerModel         =   new DefaultListModel();
        workerList          =   new JList(workerModel);
        workerScroll        =   new JScrollPane(workerList);
        workerScroll.setBorder(null);
        
        workerControls.add(addWorkerButton);
        workerControls.add(removeWorkerButton);
        
        workerPanel.add(workerControls, BorderLayout.NORTH);
        workerPanel.add(workerScroll, BorderLayout.CENTER);
    }
    
    //Initializes the crawlers URL queue components
    //Has components for add/remove and URL list
    //Queue is updated by Spider
    private void initURLMenuComponents()
    {
        urlPanel        =   new JPanel(new BorderLayout());
        urlControls     =   new JPanel(new GridLayout(1, 2));
        addUrlButton    =   new JButton("Add");
        removeUrlButton =   new JButton("Remove");
        urlModel        =   new DefaultListModel();
        urlList         =   new JList(urlModel);
        urlScroll       =   new JScrollPane(urlList);
        urlScroll.setBorder(null);
        
        addUrlButton.setIcon(new ImageIcon(miniMenuAddImage));
        removeUrlButton.setIcon(new ImageIcon(miniMenuRemoveImage));
        urlControls.add(addUrlButton);
        urlControls.add(removeUrlButton);
        
        urlPanel.add(urlScroll, BorderLayout.CENTER);
        urlPanel.add(urlControls, BorderLayout.NORTH);
        urlPanel.setBorder(BorderFactory.createTitledBorder("URL Queue"));
        
        addUrlButton.addActionListener(this);
        removeUrlButton.addActionListener(this);
        
        leftPane.add(urlPanel);
        leftPane.add(workerPanel);
        
        crawlPane.add(leftPane, BorderLayout.WEST);
        crawlPane.add(rightPane, BorderLayout.CENTER);
        crawlPane.setBackground(Color.GREEN);   
    }
    
    //A DefaultTableModel with cell editing disabled
    //Used by both tables in web & image search
    private class ResultTableModel extends DefaultTableModel
    {
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
    }
    
    //The web searches (resultsTable) table cell renderer
    //Each cell displays the IndexNodes title, URL and description
    //Additional info can be shown when expanded
    //Listener for cell selection: WebSelectionListener
    private class ResultCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
            Component parent        =   super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            JLabel titleLabel, descriptionLabel, addressLabel;
            JPanel resultCellPanel  =   new JPanel(new GridLayout(3, 1));
            resultCellPanel.setBackground(Color.WHITE);
            
            IndexNode cellNode      =   (IndexNode) value;
            
            
            String title            =   cellNode.getTitle();
            String description      =   cellNode.getDescription();
            String URL              =   cellNode.getURL();
            
            titleLabel              =   new JLabel(title);
            descriptionLabel        =   new JLabel(description);
            addressLabel            =   new JLabel(URL);
            
            titleLabel.setFont(new Font("Seif", Font.BOLD, 12));
            addressLabel.setForeground(Color.BLUE);
            
            resultCellPanel.add(titleLabel);
            resultCellPanel.add(addressLabel);
            resultCellPanel.add(descriptionLabel);
            
            JLabel parentLabel  =   (JLabel) parent;
            
            resultCellPanel.setBorder(parentLabel.getBorder());
            resultCellPanel.setBackground(parentLabel.getBackground());
            
            return resultCellPanel;
        }
    }
    
    //The image searches (resultsImageTable) table cell renderer
    //Displays a scaled image of the ImageNode's image
    private class ResultImageCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
            ImageNode imageNode     =   (ImageNode) value;
            JPanel cellPanel        =   new JPanel()
            {
                @Override
                public void paintComponent(Graphics g)
                {
                    super.paintComponent(g);
                    
                    if(imageNode != null)
                    {
                        Image image =   imageNode.getImage();
                        if(image != null)
                            g.drawImage(imageNode.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, getWidth(), getHeight(), null);
                    }
                    
                }
            };
            
            cellPanel.setBackground(Color.WHITE);
            
            //Black border on selection grey border normal
            if(resultsImagesTable.getSelectedRow() == row && resultsImagesTable.getSelectedColumn() == column) 
                cellPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            else 
                cellPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
            return cellPanel;
        }
    }
    
    //The cell renderer for the crawlers output list
    //Displays the workers name, output message and colour it
    private class OuputCellRenderer extends JLabel implements ListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
        {
            setText(value.toString());
            
            //Filter workers name
            Pattern p   =   Pattern.compile("^\\[(.*?)\\]");
            Matcher m   =   p.matcher(value.toString());
            
            Color workerColour;
            String workerName   =   null;
            
            if(m.find())
            {
                workerName      =   m.group(1);
                workerColour    =   spider.getWorkersColour(workerName);
                setForeground(workerColour);
            }
            
            else setForeground(Color.WHITE);
            return this;
        }
    }
    
    //Class for renderering a background colour
    //Draws a bg with colour of bgWidth and bgHeight
    private class BGRenderer extends JPanel
    {
        private final Color bg;
        private final int bgWidth, bgHeight;
        
        public BGRenderer(Color bg, int bgWidth, int bgHeight)
        {
            this.bg         =   bg;
            this.bgWidth    =   bgWidth;
            this.bgHeight   =   bgHeight;
            
        }
        
        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setColor(bg);
            g.fillRect(0, 0, bgWidth, bgHeight);
        }
    }
    
    //Returns any keywords entered in crawlers keywordBar
    //Splits keywords deliminated by ","
    private String[] getKeywordsFromBar()
    {
        String text = keywordBar.getText();
        
        if(text.equals("")) return null;
        else return text.split("\\s*,\\s*");
    }
    
    //Shows a dialog to add a new crawler worker
    //Contains form for crawlers name and colour
    private void promptAddWorker()
    {
        JPanel workerSettingsPanel  =   new JPanel(new BorderLayout());
        JPanel colourPanel          =   new JPanel(new GridLayout(1, 2));
        JButton colourButton        =   new JButton("Colour");
        JLabel colourLabel          =   new JLabel("Choose colour");
        JLabel nameLabel            =   new JLabel("Enter workers name");
        colourButton.setBackground(Color.WHITE);
        colourButton.setIcon(new ImageIcon(colorIconImage));
        
        //Show colour chooser dialog
        //Allow user to pick crawlers output colour
        colourButton.addActionListener((ActionEvent e) -> 
        {
             Color c = JColorChooser.showDialog(null, "Choose worker colour", Color.WHITE);
             colourButton.setBackground(c);
        });
       
        colourPanel.add(colourLabel);
        colourPanel.add(colourButton);
        workerSettingsPanel.add(colourPanel, BorderLayout.NORTH);
        workerSettingsPanel.add(nameLabel);
        
        String name         =   JOptionPane.showInputDialog(null, workerSettingsPanel, "Add worker", JOptionPane.INFORMATION_MESSAGE);
        Color workerColour  =   colourButton.getBackground();
        
        if(name == null) return;
        
        if(name.equals(""))
            JOptionPane.showMessageDialog(null, "Invalid worker settings");
        else if(spider.getWorker(name) != null)
            JOptionPane.showMessageDialog(null, "Worker names must be unique");
        else
        {
             spider.addWorker(name, workerColour);
             workerModel.addElement(name);
        }
    }
    
    //Removes a worker from the crawlers worker pane
    //If worker is selected removes directly otherwise shows dialog
    private void promptRemoveWorker()
    {
        if(spider.numWorkers() == 0) 
            JOptionPane.showMessageDialog(null, "No workers to remove");
        else
        {
            String workerName;
            if(workerList.getSelectedValue() == null)
                workerName = JOptionPane.showInputDialog(null, "Enter workers name to remove", "Remove worker", JOptionPane.INFORMATION_MESSAGE);
            else
                workerName  = workerList.getSelectedValue().toString();
            
            if(workerName != null && !spider.removeWorker(workerName))
                JOptionPane.showMessageDialog(null, "Failed to remove worker");
            
            else workerModel.removeElement(workerName);
            
        }
    }
    
    //Shows dialog for adding a seed URL
    //URL's are validated before adding 
    private void promptAddSeed()
    {
        String seedURL  =   JOptionPane.showInputDialog(null, "Enter a valid URL", "Add URL", JOptionPane.INFORMATION_MESSAGE);
        
        if(seedURL == null) return;
        
        if(!Spider.validateURL(seedURL))
            JOptionPane.showMessageDialog(null, "Invalid URL");
        else
        {
            spider.addSeedURL(seedURL);
            urlModel.addElement(seedURL);
        }
    }
    
    //Shows dialog for removing a URL from queue
   //If url is selected, removes directly otherwise shows dialog
    private void promptRemoveSeed()
    {
        if(spider.numSeeds() == 0)
            JOptionPane.showMessageDialog(null, "No URL's to remove");
        else
        {
            String url;
            if(urlList.getSelectedValue() == null)
                url =   JOptionPane.showInputDialog(null, "Enter the URL to remove", "Remove URL", JOptionPane.INFORMATION_MESSAGE);
            else
                url =   urlList.getSelectedValue().toString();
            
            if(url != null && !spider.removeSeedUrl(url))
                JOptionPane.showMessageDialog(null, "Failed to remove URL");
            else
                urlModel.removeElement(url);
        }
    }
    
    //Starts the crawler
    //Seed URL's, workers, keywords and indexer need to be added before starting
    //Crawlers button functions from menu are handled
    private void startCrawler()
    {
        String[] enteredKeywords    =   getKeywordsFromBar();
        
        if(spider.numSeeds() == 0)
            JOptionPane.showMessageDialog(null, "Please add some seed URL's");
        
        else if(spider.numWorkers() == 0)
            JOptionPane.showMessageDialog(null, "Please add atleast one worker");
        
        else if(enteredKeywords == null)
            JOptionPane.showMessageDialog(null, "Please enter some keywords to search for");
        
        else if(spider.getIndexer() == null)
            JOptionPane.showMessageDialog(null, "No index found, please create or open one");
        
        else
        {
            playButton.setEnabled(false);
            startCrawlerItem.setEnabled(false);
            stopButton.setEnabled(true);
            stopCrawlerItem.setEnabled(true);
            spider.initKeywords(enteredKeywords);
            
            if(!canResume) spider.crawl();
            else 
            {
                spider.resumeCrawling();
                canResume = false;
            }
        }
    }
    
    //Stops the crawler
    //Handles enabling/disabling of crawler controls and menu
    //Output may still occur if workers are finishing
    private void stopCrawler()
    {
        playButton.setEnabled(true);
        startCrawlerItem.setEnabled(true);
        stopButton.setEnabled(false);
        stopCrawlerItem.setEnabled(false);
        canResume   =   true;
        spider.stopCrawling();
    }    
    
    //Updates the crawlers keywords
    //Keywords by default are collected at startCrawler()
    //Can be used to add/remove keywords during crawling
    private void updateKeywords()
    {
        String[] enteredKeywords    =   getKeywordsFromBar();
        if(enteredKeywords == null)
            JOptionPane.showMessageDialog(null, "Please enter some keywords to search for");
        else
            spider.initKeywords(enteredKeywords);
    }
    
    //Outputs to the crawlers main output view with message
    //Called by spiders workers and is synced appropriatly
    //Typically output when a worker crawls something
    public static synchronized void sendOutput(String message)
    {
        if(outputModel != null)
        {
            SwingUtilities.invokeLater(() ->
            {
                outputModel.addElement(message);
                outputScroll.getVerticalScrollBar().setValue(outputScroll.getVerticalScrollBar().getMaximum());
            });
        }
    }
    
    //Sends a URL to the queue
    //Spider workers use when it finds URL's within a crawled page
    public static synchronized void sendUrlQueue(String url)
    {
        if(urlModel != null)
        {
            SwingUtilities.invokeLater(() ->
            {
               urlModel.addElement(url);
            });
        }
    }
    
    //Removes a URL from the queue
    //Spider workers call when they have crawled a URL
    public static synchronized void sendUrlQueueRemove(String url)
    {
        if(urlModel != null)
        {
            SwingUtilities.invokeLater(() -> 
            {
                urlModel.removeElement(url);
            });
        }
    }
    
    //Show a main view
    //Includes transition view and standard view pane
    private void showMainView(String viewName)
    {
        currentLayoutView   =   viewName;
        CardLayout cLayout  =   (CardLayout) viewPaneWrapper.getLayout();
        cLayout.show(viewPaneWrapper, viewName);
    }
    
    //Show a search view
    //Includes search engine view and result view
    private void showSearchView(String viewName)
    {
        currentSearchView   =   viewName;
        CardLayout cLayout  =   (CardLayout) searchPaneWrapper.getLayout();
        cLayout.show(searchPaneWrapper, viewName);
    }
    
    //Show either the results view or no results view
    //Results view shows relavent resultsTablView (web/image) 
    //No results view shows message stating no results found
    private void showResultsView(boolean hasResults)
    {
        CardLayout cLayout  =   (CardLayout) resultsCardWrapper.getLayout();
        if(hasResults)
            cLayout.show(resultsCardWrapper, HAS_RESULTS_VIEW);
        else
            cLayout.show(resultsCardWrapper, NO_RESULTS_VIEW);
    }
    
    //Show results table view
    //Results table view are contained in resultsRotatePane
    //Includes web and image search tables
    //Should be called appropriatly when searching for web or images
    private void showResultsTableView(String viewName)
    {
        CardLayout cLayout  =   (CardLayout) resultsRotatePane.getLayout();
        cLayout.show(resultsRotatePane, viewName);
    }
    
    //Performs a search from keywords used in search engines bar or results bar
    //Transition is shown for 2 seconds before showing results view
    //Search destination is dependent on search_web boolean,
    //set true for web search or false for image search
    //Querying and further page updates is done by paginate()
    private void search()
    {
        JTextField searchField  =   (currentSearchView.equals(SEARCH_PANE_VIEW))? searchBar : resultsSearchBar;
        String searchTerm       =   searchField.getText();
        
        showTransition(2000);
                
        currentPage = 1;
        showResultsView(true);
        paginate(1, getResultsPerPage());

        if(searchWeb) showResultsTableView(WEB_RESULTS_PANE);
        else showResultsTableView(IMAGE_RESULTS_PANE);

        
        showSearchView(RESULTS_PANE_VIEW);
        resultsSearchBar.setText(searchTerm);
    }
    
    //Searches the web
    //Shows web links found
    private void searchWeb()
    {
        searchWeb = true;
        search();
    }
    
    //Searches for images
    //Shows images found
    private void searchImages()
    {
        searchWeb = false;
        search();
    }
    
    //Sets the resultInfos number of results found
    //Called when new search to show how many results found
    private void updateNumResults(int numResults)
    {
        String message  =   MessageFormat.format("Found {0} results", numResults);
        resultInfo.setText(message);
    }
    
    //Updates the page info label
    //Including the current page and max number of pages
    private void updatePageLabel(int current, int max)
    {
        String info =   MessageFormat.format("Page {0}/{1}", current, max);
        pageInfoLabel.setText(info);
    }
    
    //Changes to the next page (if possible)
    //Handles prev/next button enabled/disabled
    //Updates lables, current page and paginates
    private void nextPage()
    {
        if(currentPage + 1 <= maxPage)
        {
            prevPageButton.setEnabled(true);
            if(currentPage + 2 > maxPage)
                nextPageButton.setEnabled(false);
            
            paginate(++currentPage, getResultsPerPage());
            updatePageLabel(currentPage, maxPage);
        }
    }
    
    //Changes to the prev page (if possible)
    //Handles prev/next butotn enabled/disabled
    //Updated lables, current page and paginates
    private void prevPage()
    {
        if(currentPage - 1 >= 1)
        {
            nextPageButton.setEnabled(true);
            if(currentPage -2 < 1)
                prevPageButton.setEnabled(false);
            
            paginate(--currentPage, getResultsPerPage());
            updatePageLabel(currentPage, maxPage);
        }
    }
    
    //Shows the transition view for a duration
    //Transition view should be called when searching
    //or making changes that are expected to take long
    private void showTransition(int duration)
    {
        showMainView(TRANS_PANE_VIEW);
        Timer task =   new Timer(duration, (ActionEvent e) ->
        {
            showMainView(VIEW_PANE);
        });
        
        task.setRepeats(false);
        task.start();
    }
    
    //Makes a JButton transparent
    private void makeTransparent(JButton component)
    {
        component.setOpaque(false);
        component.setContentAreaFilled(false);
        component.setBorderPainted(false);
    }
    
    //Shows a dialog for displaying and changing settings
    //Includes all settings used in spiders config
    //Sets and updates spider config appropriatly
    private void showCrawlerSettings()
    {
        SpiderConfig config =   spider.getConfig();
        
        JPanel crawlerSettingsPanel     =   new JPanel(new GridLayout(8, 2));
        JLabel crawlDistanceLabel       =   new JLabel("Max crawl distance");
        JLabel maxAdjLinksLabel         =   new JLabel("Max adjacent URL's");
        JLabel bufferSizeLabel          =   new JLabel("Buffer size");
        JLabel cacheSizeLabel           =   new JLabel("Cache size");
        JLabel updateDurationLabel      =   new JLabel("Duration (days) until update");
        JLabel maxImagesLabel           =   new JLabel("Max number of images");
        JLabel collectImagesLabel       =   new JLabel("Fetch images");
        JLabel defaultIndexFileLabel    =   new JLabel("Default index file");
        JTextField crawlDistanceField   =   new JTextField();
        JTextField maxAdjLinksField     =   new JTextField();
        JTextField bufferSizeField      =   new JTextField();
        JTextField cacheSizeField       =   new JTextField();
        JTextField updateDurationField  =   new JTextField();
        JTextField maxImagesField       =   new JTextField();
        JTextField indexFileField       =   new JTextField();
        JCheckBox collectImagesCheck    =   new JCheckBox();
        
        crawlerSettingsPanel.add(crawlDistanceLabel);
        crawlerSettingsPanel.add(crawlDistanceField);
        crawlerSettingsPanel.add(maxAdjLinksLabel);
        crawlerSettingsPanel.add(maxAdjLinksField);
        crawlerSettingsPanel.add(bufferSizeLabel);
        crawlerSettingsPanel.add(bufferSizeField);
        crawlerSettingsPanel.add(cacheSizeLabel);
        crawlerSettingsPanel.add(cacheSizeField);
        crawlerSettingsPanel.add(updateDurationLabel);
        crawlerSettingsPanel.add(updateDurationField);
        crawlerSettingsPanel.add(maxImagesLabel);
        crawlerSettingsPanel.add(maxImagesField);
        crawlerSettingsPanel.add(defaultIndexFileLabel);
        crawlerSettingsPanel.add(indexFileField);
        crawlerSettingsPanel.add(collectImagesLabel);
        crawlerSettingsPanel.add(collectImagesCheck);
        
        crawlDistanceField.setText("" + config.getMaxCrawlDistance());
        maxAdjLinksField.setText("" + config.getMaxAdjUrls());
        bufferSizeField.setText("" + config.getBufferSize());
        cacheSizeField.setText("" + config.getCacheSize());
        updateDurationField.setText("" + config.getUpdateDays());
        maxImagesField.setText("" + config.getMaxImages());
        collectImagesCheck.setSelected(config.isFetchingImages());
        indexFileField.setText(config.getDatabaseFile());
        
        int option = JOptionPane.showConfirmDialog(null, crawlerSettingsPanel, "Crawler settings", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.YES_OPTION)
        {
            try
            {
                int maxDistance         =    Integer.parseInt(crawlDistanceField.getText());
                int maxLinks            =    Integer.parseInt(maxAdjLinksField.getText());
                int bufferSize          =    Integer.parseInt(bufferSizeField.getText());
                int cacheSize           =    Integer.parseInt(cacheSizeField.getText());
                int updateDays          =    Integer.parseInt(updateDurationField.getText());
                int maxImages           =    Integer.parseInt(maxImagesField.getText());
                String defaultIndex     =    indexFileField.getText();
                boolean fetchImages     =    collectImagesCheck.isSelected();
                
                config.setBufferSize(bufferSize);
                config.setCacheSize(cacheSize);
                config.setMaxCrawlDistance(maxDistance);
                config.setMaxAdjUrls(maxLinks);
                config.setUpdateDays(updateDays);
                config.setFetchImages(fetchImages);
                config.setMaxImages(maxImages);
                config.setDatabaseFile(defaultIndex);
                
                spider.updateConfig();
            }
            
            catch(NumberFormatException e)
            {
                JOptionPane.showMessageDialog(null, "Invalid config settings");
            }
        }
    }
    
    //Shows dialog with my details :3
    private void showAuthorDetails()
    {
        String message  =   "Author: Kyle Russell\nUniversity: Auckland University of Technology\nContact: specialk.jn@gmail.com";
        JOptionPane.showMessageDialog(null, message, "Author information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    //Shows dialog for adding a new index/DB file
    //Prompts for the name of the new index file
    //Adds the file to data/indexname.db
    //Allows option to make default in which case updates spider config
    private void promptNewIndexFile()
    {
        JPanel message          =   new JPanel(new GridLayout(2, 1));
        JLabel messageLabel     =   new JLabel("Enter name for new index database");
        JPanel defaultWrapper   =   new JPanel();
        JLabel defaultLabel     =   new JLabel("Make this the default index? ");
        JCheckBox makeDefault   =   new JCheckBox();  
        
        defaultWrapper.add(defaultLabel);
        defaultWrapper.add(makeDefault);
        message.add(defaultWrapper);
        message.add(messageLabel);
        
        String fileName         =   JOptionPane.showInputDialog(null, message, "Create new index file", JOptionPane.OK_CANCEL_OPTION);
        if(fileName != null && !fileName.equalsIgnoreCase(""))
        {
            spider.setIndexer(Indexer.createIndexer("data/" + fileName + ".db"));
            searchEngine.setIndexer(spider.getIndexer());
            
            if(makeDefault.isSelected())
            {
                spider.getConfig().setDatabaseFile("data/" + fileName + ".db");
                spider.updateConfig();
            }
        }
    }
    
    //Shows dialog for opening an index file
    //Prompts for the path of the file or user can choose
    //Allows option to make default in which case updates spider config
    private void promptOpenIndexFile()
    {
        JPanel message          =   new JPanel(new GridLayout(3, 1));
        JLabel messageLabel     =   new JLabel("Enter the location of the index .db file");
        JPanel defaultWrapper   =   new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel inputWrapper     =   new JPanel();
        JButton fileOpenButton  =   new JButton("Find");
        JLabel defaultLabel     =   new JLabel("Make this the default index? ");
        JCheckBox makeDefault   =   new JCheckBox();  
        JTextField fileField    =   new JTextField();
        fileField.setPreferredSize(new Dimension(200, 25));
        
        
        defaultWrapper.add(defaultLabel);
        defaultWrapper.add(makeDefault);
        inputWrapper.add(fileField);
        inputWrapper.add(fileOpenButton);
        message.add(defaultWrapper);
        message.add(messageLabel);
        message.add(inputWrapper);
        
        fileOpenButton.addActionListener((ActionEvent e) -> 
        {
            JFileChooser jfc    =   new JFileChooser();
            int fileOpt         =   jfc.showOpenDialog(null);
            
            if(fileOpt == JFileChooser.APPROVE_OPTION)
                fileField.setText(jfc.getSelectedFile().getPath());
        });
        
        int option  =   JOptionPane.showConfirmDialog(null, message, "Open index file", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if(option != JOptionPane.CANCEL_OPTION)
        {
            String fileName =   fileField.getText();
            spider.setIndexer(Indexer.createIndexer(fileName));
            searchEngine.setIndexer(spider.getIndexer());
            
            if(makeDefault.isSelected())
            {
                spider.getConfig().setDatabaseFile(fileName);
                spider.updateConfig();
            }
        }
    }
    
    //Exits the app safely
    //Stops workers if crawling
    //Should be called as default close 
    private void exitApp()
    {
        if(spider.isCrawling())
        {
            stopCrawler();
            showTransition(1000);
            Timer task  =   new Timer(1000, (ActionEvent e)->
            {
                System.exit(0);
            });

            task.setRepeats(false);
            task.start();
        }
        
        else System.exit(0);
    }
    
    //Attempts to open a URL in the users default browser
    //Desktop is only supported on Windows (tested as well on linux)
    //If supported by OS, opens the page to the address
    private void openPage(String address)
    {
        String os   =   System.getProperty("os.name").toLowerCase();
        
        if(os.contains("win"))
        {
            if(Desktop.isDesktopSupported())
            {
                Desktop desktop =   Desktop.getDesktop();
                if(desktop.isSupported(Desktop.Action.BROWSE))
                {
                    try { desktop.browse(new URI(address)); }

                    catch(IOException | URISyntaxException e)
                    {
                        JOptionPane.showMessageDialog(null, "Could not open page");
                    }
                }
            }
        }
        
        else 
            JOptionPane.showMessageDialog(null, "Cannot open page, system is not supported");
    }
    
    //Copies the text to the users clipboard
    private void copyToClipboard(String text)
    {
        Clipboard cb    =   Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(new StringSelection(text), null);
    }
    
    //Returns the max number of pages based on results per page and numResults
    //Formula: ceil(numResults/resultsPerPage)
    private int getMaxPages(int numResults, int resultsPerPage)
    {
        return (int) Math.ceil((double) (numResults / (resultsPerPage * 1.0)));
    }
    
    //Returns the page offset 
    //Formula: resultsPerPage * index (index = pageNum - 1)
    //Prevents overflowing and negative offsets
    private int getPageOffset(int pageNum, int resultsPerPage, int numPages)
    {
        if(pageNum > numPages) pageNum = numPages;
        
        int index   =   (pageNum > 0)? (pageNum - 1) : 0;
        int offset  =   resultsPerPage * index;
        return offset;
    }
    
    //Returns the number of results per page
    //Depending on the currently selected index of filterNumResults
    //Add to or change where nessary, 0: 16, 1: 32, 3: 46
    private int getResultsPerPage()
    {
        final int RESULTS_0 =   16;
        final int RESULTS_1 =   32;
        final int RESULTS_2 =   46;
        
        int index = filterNumResults.getSelectedIndex();
        switch(index)
        {
            case 0: return RESULTS_0;
            case 1: return RESULTS_1;
            case 2: return RESULTS_2;
            default: return 0;
        }
    }
    
    //Adds the images from the ImageNode node to currentSearchImages
    //Adds up to n or numResultsPerPage images to currentSearchImages
    //Images are read before adding to improve rendering performance 
    private void addNodesImages(IndexNode node)
    {
        int maxImages           =   spider.getConfig().getMaxImages();
        int n                   =   Math.min(node.getImages().size(), maxImages);
        Iterator<String> iter   =   node.getImages().iterator();
        int numResultsPerPage   =   getResultsPerPage();
        
        for(int i = 0; i < n && currentSearchImages.size() <= numResultsPerPage; i++)
        {
            try
            {
                String image_url    =   iter.next();
                BufferedImage image =   ImageIO.read(new URL(image_url));
                
                currentSearchImages.add(new ImageNode(image_url, image));
            }
            
            catch(IOException e) {}
        }
    }
    
    //A node for the IndexNodes images
    //Holds the nodes image and image URL
    //Used specifically by resultsImagesTable renderers
    private class ImageNode
    {
        private BufferedImage image; //The image used
        private String url; //The image URL
        
        public ImageNode(String url, BufferedImage image)
        {
            this.url    =   url;
            this.image  =   image;
        }
        
        //Returns image URL
        public String getUrl()
        {
            return url;
        }
        
        //Returns the image
        public BufferedImage getImage()
        {
            return image;
        }
        
        //Set the image URL
        public void setUrl(String url)
        {
            this.url    =   url;
        }
        
        //Set the image
        public void setImage(BufferedImage image)
        {
            this.image  =   image;
        }
        
        @Override
        public String toString()
        {
            return url;
        }
    }
    
    //Responsible for page transitions and producing search results
    //Fetches results for the search term entered 
    //Handles empty searchs by redirecting to NO_RESULTS view
    private void paginate(int pageNum, int resultsPerPage)
    {
        workingLabel.setVisible(true);
        JTextField searchField  =   (currentSearchView.equals(SEARCH_PANE_VIEW))? searchBar : resultsSearchBar;
        String searchTerm       =   searchField.getText();
        List<IndexNode> results =   searchEngine.search(searchTerm, !searchWeb);
        
        //No results, show NO_RESULTS view
        if(results.isEmpty())
        {
            showResultsView(false);
            return;
        }
        
        Timer task = new Timer(500, (ActionEvent e) ->
        {   
            int numResults              =   results.size();
            int maxPages                =   getMaxPages(numResults, resultsPerPage);
            int pageOffset              =   getPageOffset(pageNum, resultsPerPage, maxPages);
            int numCols                 =   4;
            
            SwingUtilities.invokeLater(() -> 
            {
                //Searching web
                //Clear resultModel
                //Add numResultsPerPage to the resultModel
                if(searchWeb)
                {
                    resultModel.setRowCount(0);
                    for(int i = pageOffset, j = 0; i < numResults && j < resultsPerPage; i++, j++)
                        resultModel.addRow(new IndexNode[] { results.get(i) });
                }
                
                //Searching images
                //Clear resultImageModel (rendering handled)
                //Clear the stored images
                //Add new IndexNodes images to stored images
                else
                {
                    resultImageModel.setRowCount(0);
                    currentSearchImages.clear();
                    
                    for(int i = pageOffset; i < numResults; i++)
                        addNodesImages(results.get(i));
                    
                    //Gets row data images from currentSearchImages
                    //Fills unused columns with null
                    //Adds up to numCols images per row
                    //Images are already read and can be quickly rendered
                    for(int i = 0; i < currentSearchImages.size();)
                    {
                        List<ImageNode> rowData =   new ArrayList<>();
                        for(int j = 0; j < numCols; j++)
                        {
                            ImageNode node  =   (i < currentSearchImages.size() - 1)? currentSearchImages.get(i) : null;
                            rowData.add(node);
                            i++;
                        }
                        resultImageModel.addRow(rowData.toArray(new ImageNode[numCols]));
                    }
                }
                
                //Update the number of results with new search result count
                updateNumResults(results.size());
                
                //Update page info
                int numPages    =   getMaxPages(results.size(), getResultsPerPage());
                maxPage         =   numPages;
                updatePageLabel(currentPage, numPages);
                
            });
            
            workingLabel.setVisible(false);
        });
        
        task.setRepeats(false);
        task.start();
    }
    
    //filterNumResults comboBox listener for setting the number of results per page
    //On change limits the number of rows
    private class NumRowsChangeListener implements ItemListener
    {
        @Override
        public void itemStateChanged(ItemEvent e) 
        {
            if(e.getStateChange() == ItemEvent.SELECTED)
            {
                limitRows();
            }
        }
        
    }
    
    //Filters the number of results per page
    //Stays on current page (may be pushed back/forward)
    private void limitRows()
    {
        paginate(currentPage, getResultsPerPage());
    }
    
    //Selection listener for web searches resultsTable
    //On selection shows the dialog for the IndexNode
    private class WebSelectionListener implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            if(!e.getValueIsAdjusting())
            {
                int row       =   resultsTable.getSelectedRow();
                int col       =   resultsTable.getSelectedColumn();
                
                if(row >= 0 && col >= 0)
                {
                    IndexNode selectedNode  =   (IndexNode) resultModel.getValueAt(row, col);
                    showWebResultForNode(selectedNode);
                }
            }
        }
    }
    
    //Selectionlistener for image searches resultsImagesTable
    //Selection listener added to both column and row model (synced by modalOpen)
    //On selection shows the dialog for the selected ImageNode
    private class ImagesSelectionListener implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            if(!e.getValueIsAdjusting() && !modalOpen)
            {
                modalOpen = true;
                int row     =   resultsImagesTable.getSelectedRow();
                int col     =   resultsImagesTable.getSelectedColumn();
                
                
                if(row >= 0 && col >= 0)
                {
                    ImageNode selectedNode  =   (ImageNode) resultImageModel.getValueAt(row, col);
                    showImageResultsForNode(selectedNode);
                }
            }
        }
    }
    
    //Limits the number of chars in a string by max_chars
    //Adds ".." at end to indicate string was limited
    private String limitString(String str, int max_chars)
    {
        if(str != null && str.length() > max_chars)
        {
            return str.substring(0, max_chars - 2) + "..";
        }
        
        else return str;
    }
    
    //Shows a dialog for the IndexNode
    //Provides information about the IndexNode
    //Including it's URL, parent URL, title, description, updated date and page rank
    //Provides option to open page in browser and copy pages URL to clipboard
    private void showWebResultForNode(IndexNode node)
    {
        if(node == null) return;
        
        final int MAX_CHARS =   25;   
        JPanel panel        =   new JPanel(new GridLayout(7, 2));
        
        panel.add(new JLabel("URL"));
        panel.add(new JLabel(limitString(node.getURL(), MAX_CHARS)));
        panel.add(new JLabel("Parent URL"));
        panel.add(new JLabel(limitString(node.getParent(), MAX_CHARS)));
        panel.add(new JLabel("Title"));
        panel.add(new JLabel(limitString(node.getTitle(), MAX_CHARS)));
        panel.add(new JLabel("Description"));
        panel.add(new JLabel(limitString(node.getDescription(), MAX_CHARS + 10)));
        panel.add(new JLabel("Last updated"));
        panel.add(new JLabel(limitString(node.getLastUpdated().toString(), MAX_CHARS + 10)));
        panel.add(new JLabel("Page rank"));
        panel.add(new JLabel(limitString("" + node.getRank(), 12)));
        
        JButton openInBrowser   =   new JButton("Open in browser");
        JButton copyClipboard   =   new JButton("Copy to clipboard");
        openInBrowser.setIcon(new ImageIcon(webIconImage));
        copyClipboard.setIcon(new ImageIcon(clipboardImage));
        panel.add(openInBrowser);
        panel.add(copyClipboard);
        
        ActionListener listener = (ActionEvent e) -> 
        {
            Object src = e.getSource();
            
            //Open page at URL
            if(src == openInBrowser)
                openPage(node.getURL());
            
            //Copy URL to clipboard
            else if(src == copyClipboard)
                copyToClipboard(node.getURL());
        };
        
        openInBrowser.addActionListener(listener);
        copyClipboard.addActionListener(listener);
        

        
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JDialog modal   =   new JDialog(frame);
        modal.getContentPane().add(panel);
        modal.setLocation(frame.getWidth() - panel.getSize().width, frame.getHeight() / 2 - panel.getSize().height);
        modal.setTitle("Address Data");
        modal.pack();
        
        modal.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                resultsTable.clearSelection();
                modalOpen = false;
            }
        });
        
        modal.setVisible(true);
    }
    
    //Shows dialog for the ImageNode node
    //Displays information about the ImageNode 
    //Including the width, height and URL of image
    //Gives option to open image in browser (at URL) 
    //And copy image URL to clipboard
    private void showImageResultsForNode(ImageNode node)
    {
        if(node == null) return;
        
        SwingUtilities.invokeLater(() ->
        {
        final int MAX_CHARS     =   15;
        JPanel panel            =   new JPanel(new BorderLayout());
        JPanel infoPanel        =   new JPanel(new GridLayout(4, 2));
        JPanel imageWrapper     =   new JPanel();
        JLabel imageLabel       =   new JLabel(new ImageIcon(node.getImage()));
        
        infoPanel.add(new JLabel("Width"));
        infoPanel.add(new JLabel(node.getImage().getWidth() + "px"));
        infoPanel.add(new JLabel("Height"));
        infoPanel.add(new JLabel(node.getImage().getHeight() + "px"));
        infoPanel.add(new JLabel("URL"));
        infoPanel.add(new JLabel(limitString(node.getUrl(), MAX_CHARS)));
        
        JButton openInBrowser   =   new JButton("Open in browser");
        JButton copyClipboard   =   new JButton("Copy to clipboard");
        openInBrowser.setIcon(new ImageIcon(webIconImage));
        copyClipboard.setIcon(new ImageIcon(clipboardImage));
        infoPanel.add(openInBrowser);
        infoPanel.add(copyClipboard);
        
        imageWrapper.add(imageLabel);
        imageWrapper.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.LIGHT_GRAY));
        panel.add(imageWrapper, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        ActionListener listener = (ActionEvent e) -> 
        {
            Object src = e.getSource();
            
            //Open image in browser
            if(src == openInBrowser)
                openPage(node.getUrl());
            
            //Copy image URL to clipboard
            else if(src == copyClipboard)
                copyToClipboard(node.getUrl());
        };
        
        openInBrowser.addActionListener(listener);
        copyClipboard.addActionListener(listener);
        
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JDialog modal   =   new JDialog(frame);
        modal.getContentPane().add(panel);
        modal.setLocation(frame.getWidth() - panel.getSize().width, frame.getHeight() / 2 - panel.getSize().height);
        modal.setTitle("Image Data");
        modal.pack();
        
        modal.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                resultsImagesTable.clearSelection();
                modalOpen = false;
            }
        });
        
        modal.setVisible(true);  
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object src  =   e.getSource();
        
        if(src == addWorkerButton)
            promptAddWorker();
        
        else if(src == removeWorkerButton)
            promptRemoveWorker();
        
        else if(src == playButton || src == startCrawlerItem)
            startCrawler();
        
        else if(src == stopButton || src == stopCrawlerItem)
            stopCrawler();
        
        else if(src == addUrlButton)
            promptAddSeed();
        
        else if(src == removeUrlButton)
            promptRemoveSeed();
        
        else if(src == searchBackButton)
            showSearchView(SEARCH_PANE_VIEW);
        
        else if(src == webSearchButton || src == resultsSearchButton)
            searchWeb();
        
        else if(src == imageSearchButton)
            searchImages();
        
        else if(src == prevPageButton)
            prevPage();
        
        else if(src == nextPageButton)
            nextPage();
        
        else if(src == crawlSettingsButton || src == preferences)
            showCrawlerSettings();
        
        else if(src == author)
            showAuthorDetails(); 
        
        else if(src == newIndexButton || src == newIndexItem)
            promptNewIndexFile();
        
        else if(src == openIndexButton || src == openIndexItem)
            promptOpenIndexFile();
        
        else if(src == keywordUpdate)
            updateKeywords();
        
        else if(src == exit)
            exitApp();
        
    }

    public static void main(String[] args)
    {
        JFrame frame    =   new JFrame("NotGoogle");
        SpiderGUI gui   =   new SpiderGUI(frame);
        
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            //Use the guis exitApp as hook
            @Override
            public void windowClosing(WindowEvent e)
            {
                gui.exitApp();
            }
        });
                
        frame.getContentPane().add(gui);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
