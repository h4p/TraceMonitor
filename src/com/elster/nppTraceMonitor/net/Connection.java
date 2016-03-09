/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.net;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 *
 * @author wesselst
 */
public class Connection implements IThreadListener{
    
    private final Set<IConnectionListener> listeners = new CopyOnWriteArraySet<IConnectionListener>();
    
    private String ip;
    private String port;
    private Socket socket;
    
    private Date time = new Date();
    private ConnectionReader reader = null;
    private Thread readerThread;
    private ConnectionWriter writer = null;
    private Thread writerThread;
    
    private Object parent;
    
    private static final Logger LOGGER = Logger.getLogger(Connection.class.getName());
    
    
    public Connection(Object parent, String ip, String port)
    {
        this.parent = parent;
        this.ip = ip;
        this.port = port;
        
    }
    
    public void connect()
    {
        try {
            socket = new Socket(  );    // creates an unconnected socket
            socket.connect( new InetSocketAddress(ip, Integer.parseInt ( port )), TMP.SOCKET_TIMEOUT);
        }
        catch( UnknownHostException e)
        {
            unknownHostSignal();
            connectionClosedSignal();
            return;
        }
        catch( SocketTimeoutException e)
        {
            //e.printStackTrace()
            connectionRefusedSignal();
            connectionClosedSignal();
            return;
        }
        catch( IOException e )
        {
            LOGGER.log( Level.SEVERE, "{0}", e);
            connectionClosedSignal();
            return;
        }
        
        
        /* reader and writer threads */
        reader = new ConnectionReader( socket);
        writer = new ConnectionWriter( socket);
        reader.addListener ( parent instanceof IConnectionReaderListener ? (IConnectionReaderListener)parent : null );
        reader.addListener ( this  );
        writer.addListener ( this );
        
        
        readerThread = new Thread( reader ) {
            @Override
            public void interrupt()
            {
                super.interrupt();
                // reader.closeInputStream(); // finally-block in run() already does this
            }
        };
        readerThread.setName("readerThread");
        readerThread.start();

        writerThread = new Thread( writer );
        writerThread.setName("writerThread");
        writerThread.start();

    } 
    
    public void disconnect()
    {
        // If user closes the window the disconnect command is executed even if
        // no connection was established
        if( writerThread == null || readerThread == null )
        {
            return;
        }
        
        writerThread.interrupt();
        readerThread.interrupt();
    }
    
    /* Getters & Setters */
    public Socket getSocket() 
    {
        return socket;
    }
    
    public Date getTime() 
    {
        return time;
    }
    
    public ConnectionReader getReader()
    {
        return reader;
    }
    
    public ConnectionWriter getWriter()
    {
        return writer;
    }
    

    public void setHost(String host) {
        this.ip = host;
    }
    
    /* Listener registration */
    public final void addListener(final IConnectionListener listener)
    {
        listeners.add( listener );
    
    }
    
    public final void removeListener( IConnectionListener listener )
    {
        listeners.remove( listener  );
    }
    
    /* Signals */
    private void connectionClosedSignal()
    {
        LOGGER.log(Level.INFO, "Connection Close Signal");
        for( IConnectionListener listener: listeners )
        {
                listener.connectionClosedSlot();
        }
        
    }
    
    private void unknownHostSignal()
    {
        LOGGER.log(Level.SEVERE, "Unknown Host");
        for( IConnectionListener listener: listeners )
        {
                listener.unkonwnHostSlot();
        }
    }
    
    private void connectionRefusedSignal()
    {
        LOGGER.log(Level.SEVERE, "Connection refused. Server may not be running");
        for( IConnectionListener listener : listeners )
        {
            listener.connectionRefusedSlot();
        }
    }
    
    /* Slots */
    @Override
    public void threadFinishedSlot( Thread t)
    {
        
        // One of the threads has finished, close the other one and
        // inform the gui to change the icon
        
        if( t.getName().equalsIgnoreCase ( "readerThread") ) // reader thread has finished
        {
            if( !writerThread.isInterrupted () ) {
                writerThread.interrupt ();
            }
        }
        if( t.getName().equalsIgnoreCase ( "writerThread") )
        {
            if( !readerThread.isInterrupted () ) {
                readerThread.interrupt ();
            }
        }
        connectionClosedSignal (); // is called twice but this is no problem
    }
    
}
