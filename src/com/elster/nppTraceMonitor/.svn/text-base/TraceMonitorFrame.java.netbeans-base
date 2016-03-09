package com.elster.nppTraceMonitor;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.elster.nppTraceMonitor.db.ModuleTrace;
import com.elster.nppTraceMonitor.db.Module;
import com.elster.nppTraceMonitor.db.Trace;
import com.elster.nppTraceMonitor.db.*;
import com.elster.nppTraceMonitor.net.*;
import com.elster.nppTraceMonitor.utils.BitUtils;
import com.elster.nppTraceMonitor.utils.SystemUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.*;
import java.io.*;
import java.io.File;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.BatchUpdateException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.indirection.IndirectList;




/**
 *
 * @author wesselst
 */
public class TraceMonitorFrame extends javax.swing.JFrame implements IConnectionListener, IConnectionReaderListener {

    private String      mySdCardTopDir;
    private String      myEditor;
    private Connection  myConnection;
    
    // Elapsed ticks of the device since system start
    private long ticks;
    
    // Timestamp of device. Usually send once after 'connect' command
    private long utc;
    
    // Saves timestamp of the NEWEST record. used to query even newer items in the next cycle
    private Date lastTableRefresh = new Date(0);
    
    // Swing timer that updates the Trace Table
    private javax.swing.Timer refreshTimer;
    
    // Swing timer to check connectivity
    private javax.swing.Timer watchdogTimer;
    
    // Trace Table update interval. Increases performance. Gets new entries from DB after delay has passed
    private static final int TIMER_DELAY = 1000;
    
    // A second entity manager for the refreshTimer, so I don't need to synchronize the other one
    private javax.persistence.EntityManager entityMgr;
    
    // Standard Query to get all traces and may contain filters from the FilterDialog as well
    private String moduleTraceQueryString;

    // This class needs to remember all filters the user made in the FilterDialog
    private Vector<Object[]> moduleTraceFilters = new Vector<Object[]>();
    
    // Associates a trace level enum with a color. Map is initialized in constructor
    private EnumMap<TraceLevel, Color> colorMap = new EnumMap<TraceLevel, Color>(TraceLevel.class);
    
    // Ticks per miliseconds is based on the cpu speed of the device (~99MHz)
    private static final int TICKS_PER_MILISECOND = 388;
    
    // Simple Console Logger
    private static final Logger LOGGER = Logger.getLogger(TraceMonitorFrame.class.getName());
    private LoggingWindowHandler handler = null;
    
    // Mutex that protects the moduleTraceList, which can be altered by this class and the connectionReader thread
    private final Object LOCK = new Object();

    // Mutex that protects the EntityManager
    private final Object EMLOCK = new Object();
    
    
    // Used to calculate the duration of a call like this: 
    // 1: startTime = System.nanoTime();
    // <your code here>
    // 2: duration = System.nanoTime() - startTime;
    private long startTime = 0;
    /**
     * Creates new form TraceMonitorFrame
     */
    public TraceMonitorFrame() {
        
        initComponents();
        
        // Load TraceColors
        loadTraceColors();

        // Load Connection info
        loadConnectionInfo();
        myConnection = new Connection(this, hostEdt.getText (), TMP.PORT);

        // Load Paths
        loadPaths();
        
        // Standard Filter string for all incoming traces
        moduleTraceQueryString = "Select m from ModuleTrace m";
        
        // Window adapter to react on close event
        this.addWindowListener(new WindowEventHandler());

        // Change the default cell renderer of the trace table
        traceTbl.setDefaultRenderer(Date.class,     new MyTableCellRenderer());

        
        // Default rows to sort
        DefaultRowSorter sorter = ((DefaultRowSorter)fileTbl.getRowSorter());
        ArrayList list = new ArrayList();
        list.add( new RowSorter.SortKey(1, SortOrder.ASCENDING) );
        sorter.setSortKeys(list);
        sorter.sort();
        
        sorter = ((DefaultRowSorter)traceTbl.getRowSorter() );
        list = new ArrayList();
        list.add( new RowSorter.SortKey(0, SortOrder.ASCENDING) );
        sorter.setSortKeys(list);
        sorter.sort();       
        
        // Add Selection Listener to fileTbl
        ModuleTblSelectionListener moduleTblSelectionListener = new ModuleTblSelectionListener( moduleTbl );
        moduleTbl.getSelectionModel().addListSelectionListener( moduleTblSelectionListener );
        moduleTbl.getColumnModel().getSelectionModel().addListSelectionListener(moduleTbl);
        
        
        // Initialize Timer
        refreshTimer    = new javax.swing.Timer (TIMER_DELAY, new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                // Every second the timer will call this function to get all new records from
                // database. This way the performance is much better, than getting every record one by one
                refreshTable();
            }
        });
//        watchdogTimer   = new javax.swing.Timer (TMP.IDLE_PERIOD, new ActionListener () {
//            @Override
//            public void actionPerformed (ActionEvent e) {
//                myConnection.disconnect ();
//            }
//        });
        entityMgr       = javax.persistence.Persistence.createEntityManagerFactory("NbNppTraceMonitorPU").createEntityManager();
        
        // Logging
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.FINER);
        LOGGER.addHandler(console);
        
//        handler = LoggingWindowHandler.getInstance ();
//        handler.setLevel(Level.FINER);
//        LOGGER.addHandler(handler);
        
        LOGGER.setLevel(Level.FINER);


    }

    public void loadTraceColors() {
        Properties prop = new Properties();
        try {
            InputStream is = new FileInputStream( new File( SystemUtils.defaultPropertiesDirectory (), "tracecolors.properties") );
            prop.load(is);
            colorMap.put(TraceLevel.ALL,    Color.BLACK);
            colorMap.put(TraceLevel.EMERG,  new Color(Integer.parseInt(prop.getProperty ("emerg", "-36482")) ));
            colorMap.put(TraceLevel.CRIT,   new Color(Integer.parseInt(prop.getProperty ("crit", "-83714")) ));
            colorMap.put(TraceLevel.ERR,    new Color(Integer.parseInt(prop.getProperty ("err", "-18814")) ));
            colorMap.put(TraceLevel.WARN,   new Color(Integer.parseInt(prop.getProperty ("warn", "-3682")) ));
            colorMap.put(TraceLevel.NOTICE, new Color(Integer.parseInt(prop.getProperty ("notice", "-3735668")) ));
            colorMap.put(TraceLevel.INFO,   new Color(Integer.parseInt(prop.getProperty ("info", "-720962")) ));
            colorMap.put(TraceLevel.DEBUG,  new Color(Integer.parseInt(prop.getProperty ("debug", "-3342388")) ));
            colorMap.put(TraceLevel.ALL,    Color.BLACK );
        
            return;
        } catch( FileNotFoundException e) {
            LOGGER.log(Level.INFO, "Properties were not written before. Assigning standard values");
        } catch(IOException e) { 
            LOGGER.log(Level.WARNING, "IOException while reading tracecolors.properties");
        } catch( NumberFormatException e) {
            LOGGER.log(Level.WARNING, "NumberFormatException while reading tracecolors.properties");
        }
            colorMap.put(TraceLevel.ALL,    Color.BLACK);
            colorMap.put(TraceLevel.EMERG,  new Color(-36482));
            colorMap.put(TraceLevel.CRIT,   new Color(-83714));
            colorMap.put(TraceLevel.ERR,    new Color(-18814));
            colorMap.put(TraceLevel.WARN,   new Color(-3682));
            colorMap.put(TraceLevel.NOTICE, new Color(-3735668));
            colorMap.put(TraceLevel.INFO,   new Color(-720962));
            colorMap.put(TraceLevel.DEBUG,  new Color(-3342388));
            colorMap.put(TraceLevel.ALL,    Color.BLACK );
        
        
    }
       
    private void loadConnectionInfo() {
        Properties properties = new Properties();
//        InputStream in = getClass().getResourceAsStream ("/com/elster/nppTraceMonitor/resources/connection.properties");
        try {
            InputStream in = new FileInputStream( new File( SystemUtils.defaultPropertiesDirectory (), "connection.properties") );
            properties.load(in);
            hostEdt.setText ( properties.getProperty ("host", "10.49.121.10") );
            System.out.println("Read: " + hostEdt.getText () );
            return;
        } catch( FileNotFoundException e) {
            LOGGER.log(Level.INFO, "Properties were not written before. Assigning standard values");
        } catch(IOException e) { 
            LOGGER.log(Level.WARNING, "IOException while reading properties: {0}", e);
        }
        hostEdt.setText("10.49.121.10");
    }
        
    public void loadPaths() {
        Properties properties = new Properties();
        try {
            InputStream in = new FileInputStream( new File( SystemUtils.defaultPropertiesDirectory (), "paths.properties") );
            properties.load(in);
            mySdCardTopDir = properties.getProperty ("traceFilesDir", "Y:\\SDCard\\Traces");
            myEditor = properties.getProperty ("editor", "");
            return;
        } catch( FileNotFoundException e) {
            LOGGER.log(Level.INFO, "Properties were not written before. Assigning standard values");
        } catch(IOException e) { 
            LOGGER.log(Level.WARNING, "IOException while reading properties: {0}", e);
        }
        mySdCardTopDir = "Y:\\SDCard\\Traces";  
    }
            
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        NbNppTraceMonitorPUEntityManager = java.beans.Beans.isDesignTime() ? null : javax.persistence.Persistence.createEntityManagerFactory("NbNppTraceMonitorPU").createEntityManager();
        moduleQuery = java.beans.Beans.isDesignTime() ? null : NbNppTraceMonitorPUEntityManager.createQuery("SELECT m FROM Module m");
        moduleList = java.beans.Beans.isDesignTime() ? java.util.Collections.emptyList() : org.jdesktop.observablecollections.ObservableCollections.observableList(moduleQuery.getResultList());
        fileQuery = java.beans.Beans.isDesignTime() ? null : NbNppTraceMonitorPUEntityManager.createQuery("SELECT f FROM File f");
        fileList = java.beans.Beans.isDesignTime() ? java.util.Collections.emptyList() : org.jdesktop.observablecollections.ObservableCollections.observableList(fileQuery.getResultList());
        moduleTraceQuery = java.beans.Beans.isDesignTime() ? null : NbNppTraceMonitorPUEntityManager.createQuery("SELECT m FROM ModuleTrace m");
        moduleTraceList = java.beans.Beans.isDesignTime() ? java.util.Collections.emptyList() : org.jdesktop.observablecollections.ObservableCollections.observableList(moduleTraceQuery.getResultList());
        fileTableContextMnu = new javax.swing.JPopupMenu();
        debugLvlMenuItem = new javax.swing.JMenuItem();
        infoLvlMenuItem = new javax.swing.JMenuItem();
        noticeLvlMenuItem = new javax.swing.JMenuItem();
        warnLvlMenuItem = new javax.swing.JMenuItem();
        errorLvlMenuItem = new javax.swing.JMenuItem();
        critLvlMenuItem = new javax.swing.JMenuItem();
        emergLvlMenuItem = new javax.swing.JMenuItem();
        traceTableContextMnu = new javax.swing.JPopupMenu();
        filterByMenu = new javax.swing.JMenu();
        filterByModuleIdMenuItem = new javax.swing.JMenuItem();
        filterByFileNameMenuItem = new javax.swing.JMenuItem();
        filterByLineMenuItem = new javax.swing.JMenuItem();
        filterByLogLevelMenuItem = new javax.swing.JMenuItem();
        filterByMessageMenuItem = new javax.swing.JMenuItem();
        LeftRightSplit = new javax.swing.JSplitPane();
        leftSplit = new javax.swing.JSplitPane();
        moduleTblScrollPane = new javax.swing.JScrollPane();
        moduleTbl = new javax.swing.JTable();
        fileTblScrollPane = new javax.swing.JScrollPane();
        fileTbl = new javax.swing.JTable();
        traceTblScrollPane = new javax.swing.JScrollPane();
        traceTbl = new javax.swing.JTable() {
            private Border obenUnten            = new MatteBorder(1, 0, 1, 0, Color.GREEN);
            private Border weisserRahmen        = new MatteBorder(1, 0, 1, 0, Color.WHITE);
            private Border seitenRand           = new MatteBorder(0, 1, 0, 1, Color.WHITE);
            private Object level;

            @Override
            public Component prepareRenderer(
                TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                level = getModel().getValueAt(row, 4);
                if( !isRowSelected (row)) {
                    if( level instanceof Integer)
                    {
                        c.setBackground( colorMap.get( TraceLevel.getLevel((Integer)level) ) );
                    }
                }
                traceTbl.setIntercellSpacing ( new Dimension(0, 2 ));
                return c;
            }
        };
        toolbar = new javax.swing.JToolBar();
        hostEdt = new javax.swing.JTextField();
        connectBtn = new javax.swing.JButton();
        playBtn = new javax.swing.JButton();
        pauseBtn = new javax.swing.JButton();
        discardBtn = new javax.swing.JButton();
        searchBtn = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        closeItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        colorMnuItem = new javax.swing.JMenuItem();
        pathMnuItem = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        loggingConsoleItem = new javax.swing.JMenuItem();

        debugLvlMenuItem.setText("7 - DEBUG");
        debugLvlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugLvlMenuItemActionPerformed(evt);
            }
        });
        fileTableContextMnu.add(debugLvlMenuItem);

        infoLvlMenuItem.setText("6 - INFO");
        infoLvlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoLvlMenuItemActionPerformed(evt);
            }
        });
        fileTableContextMnu.add(infoLvlMenuItem);

        noticeLvlMenuItem.setText("5 - NOTICE");
        noticeLvlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noticeLvlMenuItemActionPerformed(evt);
            }
        });
        fileTableContextMnu.add(noticeLvlMenuItem);

        warnLvlMenuItem.setText("4 - WARN");
        warnLvlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warnLvlMenuItemActionPerformed(evt);
            }
        });
        fileTableContextMnu.add(warnLvlMenuItem);

        errorLvlMenuItem.setText("3 - ERROR");
        errorLvlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorLvlMenuItemActionPerformed(evt);
            }
        });
        fileTableContextMnu.add(errorLvlMenuItem);

        critLvlMenuItem.setText("2 - CRIT");
        critLvlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                critLvlMenuItemActionPerformed(evt);
            }
        });
        fileTableContextMnu.add(critLvlMenuItem);

        emergLvlMenuItem.setText("1 - EMERG");
        emergLvlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emergLvlMenuItemActionPerformed(evt);
            }
        });
        fileTableContextMnu.add(emergLvlMenuItem);

        filterByMenu.setText("Filter By:");

        filterByModuleIdMenuItem.setText("Module ID");
        filterByModuleIdMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterByModuleIdMenuItemActionPerformed(evt);
            }
        });
        filterByMenu.add(filterByModuleIdMenuItem);

        filterByFileNameMenuItem.setText("File Name");
        filterByFileNameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterByFileNameMenuItemActionPerformed(evt);
            }
        });
        filterByMenu.add(filterByFileNameMenuItem);

        filterByLineMenuItem.setText("Line");
        filterByLineMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterByLineMenuItemActionPerformed(evt);
            }
        });
        filterByMenu.add(filterByLineMenuItem);

        filterByLogLevelMenuItem.setText("Log Level");
        filterByLogLevelMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterByLogLevelMenuItemActionPerformed(evt);
            }
        });
        filterByMenu.add(filterByLogLevelMenuItem);

        filterByMessageMenuItem.setText("Message");
        filterByMessageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterByMessageMenuItemActionPerformed(evt);
            }
        });
        filterByMenu.add(filterByMessageMenuItem);

        traceTableContextMnu.add(filterByMenu);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        LeftRightSplit.setDividerLocation(350);

        leftSplit.setDividerLocation(400);
        leftSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        moduleTbl.setAutoCreateRowSorter(true);
        moduleTbl.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        moduleTbl.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, moduleList, moduleTbl);
        org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${moduleId}"));
        columnBinding.setColumnName("Module Id");
        columnBinding.setColumnClass(Integer.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${name}"));
        columnBinding.setColumnName("Name");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        moduleTblScrollPane.setViewportView(moduleTbl);
        moduleTbl.getColumnModel().getColumn(0).setMaxWidth(50);

        leftSplit.setLeftComponent(moduleTblScrollPane);

        fileTbl.setAutoCreateRowSorter(true);
        fileTbl.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        fileTbl.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, fileList, fileTbl);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${logLevel}"));
        columnBinding.setColumnName("Log Level");
        columnBinding.setColumnClass(Integer.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${fileName}"));
        columnBinding.setColumnName("File Name");
        columnBinding.setColumnClass(String.class);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        fileTbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileTblMouseReleased(evt);
            }
        });
        fileTblScrollPane.setViewportView(fileTbl);
        fileTbl.getColumnModel().getColumn(0).setMaxWidth(50);

        leftSplit.setRightComponent(fileTblScrollPane);

        LeftRightSplit.setLeftComponent(leftSplit);

        traceTbl.setAutoCreateRowSorter(true);
        traceTbl.setFont(new java.awt.Font("Lucida Console", 0, 14)); // NOI18N
        traceTbl.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        traceTbl.setAutoscrolls(false);
        traceTbl.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, moduleTraceList, traceTbl);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${timestamp}"));
        columnBinding.setColumnName("Timestamp");
        columnBinding.setColumnClass(java.util.Date.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${moduleId.moduleId}"));
        columnBinding.setColumnName("Module Id");
        columnBinding.setColumnClass(Integer.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${traceId.fileId.fileName}"));
        columnBinding.setColumnName("File");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${traceId.line}"));
        columnBinding.setColumnName("Line");
        columnBinding.setColumnClass(Integer.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${logLevel}"));
        columnBinding.setColumnName("Log Level");
        columnBinding.setColumnClass(Integer.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${decodedMessage}"));
        columnBinding.setColumnName("Decoded Message");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        traceTbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                traceTblMouseReleased(evt);
            }
        });
        traceTblScrollPane.setViewportView(traceTbl);
        traceTbl.getColumnModel().getColumn(0).setPreferredWidth(130);
        traceTbl.getColumnModel().getColumn(0).setMaxWidth(130);
        traceTbl.getColumnModel().getColumn(1).setPreferredWidth(35);
        traceTbl.getColumnModel().getColumn(1).setMaxWidth(50);
        traceTbl.getColumnModel().getColumn(2).setPreferredWidth(300);
        traceTbl.getColumnModel().getColumn(2).setMaxWidth(400);
        traceTbl.getColumnModel().getColumn(3).setPreferredWidth(50);
        traceTbl.getColumnModel().getColumn(3).setMaxWidth(50);
        traceTbl.getColumnModel().getColumn(4).setMaxWidth(80);

        LeftRightSplit.setRightComponent(traceTblScrollPane);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        hostEdt.setText("10.49.121.10");
        hostEdt.setToolTipText("IP-Address of NPP-Device");
        hostEdt.setMaximumSize(new java.awt.Dimension(120, 2147483647));
        hostEdt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hostEdtKeyReleased(evt);
            }
        });
        toolbar.add(hostEdt);

        connectBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/elster/nppTraceMonitor/resources/plug_con.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/elster/nppTraceMonitor/Bundle"); // NOI18N
        connectBtn.setText(bundle.getString("TraceMonitorPanel.connectBtn.text")); // NOI18N
        connectBtn.setToolTipText("Connect/Disconnect to/from Device");
        connectBtn.setFocusable(false);
        connectBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        connectBtn.setName("connect");
        connectBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });
        toolbar.add(connectBtn);

        playBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/elster/nppTraceMonitor/resources/play.png"))); // NOI18N
        playBtn.setText(bundle.getString("TraceMonitorPanel.playBtn.text")); // NOI18N
        playBtn.setToolTipText("Continue Updating Trace Messages");
        playBtn.setEnabled(false);
        playBtn.setFocusable(false);
        playBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        playBtn.setName("play");
        playBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        playBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playBtnActionPerformed(evt);
            }
        });
        toolbar.add(playBtn);

        pauseBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/elster/nppTraceMonitor/resources/pause.png"))); // NOI18N
        pauseBtn.setText(bundle.getString("TraceMonitorPanel.pauseBtn.text")); // NOI18N
        pauseBtn.setToolTipText("Stop Updating Trace Messages");
        pauseBtn.setEnabled(false);
        pauseBtn.setFocusable(false);
        pauseBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pauseBtn.setName("pause");
        pauseBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pauseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseBtnActionPerformed(evt);
            }
        });
        toolbar.add(pauseBtn);

        discardBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/elster/nppTraceMonitor/resources/discard.png"))); // NOI18N
        discardBtn.setText(bundle.getString("TraceMonitorPanel.discardBtn.text")); // NOI18N
        discardBtn.setToolTipText("Discard all saved Traces");
        discardBtn.setFocusable(false);
        discardBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        discardBtn.setName("discard");
        discardBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        discardBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discardBtnActionPerformed(evt);
            }
        });
        toolbar.add(discardBtn);

        searchBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/elster/nppTraceMonitor/resources/search_icon.png"))); // NOI18N
        searchBtn.setText(bundle.getString("TraceMonitorPanel.searchBtn.text")); // NOI18N
        searchBtn.setToolTipText("Filter Traces");
        searchBtn.setFocusable(false);
        searchBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        searchBtn.setName("search");
        searchBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });
        toolbar.add(searchBtn);

        jMenu1.setText("File");

        closeItem.setText("Close");
        closeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeItemActionPerformed(evt);
            }
        });
        jMenu1.add(closeItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Options");

        colorMnuItem.setText("Colors");
        colorMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorMnuItemActionPerformed(evt);
            }
        });
        jMenu2.add(colorMnuItem);

        pathMnuItem.setText("Paths");
        pathMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathMnuItemActionPerformed(evt);
            }
        });
        jMenu2.add(pathMnuItem);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");

        loggingConsoleItem.setText("Logging Console");
        loggingConsoleItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggingConsoleItemActionPerformed(evt);
            }
        });
        jMenu3.add(loggingConsoleItem);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(LeftRightSplit)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LeftRightSplit, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE))
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
        if (hostEdt.getText ().split("\\.").length != 4  ) {
            JOptionPane.showMessageDialog(null, "Connection Error", "No proper IP-Address is set!", JOptionPane.OK_CANCEL_OPTION);
            return;
        }


        if (connectBtn.getName().equalsIgnoreCase("connect")) {
            // Change Icon to 'disconnect'
            connectBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/elster/nppTraceMonitor/resources/plug_dis.png")));
            connectBtn.setName("disconnect");
            
            // Activate 'Pause' icon
            pauseBtn.setEnabled (true);
            discardBtn.setEnabled ( false );
            
            connect();

        } else {
            disconnect();
        }
    }//GEN-LAST:event_connectBtnActionPerformed

    private void discardBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discardBtnActionPerformed
       
        try {
//        DELETE query
        String query;
        query = "DELETE FROM ModuleTrace m";
        entityMgr.getTransaction().begin();
            int numOfDeleted = entityMgr.createQuery(query).executeUpdate();
            LOGGER.log(Level.INFO, "Removed {0} entries from tabe 'MODULE_TRACE'", numOfDeleted);
            JOptionPane.showMessageDialog(this, "Removed "+numOfDeleted+" entries from table 'MODULE_TRACE'", "SQL Update", JOptionPane.INFORMATION_MESSAGE);
        entityMgr.getTransaction().commit();
        
//        Clear observable list
        moduleTraceList.clear();
        } 
        catch( PersistenceException e) {
            LOGGER.log(Level.SEVERE, "PersistenceException while discarding traces");
            // This happens if the trace is too long for the database field
            // You might ask yourself why this exception is thrown here. well, since
            // jpa is quasi-transactional an exception can occur everywhere data is read
            // or updated as well
        }
            
    }//GEN-LAST:event_discardBtnActionPerformed

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        
        new FilterDialog(this, true).setVisible(true);
        
    }//GEN-LAST:event_searchBtnActionPerformed

    private void fileTblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileTblMouseReleased
        if (evt.isPopupTrigger()) {
            
            JTable source = (JTable)evt.getSource();
            int row = source.rowAtPoint( evt.getPoint() );
            int column = source.columnAtPoint( evt.getPoint() );
            
            if(!source.isRowSelected( row) )
            {
                source.changeSelection(row, column, false, false);
            }
            
            fileTableContextMnu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_fileTblMouseReleased

    private void emergLvlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emergLvlMenuItemActionPerformed
        setLogLevelForModelInFileTable(TraceLevel.EMERG);
    }//GEN-LAST:event_emergLvlMenuItemActionPerformed

    private void critLvlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_critLvlMenuItemActionPerformed
        setLogLevelForModelInFileTable(TraceLevel.CRIT);
    }//GEN-LAST:event_critLvlMenuItemActionPerformed

    private void errorLvlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorLvlMenuItemActionPerformed
        setLogLevelForModelInFileTable(TraceLevel.ERR);
    }//GEN-LAST:event_errorLvlMenuItemActionPerformed

    private void warnLvlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warnLvlMenuItemActionPerformed
        setLogLevelForModelInFileTable(TraceLevel.WARN);
    }//GEN-LAST:event_warnLvlMenuItemActionPerformed

    private void noticeLvlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noticeLvlMenuItemActionPerformed
        setLogLevelForModelInFileTable(TraceLevel.NOTICE);
    }//GEN-LAST:event_noticeLvlMenuItemActionPerformed

    private void infoLvlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoLvlMenuItemActionPerformed
        setLogLevelForModelInFileTable(TraceLevel.INFO);
    }//GEN-LAST:event_infoLvlMenuItemActionPerformed

    private void debugLvlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugLvlMenuItemActionPerformed
        setLogLevelForModelInFileTable(TraceLevel.DEBUG);
    }//GEN-LAST:event_debugLvlMenuItemActionPerformed

    private void playBtnActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playBtnActionPerformed
        playBtn.setEnabled(false);
        pauseBtn.setEnabled (true);
        discardBtn.setEnabled( false );
        
        refreshTimer.start();
    }//GEN-LAST:event_playBtnActionPerformed

    private void pauseBtnActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseBtnActionPerformed
        pauseBtn.setEnabled(false);
        playBtn.setEnabled( true );
        discardBtn.setEnabled( true );
        
        refreshTimer.stop();
    }//GEN-LAST:event_pauseBtnActionPerformed

    private void traceTblMouseReleased (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_traceTblMouseReleased
        if (evt.isPopupTrigger()) {
            
            JTable source = (JTable)evt.getSource();
            int row = source.rowAtPoint( evt.getPoint() );
            int column = source.columnAtPoint( evt.getPoint() );
            
            if(!source.isRowSelected( row) )
            {
                source.changeSelection(row, column, false, false);
            }
            
            traceTableContextMnu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
        else {
            // left mouse button 
            if (evt.getClickCount() == 2 && !evt.isConsumed()) {
                evt.consume();
                //handle double click event.
                JTable source = (JTable)evt.getSource();
                int row = source.rowAtPoint( evt.getPoint() );
                ModuleTrace clickedModuleTrace = moduleTraceList.get( source.convertRowIndexToModel (row) );
                if(clickedModuleTrace != null) {
                    int line = clickedModuleTrace.getTraceId ().getLine();
                    String fileName = clickedModuleTrace.getTraceId ().getFileId ().getFileName ();
                    
                    // open afb file
                    File theFile = new File( "Y:\\AFB\\NPP_AFB_"+clickedModuleTrace.getModuleId ().getName ().split("-")[0]+"\\"+fileName );
                    if( theFile.exists () ) {
                        openFileInEditor( theFile.getAbsolutePath (), line );
                    }
                    
                    // open sfb file
                    theFile = new File( "Y:\\SFB\\NPP_SFB_"+clickedModuleTrace.getModuleId ().getName ().split("-")[0]+"\\"+fileName );
                    if( theFile.exists () ) {
                        openFileInEditor( theFile.getAbsolutePath (), line );
                    }
                    
                    // open core file
                    theFile = new File( "Y:\\NPP_CORE_System\\"+fileName );
                    if( theFile.exists () ) {
                        openFileInEditor( theFile.getAbsolutePath (), line );
                    }
                    
                }
            }

        }
    }//GEN-LAST:event_traceTblMouseReleased

    private void filterByModuleIdMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterByModuleIdMenuItemActionPerformed
        ModuleTrace selectedTrace = getSelectedTrace ();
        if( selectedTrace == null ) {
            return;
        }
        
        Integer moduleId = selectedTrace.getModuleId ().getModuleId ();
        if(moduleTraceFilters.size() > 0 )
        {
            moduleTraceFilters.add (0, new Object[]{ "moduleId.moduleId", "=", moduleId.toString (), "AND" } );
        }
        else
        {
            moduleTraceFilters.add (0, new Object[]{ "moduleId.moduleId", "=", moduleId.toString (), "" } );
        }
        
        
    }//GEN-LAST:event_filterByModuleIdMenuItemActionPerformed

    private void filterByFileNameMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterByFileNameMenuItemActionPerformed
        ModuleTrace selectedTrace = getSelectedTrace ();
        if( selectedTrace == null ) {
            return;
        }

        String fileName = selectedTrace.getTraceId ().getFileId ().getFileName ();
        if(moduleTraceFilters.size() > 0 )
        {
            moduleTraceFilters.add (0, new Object[]{ "traceId.fileId.fileName", "=", fileName, "AND" } );
        }
        else
        {
            moduleTraceFilters.add (0, new Object[]{ "traceId.fileId.fileName", "=", fileName, "" } );
        }
    }//GEN-LAST:event_filterByFileNameMenuItemActionPerformed

    private void filterByLineMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterByLineMenuItemActionPerformed
        ModuleTrace selectedTrace = getSelectedTrace ();
        if( selectedTrace == null ) {
            return;
        }

        Integer line = selectedTrace.getTraceId ().getLine ();
        if(moduleTraceFilters.size() > 0 )
        {
            moduleTraceFilters.add (0, new Object[]{ "traceId.line", "=", line.toString (), "AND" } );
        }
        else
        {
            moduleTraceFilters.add (0, new Object[]{ "traceId.line", "=", line.toString (), "" } );
        }
    }//GEN-LAST:event_filterByLineMenuItemActionPerformed

    private void filterByLogLevelMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterByLogLevelMenuItemActionPerformed
        ModuleTrace selectedTrace = getSelectedTrace ();
        if( selectedTrace == null ) {
            return;
        }
        

        int level = selectedTrace.getLogLevel ();
        if(moduleTraceFilters.size() > 0 )
        {
            moduleTraceFilters.add (0, new Object[]{ "logLevel", "=", level, "AND" } );
        }
        else
        {
            moduleTraceFilters.add (0, new Object[]{ "logLevel", "=", level, "" } );
        }
    }//GEN-LAST:event_filterByLogLevelMenuItemActionPerformed

    private void filterByMessageMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterByMessageMenuItemActionPerformed
        ModuleTrace selectedTrace = getSelectedTrace ();
        if( selectedTrace == null ) {
            return;
        }
        

        String message = selectedTrace.getDecodedMessage ();
        if(moduleTraceFilters.size() > 0 )
        {
            moduleTraceFilters.add (0, new Object[]{ "decodedMessage", "=", message, "AND" } );
        }
        else
        {
            moduleTraceFilters.add (0, new Object[]{ "decodedMessage", "=", message, "" } );
        }
    }//GEN-LAST:event_filterByMessageMenuItemActionPerformed

    private void hostEdtKeyReleased (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hostEdtKeyReleased
        System.out.println(evt.getKeyCode () == KeyEvent.VK_ENTER);
        
        if( evt.getKeyCode () == KeyEvent.VK_ENTER )
        {
            try {
                Properties prop = new Properties();
                OutputStream out = new FileOutputStream( new File( SystemUtils.defaultPropertiesDirectory (), "connection.properties") );
                
                prop.setProperty ("host", hostEdt.getText ());
                prop.store(out, "");
                System.out.println("Wrote: " + hostEdt.getText () );
                myConnection.setHost (hostEdt.getText() );
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
    
            } catch(IOException e) { 
                LOGGER.log(Level.WARNING, "IOException while writing properties");
            }
        }
    }//GEN-LAST:event_hostEdtKeyReleased

    private void colorMnuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorMnuItemActionPerformed
        new ColorDialog (this, rootPaneCheckingEnabled).setVisible (true);
    }//GEN-LAST:event_colorMnuItemActionPerformed

    private void pathMnuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathMnuItemActionPerformed
        new PathDialog (this, rootPaneCheckingEnabled).setVisible (true);
    }//GEN-LAST:event_pathMnuItemActionPerformed

    private void closeItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeItemActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeItemActionPerformed

    private void loggingConsoleItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggingConsoleItemActionPerformed
        LoggingWindowHandler h = LoggingWindowHandler.getInstance ();
        h.getLoggingWindow ().setVisible(true);
    }//GEN-LAST:event_loggingConsoleItemActionPerformed

    private ModuleTrace getSelectedTrace() {
        // Get FILE object from selected table row
        ModuleTrace selectedTrace=null;
        try
        {
            selectedTrace = moduleTraceList.get( traceTbl.convertRowIndexToModel( traceTbl.getSelectedRow() ) );
        }
        catch( ArrayIndexOutOfBoundsException e)
        {
            LOGGER.log( Level.SEVERE, "Selected Row has no Model");
            return null;
        }
        if( selectedTrace == null )
        {
            LOGGER.log( Level.SEVERE, "Selected Row has no Model");
            return null;
        }
        
        return selectedTrace;
    }
    
    /**
     * Connects to the device if user presses
     * 'connect' button
     */
    public void     connect() {
        // Clear database
        dropAllTables ();
        
        // Start refreshing the traceTbl
        refreshTimer.start();
        
        // Listen for connection changes
        myConnection.setHost (hostEdt.getText() );
        myConnection.addListener(this);
        myConnection.connect();

    }

    /**
     * Disconnects from device if user presses
     * 'disconnect' button
     */
    public void     disconnect() {
        if( myConnection != null)
        {
            myConnection.disconnect();
        }
    }
    
    private void    dropAllTables() {
        try
        {
            // Clear view
            moduleList.clear();
            fileList.clear();
            moduleTraceList.clear();

            // Delete all entries from db
            NbNppTraceMonitorPUEntityManager.clear ();
            NbNppTraceMonitorPUEntityManager.getTransaction ().begin ();
                // Table ModuleTrace
                NbNppTraceMonitorPUEntityManager.createQuery ("DELETE from ModuleTrace", ModuleTrace.class).executeUpdate ();
                NbNppTraceMonitorPUEntityManager.createQuery ("DELETE from ModuleTraceBuffer", ModuleTraceBuffer.class).executeUpdate ();
                NbNppTraceMonitorPUEntityManager.createQuery ("DELETE from Trace", Trace.class).executeUpdate ();
                NbNppTraceMonitorPUEntityManager.createQuery ("DELETE from Module", Module.class).executeUpdate ();
                NbNppTraceMonitorPUEntityManager.createQuery ("DELETE from File", File.class).executeUpdate ();
            NbNppTraceMonitorPUEntityManager.getTransaction ().commit ();
        }
        catch( RollbackException e)
        {
            LOGGER.log( Level.SEVERE, "RollbackException: Cannot drop tables");
        }
    }

    
    /**
    * Gets the 'File' entity class object out of the JTable and 
    * sets the log level to the new value.
    * The object is still assigned to the entity-manager, so all
    * changed will directly be written to database
    * 
    * @param level 
    */
    private void setLogLevelForModelInFileTable(TraceLevel level) {
        
        com.elster.nppTraceMonitor.db.File fileModel=null;
        Module moduleModel=null;
        int[] selectedRows;
      
        
        
        selectedRows = moduleTbl.getSelectedRows ();
        if ( selectedRows == null ) {
            JOptionPane.showMessageDialog (this, "Please select a  Module first", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            moduleModel = moduleList.get( moduleTbl.convertRowIndexToModel ( selectedRows[0] ) );
        } catch( Exception e) {
            JOptionPane.showMessageDialog (this, "Model in Table has no Module", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Looop through all selected rows
        selectedRows = fileTbl.getSelectedRows ();
        for(int i : selectedRows )
        {
            // Get FILE entity from table row
            try
            {
                fileModel = fileList.get( fileTbl.convertRowIndexToModel( i ) );
            }
            catch( ArrayIndexOutOfBoundsException e)
            {
                JOptionPane.showMessageDialog (this, "Selected Row has no Model", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if( fileModel == null )
            {
                JOptionPane.showMessageDialog (this, "Selected Row has no Model", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get MODULE object
            Module module;
            String query =
            "SELECT m "
            + "FROM File f JOIN f.moduleCollection m "
            + "WHERE f.fileId = :fileId "
            + "AND m.moduleId = :moduleId";
            try
            {
                module = NbNppTraceMonitorPUEntityManager
                        .createQuery(query, Module.class)
                        .setParameter("fileId", fileModel.getFileId() )
                        .setParameter("moduleId", moduleModel.getModuleId () )
                        .getSingleResult();
            }
            catch( NoResultException e)
            {
                LOGGER.log(Level.SEVERE, "File <--> Module integrity broken");
                return;
            }
            catch( PersistenceException e)
            {
                LOGGER.log( Level.SEVERE, "PersistenceException: {0}", e);
                return;
            }

            // Send fileId and traceLevel to the device
            myConnection.getWriter().setFileTraceLevel( module.getModuleId(), fileModel.getFileIndex (), level );
        
        }
    }
    
    /**
     * The filter dialog uses this setter to apply new
     * criteria to the query
     * @param query 
     */
    private void    setModuleTraceQuery( String query ) {
        try
        {
            // JOptionPane.showMessageDialog(this, "New Query: \n\n" + query, "Query", JOptionPane.INFORMATION_MESSAGE);
            
            // Extend query with 'decodedMessage!=""' (so new traces will not be in this resultSet).
            // With new traces I mean those that arrive after the last refresh interval (1 sec. window )
            // 
            if( !query.contains("WHERE") ) {
                moduleTraceQuery = NbNppTraceMonitorPUEntityManager.createQuery( query + " WHERE m.decodedMessage!=\"\"" );
            }
            else
            {
                moduleTraceQuery = NbNppTraceMonitorPUEntityManager.createQuery( query + " AND m.decodedMessage!=\"\"" );
            }
            
//            moduleTraceQuery = NbNppTraceMonitorPUEntityManager.createQuery (query);
//            JOptionPane.showMessageDialog(this, "Adding "+ moduleTraceQuery.getResultList ().size ()+" results for query:\n\n" +query, "Filter Query", JOptionPane.INFORMATION_MESSAGE);
                
            // delete & refill the table to filter
            synchronized( LOCK )
            {
                moduleTraceList.clear();
                moduleTraceList.addAll( moduleTraceQuery.getResultList () );
            }
            
            // Remember the query so the refreshTraceTable() function can use this as well
            moduleTraceQueryString = query;
            
            // Autoscroll to last column
            traceTbl.scrollRectToVisible(traceTbl.getCellRect(traceTbl.getRowCount()-1, traceTbl.getColumnCount(), true));
        }
        catch( IllegalArgumentException e)
        {
            JOptionPane.showMessageDialog(this, "SQL-Query \n'"+query+"'\ncannot be parsed", "IllegalArgumentException", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    public void     setModuleTraceFilters( Vector<Object[]> filters )  {
        String query = "Select m from ModuleTrace m WHERE ";
        moduleTraceFilters = filters;
        
        if( filters != null )
        {
            // filter has four elements
            // filter[0] = table column
            // filter[1] = operator (=,<, >,..)
            // filter[2] = value
            // filter[3] = junction (AND.OR)
            for( Object[] filter : filters )
            {
                try
                {
                    query+= "m."+filter[0] + " " +filter[1]+ " " +filter[2] + " " +filter[3] + " ";
                } 
                catch(IndexOutOfBoundsException e)
                {
                    break;
                }
            }
        }
        else
        {
            query = "Select m from ModuleTrace m";
        }
        
        setModuleTraceQuery (query);
    }
    
    public Vector<Object[]>   getModuleTraceFilters( ) {
        return moduleTraceFilters;
    }
    
    private void    printAllTables() {
        System.out.println("Printing table: 'MODULE'");
        List<Module> modules = NbNppTraceMonitorPUEntityManager.createNamedQuery("Module.findAll", Module.class).getResultList();
        for (Module m : modules) {
            System.out.println(m.getModuleId() + " " + m.getName() );
            for( com.elster.nppTraceMonitor.db.File f : m.getFileCollection () )
            {
                System.out.println( "\tFiles: " + f.getFileId() );
            }
        }

        System.out.println("Printing table: 'FILE'");
        List<com.elster.nppTraceMonitor.db.File> files = NbNppTraceMonitorPUEntityManager.createNamedQuery("File.findAll", com.elster.nppTraceMonitor.db.File.class).getResultList();
        for (com.elster.nppTraceMonitor.db.File f : files) {
            System.out.println("FileID: " + f.getFileId() + " FileIndex: " + f.getFileIndex() + " FileName: " + f.getFileName() + " LogLevel: " + f.getLogLevel() );
            for(Module m : f.getModuleCollection ())
            {
                System.out.println("\tModules:" + m.getModuleId ());
            }
        }

        System.out.println("Printing table: 'TRACE'");
        List<Trace> traces = NbNppTraceMonitorPUEntityManager.createNamedQuery("Trace.findAll", Trace.class).getResultList();
        for (Trace t : traces) {
            System.out.println("TraceID: " + t.getTraceId() + " TraceIndex: " + t.getTraceIndex() + " FileID: " + t.getFileId().getFileId() + " Line: " + t.getLine());
        }

//        System.out.println("Printing table: 'MODULE_TRACE'");
//        List<ModuleTrace> moduleTraces = NbNppTraceMonitorPUEntityManager.createNamedQuery("ModuleTrace.findAll", ModuleTrace.class).getResultList();
//        for (ModuleTrace t : moduleTraces) {
//            System.out.println("ModuleID: " + t.getModuleId().getModuleId() + " TraceID: " + t.getTraceId().getTraceId() + " Timestamp: " + t.getTimestamp() + " DecodedMsg: " + t.getDecodedMessage() + " LogLevel: " + t.getLogLevel());
//        }
    }

    private void    openFileInEditor(String fileName, int line) {
        
        if( myEditor.equalsIgnoreCase ( "" ) ) {
            JOptionPane.showMessageDialog (this, "Editor not set!", "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
       
        
        try {
            if( myEditor.endsWith("notepad++.exe") ) {
                new ProcessBuilder(myEditor, "-nosession", "-n"+line, fileName).start();
            }
            else if ( myEditor.endsWith ("codeblocks.exe")) {
                new ProcessBuilder(myEditor, "--file[:"+line+"]=", fileName).start();
            }
            else {
                new ProcessBuilder(myEditor, fileName);
            }
            
        }
        catch(IOException e) {
            LOGGER.log(Level.SEVERE, "IOException while opening Editor: {0}", e);
        }
        
    }
    
    // Slots 
    //
    @Override
    public void     connectionClosedSlot() {
        // Change Icon to 'connect'
        connectBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/elster/nppTraceMonitor/resources/plug_con.png")));
        connectBtn.setName("connect");
        
        // Deactivate 'Pause' and 'Play' button
        pauseBtn.setEnabled(false);
        playBtn.setEnabled(false);
        
        // Stop the refreshTimer
        refreshTimer.stop();
        
        myConnection.removeListener(this);  
    }

    @Override
    public void     unkonwnHostSlot() {
        JOptionPane.showMessageDialog(this, "Unknown Host");
    }

    @Override
    public void     connectionRefusedSlot() {
        JOptionPane.showMessageDialog(this, "Connection Refused. Server may not be running.");
    }

    
    /**
     * Signal that device is still reachable. 
     * Resets the watchdog timer on client side
     */
    @Override
    public void     idleSlot() {
        LOGGER.log(Level.FINER, "Idle received!");
//        watchdogTimer.restart (); //reset watchdog
    }
    /**
     * Synchronizes this computer with the device. The timestamp array contains
     * one of two timestamp formats. Usually the device sends this data right at
     * the beginning of the connection. The timestamp contains the UTC Date
     * format as well as the ticks (clock cycles) since device startup
     *
     * @param timestamp
     */
    @Override
    public void     timestampSlot(final byte[] timestamp) {
        int         timeZoneOffset;
        int         dstOffset;
        int         daylightSavingActive;
        
        if( timestamp == null)
        {
            LOGGER.log( Level.INFO, "Timestamp is null");
            return;
        }
        ByteBuffer  timestampBuffer = ByteBuffer.allocate(timestamp.length + 2); // +2 for padding bytes, to fill up 48-bit timestamp to match 8 bytes (long)
        
        // Adjust Byte Order
        timestampBuffer.order( TMP.BYTE_ORDER ); 
        // A B | C D E F   -->  B A | F E D C
        // where AB is the upper timestamp and CDEF is the lower timestamp
        

        
        
        // Check timestamp length
        if (timestamp.length < TMP.TIMESYNC1_LEN) {
            LOGGER.log(Level.WARNING, "Timestamp length does not fit");
            return;
        }
        else
        {
            LOGGER.log( Level.FINER, "Timestamp received");
        }


        // Extract ticks since system start
        timestampBuffer.clear();
        timestampBuffer.put(Arrays.copyOfRange(timestamp, 2, 6));   // lower timestamp (32-bit)
        timestampBuffer.put(Arrays.copyOfRange(timestamp, 0, 2));   // upper timestamp (16-bit)
        timestampBuffer.put( new byte[] { 0x00, 0x00 } );           // padding
        ticks = timestampBuffer.getLong(0);

        
        
        // Extract UTC
        timestampBuffer.clear();
        timestampBuffer.put(Arrays.copyOfRange(timestamp, 6, 10));
        utc = timestampBuffer.getInt(0); // getInt() takes 4 bytes

        
        // Print date for debugging
        Date d = new Date( utc * 1000 ); // time_t in seconds, but java.utils.date in mili-seconds
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );
        df.setTimeZone( TimeZone.getTimeZone ("GMT+0:00")); // London
        // JOptionPane.showMessageDialog (this, "Connected. Time on device: " + df.format(d), "Connected Box", JOptionPane.INFORMATION_MESSAGE);

        if (timestamp.length == TMP.TIMESYNC2_LEN) {
            // Extract time zone offset
            timestampBuffer.clear();
            timestampBuffer.put(timestamp, 11, 13);
            timeZoneOffset = timestampBuffer.getShort(0);

            // Extract dst offset 
            timestampBuffer.clear();
            timestampBuffer.put(timestamp, 13, 15);
            dstOffset = (timestampBuffer.getShort(0) & TMP.TIME_DST_BITMASK) >> TMP.TIME_DST_SHIFT;

            // Extract daylight saving active flag 
            daylightSavingActive = timestampBuffer.getShort(0) & TMP.TIME_DAYLIGHT_BITMASK;

            // Add offsets to utc
            utc = utc + timeZoneOffset + dstOffset;
        }

    }

    /**
     * The difference between the ticks in the trace message and the ticks from
     * the last timesync divided by the constant TICKS_PER_SECOND is the passed
     * time in seconds. This value is added to the utc variable as an offset.
     * 
     * Also java.util.Date uses miliseconds for utc instead of seconds (time_t)
     *
     * @param ticks
     * @return ticks in miliseconds
     */
    private long    getTimestamp(long pTicks) {
        // return Calendar.getInstance ().getTime ().getTime ();
        return (utc*1000 + (( pTicks - ticks) / TICKS_PER_MILISECOND ) );

    }

    /**
     * Saves and incoming trace to database. It extracts the timestamp, ids and
     * the argument list from the byte array and assigns these values to the
     * proper entity class objects
     *
     * @param trace
     */
    @Override
    public void     traceSlot(final byte[] trace) {
        long        ticksSinceTimeSync;
        int         uniqueIds;
        int         moduleId;
        int         fileIndex;
        int         traceIndex;
        int         traceLevel;
        int         argTypes = 0;
        ByteBuffer  traceBuffer;
        ByteBuffer  argListBuffer = null;
        Module      matchedModule = null;   // result of sql query
        Trace       matchedTrace  = null;   // result of sql query
        
        
        // Check if trace is complete
        if (trace == null || trace.length < TMP.TRACE_MESSAGE_MIN_LENGTH) 
        {
            LOGGER.log(Level.INFO, "Trace is too short");
            return;
        } 
        else 
        {
            LOGGER.log(Level.FINER, "Trace received");
        }
        

        // Allocate memory for buffer
        traceBuffer     = ByteBuffer.allocate( TMP.TRACE_MESSAGE_MIN_LENGTH );
        argListBuffer   = ByteBuffer.allocate( trace.length - TMP.TRACE_MESSAGE_MIN_LENGTH );

        
        
        // Adjust Byte Order
        traceBuffer.order( TMP.BYTE_ORDER ); // Adjust Byte Order
        
        
        
        // Extract timestamp
        traceBuffer.put(Arrays.copyOfRange(trace, 2, 6));
        traceBuffer.put(Arrays.copyOfRange(trace, 0, 2));
        ticksSinceTimeSync = traceBuffer.getLong(0);


        
        // Extract ids
                          traceBuffer.clear();
        uniqueIds       = traceBuffer.put( Arrays.copyOfRange( trace, 6, 10) ).getInt(0);
        moduleId        = (uniqueIds & TMP.MODULE_ID_BITMASK)   >> TMP.MODULE_ID_SHIFT;
        fileIndex       = (uniqueIds & TMP.FILE_INDEX_BITMASK)  >> TMP.FILE_INDEX_SHIFT;
        traceIndex      = (uniqueIds & TMP.TRACE_INDEX_BITMASK) >> TMP.TRACE_INDEX_SHIFT;
        traceLevel      = (uniqueIds & TMP.TRACE_LEVEL_BITMASK) >> TMP.TRACE_LEVEL_SHIFT;
        
        
        
        // If trace contains (optional) arguments, then read the types and their values
        if (trace.length > TMP.TRACE_MESSAGE_MIN_LENGTH) {
                          traceBuffer.clear();
            argTypes    = traceBuffer.put( trace, 10, 4).getInt(0);
            argListBuffer.put(trace, 14, trace.length - (TMP.TRACE_MESSAGE_MIN_LENGTH+4));
        }

        

        // Get MODULE from Database
        try
        {
            matchedModule = NbNppTraceMonitorPUEntityManager.createNamedQuery("Module.findByModuleId", Module.class).setParameter("moduleId", moduleId).getSingleResult();
        }
        catch( NoResultException e) {
            LOGGER.log(Level.INFO, "No Module with this moduleId in database");
            bufferTraceData (moduleId, fileIndex, traceIndex, traceLevel, ticksSinceTimeSync, argTypes, argListBuffer.array ());
            return;
        }

        

        // Get TRACE from Database
        String q =
                "SELECT t "
                + "FROM Module m JOIN m.fileCollection f JOIN f.traceCollection t "
                + "WHERE m.moduleId = :moduleId AND "
                + "f.fileIndex = :fileIndex AND "
                + "t.traceIndex = :traceIndex";

        try {
            matchedTrace = (Trace) NbNppTraceMonitorPUEntityManager.createQuery(q).setParameter("traceIndex", traceIndex).setParameter("moduleId", moduleId).setParameter("fileIndex", fileIndex).getSingleResult();
        } catch (NoResultException e) {
            LOGGER.log(Level.INFO, "Trace Message not found for this unique id\nWriting into buffer table");
            bufferTraceData (moduleId, fileIndex, traceIndex, traceLevel, ticksSinceTimeSync, argTypes, argListBuffer.array() );
            return;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "SQL Query is wrong and cannot be compiled: {0}", e);
            return;
        } catch (DatabaseException e) {
            LOGGER.log(Level.SEVERE, "SQL Query is wrong and cannot be compiled: {0}", e);
            return;
        }


        try
        {
            // Create new MODULE_TRACE entry in database
            NbNppTraceMonitorPUEntityManager.getTransaction().begin();
                ModuleTrace newModuleTrace = new ModuleTrace();
                newModuleTrace.setModuleId( matchedModule );
                newModuleTrace.setTraceId( matchedTrace );
                newModuleTrace.setTimestamp( new Date( getTimestamp( ticksSinceTimeSync )) );
                newModuleTrace.setArgumentTypes( argTypes );
                newModuleTrace.setArgumentList( argListBuffer.array() );
                newModuleTrace.setLogLevel(traceLevel);

                // Bidirectional transition of MODULE and TRACE
                matchedModule.getModuleTraceCollection().add(newModuleTrace);
                matchedTrace.getModuleTraceCollection().add(newModuleTrace);

                NbNppTraceMonitorPUEntityManager.persist( newModuleTrace );
            NbNppTraceMonitorPUEntityManager.getTransaction().commit(); // rollback on error
            
            LOGGER.log(Level.FINER, "Trace saved");
            
        }
        catch( RollbackException e )
        {
            LOGGER.log( Level.SEVERE, "COMMIT for this trace entry FAILED");
        }
        catch( Exception e)
        {
            LOGGER.log( Level.SEVERE, "java.sql.SQLDataException: Die Zeichenfolgendarstellung eines datetime-Wertes liegt außerhalb des Bereichs.");
        }
        
    }

    /**
     * Buffers an incoming trace message even if the module is not present.
     * Later if the TraceMonitor got the module list and parsed the trace files
     * it can decode the message.
     * 
     * @param moduleId
     * @param fileIndex
     * @param traceIndex
     * @param traceLevel
     * @param ticksSinceTimeSync
     * @param argTypes
     * @param argListBuffer 
     */
    private void    bufferTraceData( int moduleId, int fileIndex, int traceIndex, 
                                     int traceLevel, long ticksSinceTimeSync, int argTypes, byte[] argList)  {
        
            ModuleTraceBuffer newModuleTraceBuffer = new ModuleTraceBuffer();
            try 
            {
                // Write this trace into table 'MODULE_TRACE_BUFFER' until this module is loaded
                NbNppTraceMonitorPUEntityManager.getTransaction ().begin();
                    newModuleTraceBuffer.setModuleId ( moduleId );
                    newModuleTraceBuffer.setFileIndex ( fileIndex );
                    newModuleTraceBuffer.setTraceIndex (traceIndex );
                    newModuleTraceBuffer.setTicksSinceTimeSync ( BigInteger.valueOf (ticksSinceTimeSync )) ;
                    newModuleTraceBuffer.setArgumentTypes (argTypes);
                    newModuleTraceBuffer.setArgumentList ( argList );
                    newModuleTraceBuffer.setLogLevel (traceLevel);
                    NbNppTraceMonitorPUEntityManager.persist( newModuleTraceBuffer );
                NbNppTraceMonitorPUEntityManager.getTransaction ().commit ();
            } 
            catch(RollbackException ex) 
            {
                LOGGER.log( Level.SEVERE, "Trace lost because of RollbackException:  "
                        + "ModuleTraceBufferId is mapped to a primary key column in the database. Updates are not allowed.");
            }
            LOGGER.log(Level.FINER, "Buffered Trace Message");
    }
    /**
     * Reads traces from the buffer table which belong to a certain module.
     * It then adds them to the to the actual MODULE_TRACE table
     * @param module 
     */
    private void    saveTraceData ( Module module ) {
            String                  query;
            List<ModuleTraceBuffer> resultList;
            ModuleTrace             newModuleTrace = null;
            Trace                   matchedTrace = null;
            
            // Look if traces have been buffered for this module
            query = "SELECT m "+
                    "FROM ModuleTraceBuffer m WHERE m.moduleId = :moduleId";
            resultList = NbNppTraceMonitorPUEntityManager.createQuery (query, ModuleTraceBuffer.class).setParameter("moduleId", module.getModuleId() ).getResultList ();
            
            for( ModuleTraceBuffer trace : resultList )
            {
                
                
                // Get TRACE from Database
                String q =
                        "SELECT t "
                        + "FROM Module m JOIN m.fileCollection f JOIN f.traceCollection t "
                        + "WHERE m.moduleId = :moduleId AND "
                        + "f.fileIndex = :fileIndex AND "
                        + "t.traceIndex = :traceIndex";

                try {
                    matchedTrace = (Trace) NbNppTraceMonitorPUEntityManager.createQuery(q).setParameter("traceIndex", trace.getTraceIndex ()).setParameter("moduleId", module.getModuleId ()).setParameter("fileIndex", trace.getFileIndex ()).getSingleResult();
                } catch (NoResultException e) {
                    LOGGER.log(Level.FINE, "No buffered traces for this module");
                    continue;
                }
                
                
                try
                {
                    // Create new MODULE_TRACE entry in database
                    NbNppTraceMonitorPUEntityManager.getTransaction().begin();
                        newModuleTrace = new ModuleTrace( );
                        newModuleTrace.setModuleId ( module );
                        newModuleTrace.setTraceId (matchedTrace);
                        newModuleTrace.setTimestamp ( new Date( getTimestamp ( trace.getTicksSinceTimeSync ().longValue() ) ));
                        newModuleTrace.setLogLevel ( trace.getLogLevel () );
                        newModuleTrace.setArgumentTypes ( trace.getArgumentTypes () );
                        newModuleTrace.setArgumentList ( trace.getArgumentList () );

                        // Bidirectional transition of MODULE and TRACE
                        module.getModuleTraceCollection().add(newModuleTrace);
                        matchedTrace.getModuleTraceCollection().add(newModuleTrace);
                        NbNppTraceMonitorPUEntityManager.persist( newModuleTrace );
                        
                        // Delete trace from buffer table
                        NbNppTraceMonitorPUEntityManager.detach ( trace );
                    NbNppTraceMonitorPUEntityManager.getTransaction().commit(); // rollback on error


                    decodeTraceMessage( newModuleTrace );

                    // Update gui by adding to observable list
                    synchronized( LOCK )
                    {
                        moduleTraceList.add(newModuleTrace);
                    }

                }
                catch( RollbackException e )
                {
                    // column in db too short or primary key already updated
                    LOGGER.log( Level.SEVERE, "Commit for this trace entry failed");
                }
                catch( Exception e )
                {
                    LOGGER.log( Level.SEVERE, "java.sql.SQLDataException: Die Zeichenfolgendarstellung eines datetime-Wertes liegt außerhalb des Bereichs.");
                }
                
            }
        
            LOGGER.log(Level.INFO, "Retrieved trace from buffer");
        
    }
    
    /**
     * Inserts the parameters in the format string. 
     * The result is a readable trace message.
     *
     * @param primaryKey
     */
    private void    decodeTraceMessage ( ModuleTrace pModuleTrace ) {
        ByteBuffer      argListBuffer = null;
        Vector<Object>  argList = new Vector<Object>(); // separated
        int             argTypes;
        int[]           argTypesList = new int[10];
        int             numTypes;
        Trace           trace;
        int             informationLoss;
       
        
        LOGGER.log( Level.FINER, "Decoding Trace Message");
        
        // Get the TRACE object to get the formatString
        try
        {
            trace = entityMgr.createNamedQuery("Trace.findByTraceId", Trace.class).setParameter("traceId", pModuleTrace.getTraceId().getTraceId() ).getSingleResult();
        }
        catch( NoResultException e)
        {
            LOGGER.log(Level.SEVERE, "No trace object for this primaryKey");
            return;
        }

        
        // Store argument types in an array
        argTypes         = pModuleTrace.getArgumentTypes();
        informationLoss  = (argTypes & TMP.ARGS_LOST_SHIFT)         >> TMP.ARGS_LOST_SHIFT;
        argTypesList[0]  = (argTypes & TMP.ARGS_FIRST_BITMASK)      >> TMP.ARGS_FIRST_SHIFT;
        argTypesList[1]  = (argTypes & TMP.ARGS_SECOND_BITMASK)     >> TMP.ARGS_SECOND_SHIFT;
        argTypesList[2]  = (argTypes & TMP.ARGS_THIRD_BITMASK)      >> TMP.ARGS_THIRD_SHIFT;
        argTypesList[3]  = (argTypes & TMP.ARGS_FOURTH_BITMASK)     >> TMP.ARGS_FOURTH_SHIFT;
        argTypesList[4]  = (argTypes & TMP.ARGS_FIFTH_BITMASK)      >> TMP.ARGS_FIFTH_SHIFT;
        argTypesList[5]  = (argTypes & TMP.ARGS_SIXTH_BITMASK)      >> TMP.ARGS_SIXTH_SHIFT;
        argTypesList[6]  = (argTypes & TMP.ARGS_SEVENTH_BITMASK)    >> TMP.ARGS_SEVENTH_SHIFT;
        argTypesList[7]  = (argTypes & TMP.ARGS_EIGHTH_BITMASK)     >> TMP.ARGS_EIGHTH_SHIFT;
        argTypesList[8]  = (argTypes & TMP.ARGS_NINTH_BITMASK)      >> TMP.ARGS_NINTH_SHIFT;
        argTypesList[9]  = (argTypes & TMP.ARGS_TENTH_BITMASK)      >> TMP.ARGS_TENTH_SHIFT;
        
        
        
        // Get number of arguments by testing if type is 0
        numTypes=0;
        while( argTypesList[numTypes] != 0 )
        {
            numTypes++;
        }
        
        
        
        // Cast BLOB to list of java datatypes
        if( pModuleTrace.getArgumentList() instanceof byte[] )
        {
            argListBuffer = ByteBuffer.wrap( (byte[])pModuleTrace.getArgumentList() );
            argListBuffer.order(TMP.BYTE_ORDER);
        } else {
            LOGGER.log(Level.WARNING, "ArgumentList ist not of type: byte[]");
            return;
        }
        
        
        
        
        // Check if decoding is necessary
        if( numTypes == 0 || (argListBuffer == null) )
        {
            LOGGER.log( Level.FINER, "Format String is plain text");
            pModuleTrace.setDecodedMessage( trace.getFormatString() );
            
            return;
        }
        
        
        
        ArgType currentType=null;
        try
        {
            for(int i = 0; i < numTypes; i++)
            {
                // Get enum value that represents a datatype
                currentType = ArgType.getArgType( argTypesList[i] );
                if( currentType != null )
                {
                    if ( currentType == ArgType.STRING ) {
                        
                       
                        
                        // Search position of '\0' character
                        int posStart = argListBuffer.position();
                        int posEnd   = -1;
                        argListBuffer.mark ();
                        for( int j = posStart; j < argListBuffer.capacity(); j++ )
                        {
                            if( (byte)'\0' == argListBuffer.get() )
                            {
                                posEnd = argListBuffer.position()-1;
                                break;
                            }
                        }
                        
                        // Get string from byte array
                        if( posEnd != -1 )
                        {
                            argListBuffer.reset ();
                            byte[] theString = new byte[posEnd-posStart];
                            argListBuffer.get(theString, 0, posEnd-posStart);
                            argListBuffer.get(); // step over null-character
                            argList.add( new String(theString) );
                        }
                        

                    }
                    else if ( currentType == ArgType.DOUBLE ) {
                        argList.add( argListBuffer.getDouble() );
                    }
                    else if ( currentType == ArgType.LONG_INT ) {
                        argList.add( argListBuffer.getLong () );
                    }
                    else if ( currentType == ArgType.INT ) {
                        argList.add( argListBuffer.getInt() );
                    }
                    else if ( currentType == ArgType.SHORT_INT )
                    {
                        argList.add( argListBuffer.getShort() );
                    }
                    else if ( currentType == ArgType.CHAR )
                    {
                        argList.add( argListBuffer.get() );
                    }
                    else {
                        LOGGER.log( Level.WARNING, "Argument Type was not recognized!");
                        return;
                    }
                }
            }
            
        }
        catch( IndexOutOfBoundsException e)
        {
            LOGGER.log( Level.SEVERE, "Elements in buffer not sufficient to parse current datatype: {0}", e);
        }
        catch( BufferUnderflowException e)
        {
            LOGGER.log (Level.SEVERE, "ArgumentList has not enough elements for this datatype");
        }
        
        
        try 
        {
//            entityMgr.getTransaction ().begin ();
            // replace '%llf', which is not supported by String.format
            pModuleTrace.setDecodedMessage( String.format( trace.getFormatString ().replace("%lld", "%d").replace("%ld", "%d"), argList.toArray () ) );
            
            // Show '{..}' at the end of a trace message if data was lost (parameters exeeded 128 Bytes)
            if(informationLoss > 0) {
                pModuleTrace.setDecodedMessage (pModuleTrace.getDecodedMessage () + "{..}");
            }
//            entityMgr.getTransaction ().commit ();
        } 
        catch (IllegalFormatException e) {
            LOGGER.log(Level.SEVERE, "Cannot decode trace message with this parameters");
        }
//        catch( RollbackException e) {
//            LOGGER.log(Level.SEVERE, "Cannot decode trace message. It is possible, that the trace is too long");
//        }
    }

    /**
     * Uses the module name to open and parse the trace files. It creates a new
     * entry in the MODULE table to store the id and name. For every line in the
     * trace file (.tfi) a new entry in the FILE table is made. For every line
     * in the trace data file (.td) a new entry in the TRACE table is made
     *
     * @param module
     */
    @Override
    public void     moduleSlot (final byte[] module) {
        Module      newModule;
        int         moduleId;
        String      moduleName;
        ByteBuffer  moduleNameBuffer;
        

        if(module==null)
        {
            LOGGER.log(Level.SEVERE, "module is null");
            return;
        }
        
        // Check if module message is complete
        if( module.length < TMP.MODULE_MESSAGE_MIN_LENGTH )
        {
            LOGGER.log( Level.WARNING, "Module too short");
            return;
        }
        else
        {
            LOGGER.log( Level.FINER, "Module received");
        }
        
        
        // Allocate memory for buffer
        moduleNameBuffer = ByteBuffer.allocate ( module.length -1 ); // - moduleId
        
        
        
        // Adjust Byte Order
        moduleNameBuffer.order ( TMP.BYTE_ORDER );
        
        
        
        // Extract moduleId
        moduleId = (module[0] & TMP.MODULE_ID_SHORT_BITMASK);
        
        
        // Extract moduleName
        try 
        {
            moduleNameBuffer.put(module, 1, module.length -1);
            moduleName = new String(moduleNameBuffer.array(), 0, moduleNameBuffer.capacity()-1, "US-ASCII");
        } 
        catch (UnsupportedEncodingException e) 
        {
            LOGGER.log(Level.SEVERE, "Encoding Error in module name");
            return;
        }



        // Check if moduleId already exists in database
        Module moduleIdMatch = null;
        try {
            moduleIdMatch = NbNppTraceMonitorPUEntityManager.createNamedQuery("Module.findByModuleId", Module.class).setParameter("moduleId", moduleId).getSingleResult();
        } catch (NoResultException e) {
            LOGGER.log(Level.FINER, "New ModuleID"); // normal case
        }

        if (moduleIdMatch != null) {
            LOGGER.log(Level.INFO, "ModuleID already exists in database");
            return;
        }



        // Add new module to database
        newModule = new Module();
        newModule.setModuleId(moduleId);
        newModule.setName(moduleName);
        NbNppTraceMonitorPUEntityManager.persist(newModule);
        LOGGER.log(Level.FINER, "Persisting {0}", newModule.getName());
        // Add new module to observable list
        moduleList.add(newModule);

        // Check if moduleName already exists in database
        Module moduleNameMatch = null;
        try 
        {
            moduleNameMatch = NbNppTraceMonitorPUEntityManager.createNamedQuery("Module.findByName", Module.class).setParameter("name", moduleName).getSingleResult();
        } 
        catch (NoResultException e) 
        {
            LOGGER.log(Level.FINER, "New ModuleName"); // normal case
        }

        if (moduleNameMatch != null) 
        {
            LOGGER.log(Level.INFO, "Module Name already exists in database");
        
            // Assign the same files to the new module
            newModule.setFileCollection(moduleNameMatch.getFileCollection());
            for( com.elster.nppTraceMonitor.db.File f : moduleNameMatch.getFileCollection () ) {
                f.getModuleCollection ().add ( newModule );
            }

        } 
        else 
        {
            // TFI was not parsed so far, search TFI
            File traceFile = new File(mySdCardTopDir, newModule.getName() + ".trace");
            if (! traceFile.exists()) {
                LOGGER.log(Level.WARNING, "Module has no Trace-File");

//                // Select new SDCard topdir
//                JFileChooser chooser = new JFileChooser( new File( mySdCardTopDir, "Traces") );
//                chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
//
//                    @Override
//                    public boolean accept(File f) {
//                        return f.getName().toLowerCase().endsWith(".trace");
//                    }
//
//                    @Override
//                    public String getDescription() {
//                        return "Trace-File(*.trace)";
//                    }
//                });
//                chooser.setSelectedFile( new File( newModule.getName() + ".trace" ) );
//                int returnVal = chooser.showOpenDialog(this);
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    // Update searchpath and set trace file to selected file
//                    mySdCardTopDir = chooser.getSelectedFile().getPath().toString();
//                    traceFile = chooser.getSelectedFile();
//
//                } else {
                    return;
//                }
            }


            // Parse trace file and create new FILE entries in database
            parseTraceFile(newModule, traceFile);

            
            // Check trace buffer for messages belonging to this module
            saveTraceData (newModule);
            
            // Get File Levels from the device to update the column
//            myConnection.getWriter ().requestModuleTraceLevelTable ( (byte)newModule.getModuleId ().byteValue () );
        }
    }

    /**
     * Reads every line in the trace file and saves new FILE and TRACE records.
     * Checks every line if it contains trace-file-information or trace-data-information
     * and extracts the fields for the new FILE/TRACE entity
     *
     * @param module
     */
    private void    parseTraceFile (Module module, File traceFile) {
        final String    groupDelimiter = ": ";
        final String    idsDelimiter = "\\.";
        BufferedReader  br;
        String          line;
        String          fileName;
        int             fileIndex;
        int             traceIndex;
        int             lineNumber;       
        int             traceLevel; // not used
        String          formatString;
        String[]        idsAndData;
        String[]        ids;
        Trace           newTrace;
        com.elster.nppTraceMonitor.db.File newFileEntry;


        // Open trace file
        try 
        {
            br = new BufferedReader(new FileReader( traceFile ));
        } 
        catch (FileNotFoundException e) 
        {
            // File existence was already checked, so this shouldn't happen
            LOGGER.log(Level.SEVERE, "Cannot find trace file: {0}", traceFile);
            return;
        }
        LOGGER.log(Level.INFO, "Parsing {0}", traceFile);

        
        // Clear fileList from other module files
        fileList.clear();
        
        
        
        // Parse every line in file
        try {
            while ((line = br.readLine()) != null) {
                
                try {
//                    LOGGER.log(Level.FINEST, "Read line: {0}", line);

                    // Tokenize the string
                    idsAndData = line.split(groupDelimiter, 2);
                    ids = idsAndData[0].split(idsDelimiter, 4);


                    // Check if line contains TraceFileInfo or TraceDataInfo
                    if( ids.length == 4 && idsAndData.length == 2 ) {
                        // Trace Data Information

                        try {
                            fileIndex   = Integer.parseInt ( ids[0].trim() );
                            traceIndex  = Integer.parseInt ( ids[1].trim() );
                            lineNumber  = Integer.parseInt ( ids[2].trim() );
                            traceLevel  = Integer.parseInt ( ids[3].trim() );
                            formatString= idsAndData[1];
                        } catch (NumberFormatException e) {
                            LOGGER.log(Level.WARNING, "Cannot parse line \"{0}\" in trace file {1}", new Object[]{line, traceFile.getName ()});
                            continue;
                        }


                        // Get FILE for new TRACE entry
                        com.elster.nppTraceMonitor.db.File matchedFile = null;
                        String query =
                                "SELECT f "
                                + "FROM Module m JOIN m.fileCollection f "
                                + "WHERE f.fileIndex = :fileIndex AND "
                                + "m.moduleId = :moduleId";

                        try 
                        {
//                            startTime = System.nanoTime ();
                            matchedFile = (com.elster.nppTraceMonitor.db.File) NbNppTraceMonitorPUEntityManager.createQuery(query).setParameter("fileIndex", fileIndex).setParameter("moduleId", module.getModuleId()).getSingleResult();
//                            LOGGER.log(Level.FINEST, "Duration of matchingFile query: {0}", System.nanoTime()-startTime );
                        } 
                        catch (NoResultException e) 
                        {
                            LOGGER.log(Level.SEVERE, "Cannot find FILE for FileIndex '{0}' in '{1}", new Object[]{fileIndex, traceFile});
                            continue;
                        } 
                        catch (IllegalArgumentException e) 
                        {
                            LOGGER.log(Level.SEVERE, "SQL Query cannot be compiled"); 
                            continue;
                        }


                        // Create new TRACE entry
//                        startTime = System.nanoTime ();
                        newTrace = new Trace();
//                        NbNppTraceMonitorPUEntityManager.getTransaction().begin();
                            newTrace.setFileId(matchedFile);
                            newTrace.setTraceIndex(traceIndex);
                            newTrace.setLine(lineNumber);
                            newTrace.setFormatString(formatString);
                            NbNppTraceMonitorPUEntityManager.persist(newTrace);
//                        NbNppTraceMonitorPUEntityManager.getTransaction().commit();
//                        LOGGER.log(Level.FINEST, "Duration of Trace Insert query: {0}", System.nanoTime() - startTime );

                    }
                    else {
                        // Trace File Information

                        if (idsAndData.length != 2 || ids.length != 1 ) {
                            LOGGER.log(Level.WARNING, "Line \"{0}\" in {1} has not tfi format", new Object[]{line, traceFile.getName ()});
                            continue;
                        }


                        try {
                            fileIndex = Integer.parseInt(idsAndData[0].trim());
                            fileName = idsAndData[1];
                        } catch (NumberFormatException e) {
                            LOGGER.log(Level.WARNING, "Cannot parse line \"{0}\" in trace file {1}", new Object[]{line, traceFile.getName ()});
                            continue;
                        }

                        // Create new FILE entry in database
//                        startTime = System.nanoTime ();
                        newFileEntry = new com.elster.nppTraceMonitor.db.File();
                        newFileEntry.setFileIndex(fileIndex);
                        newFileEntry.setFileName(fileName);
                        newFileEntry.setLogLevel(TraceLevel.WARN.getValue());
                        Collection<Module> linkedModules = new ArrayList<Module>();
                        linkedModules.add(module);
                        newFileEntry.setModuleCollection ( linkedModules );
                        if ( module.getFileCollection () == null ) {
                            Collection<com.elster.nppTraceMonitor.db.File> linkedFiles = new ArrayList<com.elster.nppTraceMonitor.db.File>();
                            linkedFiles.add (newFileEntry);
                            module.setFileCollection (linkedFiles);
                        }
                        module.getFileCollection ().add(newFileEntry);
                        
                        NbNppTraceMonitorPUEntityManager.getTransaction().begin();
                            NbNppTraceMonitorPUEntityManager.persist(newFileEntry);
                        NbNppTraceMonitorPUEntityManager.getTransaction().commit(); // commit to generate fileId. Else t
                        // Add to fileList which is observed by 'fileTbl'
                        fileList.add(newFileEntry);
//                        LOGGER.log(Level.FINEST, "Duration of File Insert query: {0}", System.nanoTime () - startTime);


                        // remark: I use two transactions, because the first transaction
                        // creates an empty moduleCollection for the new file entry, otherwise
                        // getModuleCollection.add(module) would throw a NullPointerException
//                        startTime=System.nanoTime();
//                        NbNppTraceMonitorPUEntityManager.getTransaction ().begin();
//                            // Link FILE to MODULE
//                            newFileEntry.getModuleCollection().add(module);
//                            // Link MODULE to FILE
//                            module.getFileCollection().add(newFileEntry);
//                        NbNppTraceMonitorPUEntityManager.getTransaction ().commit ();
//                        LOGGER.log(Level.FINEST, "Duration of File Insert query2: {0}", System.nanoTime () - startTime);
                    }

                } 
                catch (RollbackException e) {
                    LOGGER.log(Level.SEVERE, "RollbackException while parsingFile ");
                    continue;
                }
            }
        } 
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IOException in parseTraceFile: {0}", e);
        }
        LOGGER.log(Level.FINEST, "Done parsing {0}", traceFile);
    }

    /**
     * Updates the (log-)level of a single file or all files belonging to a
     * specific module
     *
     * @param fileId
     * @param level
     */
    @Override
    public void     traceLevelSlot (final byte[] pTraceLevelData) {

        if(pTraceLevelData==null) {
            return;
        }
        
        if (pTraceLevelData.length < TMP.TRACE_LEVEL_MIN_LENGTH) 
        {
            LOGGER.log(Level.WARNING, "TraceLevel data too short");
        }
        else if (pTraceLevelData.length == TMP.TRACE_LEVEL_MIN_LENGTH) 
        {
            getFileTraceLevel( pTraceLevelData );
        }
        else 
        {
            getModuleTraceLevelTable( pTraceLevelData );
        }
    }

    private void    getFileTraceLevel (byte[] pTraceLevelData) {
        Integer     data;
        Integer     moduleId;
        Integer     fileIndex;
        Integer     traceLevel;
        ByteBuffer  traceLevelBuffer;
        
        
        if( pTraceLevelData.length < 3 )
        {
            LOGGER.log( Level.SEVERE, "TraceLevelData too short");
            return;
        }
        
        // Allocate memory for buffer
        traceLevelBuffer = ByteBuffer.allocate(2);
        
        // Adjust Byte Order
        traceLevelBuffer.order ( TMP.BYTE_ORDER );
        
        
        // Extract moduleId, fileIndex and traceLevel
        traceLevelBuffer.put( Arrays.copyOfRange(pTraceLevelData, 0, 2) );
        data        = (int)traceLevelBuffer.getShort (0) << 16;
        moduleId    = (data & TMP.MODULE_ID_BITMASK)    >> TMP.MODULE_ID_SHIFT;
        fileIndex   = (data & TMP.FILE_INDEX_BITMASK)   >> TMP.FILE_INDEX_SHIFT;
        traceLevel  = pTraceLevelData[2] & TMP.TRACE_LEVEL_BITMASK;

        updateTraceLevel( moduleId, fileIndex, traceLevel);
    }
    
    private void    getModuleTraceLevelTable(byte[] pTraceLevelData) {
        int         moduleId;
        int         fileIndex;
        int         traceLevel;
        
        // Extract moduleId
        moduleId = pTraceLevelData[0] & TMP.MODULE_ID_SHORT_BITMASK;
        
        
        // Set new trace level
        for(int i=1; i < pTraceLevelData.length; i++)
        {
            fileIndex   = (pTraceLevelData[i] & 0xF0) >> 4;
            traceLevel  = (pTraceLevelData[i] & 0x0F);

            updateTraceLevel(moduleId, fileIndex, traceLevel);
        }
        
    }
    
    private void    updateTraceLevel( int moduleId, int fileIndex, int traceLevel ) {
        // FIXME: moduleId has wrong value
        
        // Get the fileID from database
        String query =
                "SELECT f "
                + "FROM Module m JOIN m.fileCollection f "
                + "WHERE f.fileIndex = :fileIndex AND "
                + "m.moduleId = :moduleId";

        com.elster.nppTraceMonitor.db.File matchedFile;
        try 
        {
            matchedFile = (com.elster.nppTraceMonitor.db.File) NbNppTraceMonitorPUEntityManager.createQuery(query).setParameter("fileIndex", fileIndex).setParameter("moduleId", moduleId).getSingleResult();
        } 
        catch (NoResultException e) 
        {
            LOGGER.log(Level.SEVERE, "Updating trace level failed: Cannot find module with this fileId in database");
            return;
        } 
        catch (IllegalArgumentException e) 
        {
            LOGGER.log(Level.SEVERE, "SQL Query Syntax Error: {0}", e);
            return;
        }

        // Update file entry
        matchedFile.setLogLevel( traceLevel );  
        

        // Update cell to show new trace level
        if (fileList.contains( matchedFile ) )
        {
            String q =
            "SELECT f "
            + "FROM Module m JOIN m.fileCollection f "
            + "WHERE m.moduleId = :moduleId";
            fileQuery = NbNppTraceMonitorPUEntityManager.createQuery ( q );
            fileList.clear();
            fileList.addAll ( fileQuery.setParameter( "moduleId", moduleId ).getResultList() );
        }
    }
    
    @Override
    public void     serverDisconnectedSlot( )
    {
        JOptionPane.showMessageDialog (this, "Server closed Connection.", "Disconnect Event", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void    refreshTable()  {
        
        LOGGER.log( Level.FINEST, "Refreshing table");
        
        // Query database for newest records
        List<ModuleTrace> newTraceRecordList;
                if( moduleTraceQueryString.contains("WHERE"))
                {
                    newTraceRecordList = entityMgr.createQuery (moduleTraceQueryString + " AND m.timestamp > :lastTableRefresh order by m.timestamp", ModuleTrace.class).
                    setParameter("lastTableRefresh", lastTableRefresh).
                    getResultList ();
                }
                else
                {
                    newTraceRecordList = entityMgr.createQuery (moduleTraceQueryString + " WHERE m.timestamp > :lastTableRefresh order by m.timestamp", ModuleTrace.class).
                    setParameter("lastTableRefresh", lastTableRefresh).
                    getResultList ();
                }
        
        
        if( newTraceRecordList.size() < 1)
        {
            return;
        }
        
        // Decode the message for all new traces
        for(ModuleTrace t : newTraceRecordList)
        {
            decodeTraceMessage( t );
        }
        
        // Append new records to the list, this will automically affect the table through beans binding
        synchronized( LOCK )
        {
            moduleTraceList.addAll( newTraceRecordList );
        }
        
        // Remember the timestamp from the newest trace for comparison
        lastTableRefresh = newTraceRecordList.get( newTraceRecordList.size()-1 ).getTimestamp ();
    
        // Autoscroll to last column
         traceTbl.scrollRectToVisible(traceTbl.getCellRect(traceTbl.getRowCount()-1, 0, true));
    
    }
    
    /**
     * TableCellRenderer controls how a table cell is rendered.
     * For the trace monitor it changes the foreground color of an 
     * entire row depending on the trace level.
     */
    public class    MyTableCellRenderer extends DefaultTableCellRenderer {
//        private SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
        private SimpleDateFormat df = new SimpleDateFormat( "HH:mm:ss.SSS" );
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
           
            
            // Format Date
            if(value instanceof Date)
            {
                df.setTimeZone ( TimeZone.getTimeZone ( "GMT+0:00")); // London
                value = df.format(value);
            }
            
            
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    /**
     * A JTable object can inform multiple listeners about changes
     * in the selection. In the constructor, I've added an instance
     * of this class to the moduleTbl. This way the program can reload
     * the fileTbl to show the files of the selected module
     */
    public class    ModuleTblSelectionListener implements ListSelectionListener  {

        private JTable table;
        
        public ModuleTblSelectionListener( JTable table ) {
            this.table = table;
        }
        
        @Override
        public void valueChanged(ListSelectionEvent evt)
        {
            
            if( evt.getValueIsAdjusting() )
            {
                return;
                // Mouse button has not yet been released
            }
            
            if( evt.getSource() == table.getSelectionModel() 
                    && table.getRowSelectionAllowed() )
            {
                LOGGER.log( Level.FINEST, "Selection changed!");
                
                // Get MODULE object from selected table row
                Module selectedModule=null;
                try
                {
                    selectedModule = moduleList.get( moduleTbl.convertRowIndexToModel( moduleTbl.getSelectedRow() ) );
                }
                catch( ArrayIndexOutOfBoundsException e)
                {
                    LOGGER.log( Level.SEVERE, "Selected Row has no Model");
                    return;
                }
                catch( IndexOutOfBoundsException e)
                {
                    LOGGER.log( Level.SEVERE, "Selected Row has no Model");
                    return; 
                }
                if( selectedModule == null )
                {
                    LOGGER.log( Level.SEVERE, "Selected Row has no Model");
                    return;
                }
                
                // Set new fileQuery
                String query =
                "SELECT f "
                + "FROM Module m JOIN m.fileCollection f "
                + "WHERE m.moduleId = :moduleId";
                fileQuery = entityMgr.createQuery ( query ); // use entityMgr instead of NbNppTraceMonitor
                fileList.clear();
                fileQuery.setParameter("moduleId", selectedModule.getModuleId());
                fileQuery.setHint (QueryHints.REFRESH, HintValues.TRUE); // wichtig um frische ergebnisse zu erhalten
                fileList.addAll ( fileQuery.getResultList() );

                
            }
            else if( evt.getSource() == table.getColumnModel() && table.getColumnSelectionAllowed() ) {
                int first = evt.getFirstIndex();
                int last = evt.getLastIndex();
                
                System.out.println("c_first: " + first + " c_last: " + last );
            }
                
        }
        
    }
    
    /**
     * Close Event of TraceMonitorFrame. Used to disconnect from device
     * if user simply closes the window
     */
    class           WindowEventHandler extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent evt) {

            if (evt.getSource() instanceof TraceMonitorFrame)
            {
                // Disconnect from device if connected
                TraceMonitorFrame source = (TraceMonitorFrame)evt.getSource();
                source.disconnect(); 
            }

            // Save properties
            try {
                Properties prop = new Properties();
                OutputStream out = new FileOutputStream( new File( SystemUtils.defaultPropertiesDirectory (), "connection.properties") );
                
                prop.setProperty ("host", hostEdt.getText ());
                prop.store(out, "");
                System.out.println("Wrote: " + hostEdt.getText () );    
            } catch(IOException e) { 
                LOGGER.log(Level.WARNING, "IOException while writing properties");
            }
            
            
            
            System.out.println("exit 0");
            System.exit(0);
        }
}
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TraceMonitorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TraceMonitorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TraceMonitorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TraceMonitorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new TraceMonitorFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane LeftRightSplit;
    private javax.persistence.EntityManager NbNppTraceMonitorPUEntityManager;
    private javax.swing.JMenuItem closeItem;
    private javax.swing.JMenuItem colorMnuItem;
    private javax.swing.JButton connectBtn;
    private javax.swing.JMenuItem critLvlMenuItem;
    private javax.swing.JMenuItem debugLvlMenuItem;
    private javax.swing.JButton discardBtn;
    private javax.swing.JMenuItem emergLvlMenuItem;
    private javax.swing.JMenuItem errorLvlMenuItem;
    private java.util.List<com.elster.nppTraceMonitor.db.File> fileList;
    private javax.persistence.Query fileQuery;
    private javax.swing.JPopupMenu fileTableContextMnu;
    private javax.swing.JTable fileTbl;
    private javax.swing.JScrollPane fileTblScrollPane;
    private javax.swing.JMenuItem filterByFileNameMenuItem;
    private javax.swing.JMenuItem filterByLineMenuItem;
    private javax.swing.JMenuItem filterByLogLevelMenuItem;
    private javax.swing.JMenu filterByMenu;
    private javax.swing.JMenuItem filterByMessageMenuItem;
    private javax.swing.JMenuItem filterByModuleIdMenuItem;
    private javax.swing.JTextField hostEdt;
    private javax.swing.JMenuItem infoLvlMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JSplitPane leftSplit;
    private javax.swing.JMenuItem loggingConsoleItem;
    private java.util.List<com.elster.nppTraceMonitor.db.Module> moduleList;
    private javax.persistence.Query moduleQuery;
    private javax.swing.JTable moduleTbl;
    private javax.swing.JScrollPane moduleTblScrollPane;
    private java.util.List<com.elster.nppTraceMonitor.db.ModuleTrace> moduleTraceList;
    private javax.persistence.Query moduleTraceQuery;
    private javax.swing.JMenuItem noticeLvlMenuItem;
    private javax.swing.JMenuItem pathMnuItem;
    private javax.swing.JButton pauseBtn;
    private javax.swing.JButton playBtn;
    private javax.swing.JButton searchBtn;
    private javax.swing.JToolBar toolbar;
    private javax.swing.JPopupMenu traceTableContextMnu;
    private javax.swing.JTable traceTbl;
    private javax.swing.JScrollPane traceTblScrollPane;
    private javax.swing.JMenuItem warnLvlMenuItem;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
