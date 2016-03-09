
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.net;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.elster.nppTraceMonitor.net.TMPToken;
import java.io.*;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.ConsoleHandler;


/**
 *
 * @author wesselst
 */
public class ConnectionReader implements Runnable{
    
    private final Set<IThreadListener> threadListeners = new CopyOnWriteArraySet<IThreadListener>();
    private final Set<IConnectionReaderListener> connectionReaderListeners = new CopyOnWriteArraySet<IConnectionReaderListener>();
    private Socket pSocket = null;
    private DataInputStream in = null;
    private byte[] buffer;
    private TMPToken token;
    private byte[] dataField;
    private int length;
    
    private static final Logger LOGGER = Logger.getLogger( ConnectionReader.class.getName() );
    
    public ConnectionReader(final Socket pSocket) {
        this.pSocket = pSocket;
        
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.WARNING);
        LOGGER.addHandler(console);
        LOGGER.setLevel(Level.WARNING);
    }
    
    
    @Override
    public void run() {
        
        try 
        {
            in = new DataInputStream( pSocket.getInputStream() );
            
            /* reading loop */
            while( true ) 
            {

                /* read token and length */
                buffer = new byte[2];
                try 
                {
                    in.readFully(buffer, 0, 2); /* read() blocks. To terminate thread one has to
                    * close the InputStream from outside the thread. I've overwritten the
                    * interrupt() method of this thread to also close 'in'. So you can end
                    * the thread with an interrupt, which is the proper way in java
                    */
                }
                catch( EOFException e )
                {
                    LOGGER.log( Level.WARNING, "Buffer not fully read!");
                    continue;
                }
                
                token = new TMPToken( buffer[0] );
                length = (int) buffer[1];
                
                /* read following data */
                if(length > 0)
                {
                    buffer = new byte[length];
                    try 
                    {
                        in.readFully(buffer, 0, length);
                    }
                    catch(EOFException e)
                    {
                        LOGGER.log( Level.WARNING, "Buffer not fully read!");
                        continue;  
                    }
                    dataField = buffer.clone();
                }
                else
                {
                    dataField = null;
                }
                

               // Server disconnect
                if( token.isFromDevice() && token.isDisconnectCommand() )
                {
                    LOGGER.info("Server disconnected!");
                    serverDisconnectedSignal ();
                    return; // exit thread
                }
                
                processToken( token, dataField );

            }
        } 
        catch( SocketException e)
        {
            LOGGER.log( Level.FINER, "Socket automatically closed because the Writer closed his OutputStream" );
        } 
        catch( InterruptedIOException e) 
        {
            Thread.currentThread().interrupt(); //important
            LOGGER.log(Level.FINER, "Interrupted via InterruptedIOException");
        } 
        catch( IOException e ) 
        {
            if( !Thread.currentThread().isInterrupted() ) {
                LOGGER.log( Level.FINER, "{0}", e);
            }
            else {
                LOGGER.log(Level.WARNING, "Interrupted!");
            }
        } 
        finally 
        {
            closeInputStream();
            threadFinishedSignal();
        }
    }
    
    private void processToken( TMPToken pToken, final byte[] pDataField )
    {

//        TimeStamp
        if( pToken.isFromDevice() && pToken.isConnectCommand() )
        {
            timestampReceivedSignal( pDataField );
        }
        
//        Trace Data
        else if( pToken.isFromDevice() && pToken.isTraceCommand() )
        {
            traceMessageReceivedSignal( pDataField );
        }
        
        
//        Module
        else if( pToken.isFromDevice() && pToken.isModuleCommand() )
        {
            moduleReceivedSignal( pDataField );
            
        }
        
//        Level
        else if( pToken.isFromDevice() && pToken.isLevelCommand() )
        {
            traceLevelReceivedSignal( pDataField );
        }
        
//        Idle
        else if( pToken.isFromDevice() && pToken.isIdleCommand() ) 
        {
            idleReceivedSignal();
        }
        
//        Unknown Token
        else
        {
            LOGGER.log(Level.WARNING, "Token not recognized!");
        }
        
    }
    
    public void closeInputStream()
    {
       try 
       {
           in.close();
           LOGGER.log( Level.FINE, "Closed InputStream");
       } 
       catch( IOException e) 
       {
           // quiet close
       }
    }
    
    
    
    
    /* Thread observation */
    public final void addListener(final IThreadListener listener)
    {
        threadListeners.add( listener );
    
    }
    
    public final void removeListener( IThreadListener listener )
    {
        threadListeners.remove( listener  );
    }
    
    public final void threadFinishedSignal( )
    {
        for( IThreadListener listener : threadListeners)
        {
            listener.threadFinishedSlot( Thread.currentThread() );
        }
    }
    
    /* ConnectionReader observation */
    public final void addListener(final IConnectionReaderListener listener)
    {
        connectionReaderListeners.add( listener );
    }
    
    public final void removeListener( IConnectionReaderListener listener )
    {
        connectionReaderListeners.remove( listener );
    }
    
    
    public final void idleReceivedSignal() {
        for( IConnectionReaderListener listener : connectionReaderListeners ) {
            listener.idleSlot();
        }
    }
    
    public final void timestampReceivedSignal( final byte[] timestamp )    {
        for( IConnectionReaderListener listener: connectionReaderListeners )        {
            listener.timestampSlot( timestamp );
        }
    }
    
    public final void traceMessageReceivedSignal( final byte[] trace )    {
        for( IConnectionReaderListener listener: connectionReaderListeners )        {
            listener.traceSlot( trace );
        }
    }
    
    public final void moduleReceivedSignal( final byte[] module )    {
        for( IConnectionReaderListener listener: connectionReaderListeners )        {
            listener.moduleSlot( module );
        }    
    }
    
    
    public final void traceLevelReceivedSignal( final byte[] tracelevel ) {
        for( IConnectionReaderListener listener: connectionReaderListeners ) {
            listener.traceLevelSlot( tracelevel );
        }      
    }
    
    public final void serverDisconnectedSignal( ) {
        for( IConnectionReaderListener listener : connectionReaderListeners ) {
            listener.serverDisconnectedSlot ();
        }
    }
}
