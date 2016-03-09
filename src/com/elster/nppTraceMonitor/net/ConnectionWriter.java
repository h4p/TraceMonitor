/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.elster.nppTraceMonitor.net.TMPToken;
import com.elster.nppTraceMonitor.utils.BitUtils;
import java.io.*;
import java.net.SocketException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.ConsoleHandler;

/**
 *
 * @author wesselst
 */
public class ConnectionWriter implements Runnable{
    
    private final Set<IThreadListener> listeners = new CopyOnWriteArraySet<IThreadListener>();
    
    private Socket pSocket;
    private DataOutputStream out;
    private final LinkedList<TMCommand> commandQueue = new LinkedList<TMCommand>();
    
    private static final Logger LOGGER = Logger.getLogger( ConnectionWriter.class.getName() );
    
    
    public ConnectionWriter( final Socket pSocket ) {
        this.pSocket = pSocket;
        
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.FINE);
        LOGGER.addHandler(console);
        LOGGER.setLevel(Level.FINE);
    }
    
    
    @Override
    public void run() 
    {
        
        try {
            out = new DataOutputStream ( pSocket.getOutputStream() );
            
            /* send CONNECT */
            LOGGER.log( Level.FINER, "CONNECTing");
            out.write( TMPToken.CONNECT | TMPToken.PC );
            out.write( 0x01 ); // length
            out.write( TMP.VERSION );
            
            /* request ALL MODULE INFO */
            LOGGER.log( Level.FINER, "Requesting ALL MODULE information");
            out.writeByte( TMPToken.MODULE | TMPToken.PC );
            out.writeByte( 0x00 );

            /* writing loop */
            while( !Thread.currentThread().isInterrupted() )
            {
                    
                    /* Check the command queue for new items */
                    synchronized( commandQueue )
                    {
                        while ( commandQueue.isEmpty() )
                        {
                            /* Wait for a command, but not forever because of the idle signal */
                            commandQueue.wait( TMP.IDLE_PERIOD - TMP.IDLE_PERIOD_OFFSET );
                            
                            /* Idle signal */
                            LOGGER.log( Level.FINE, "Idle" );
                            // TODO: write doesn't file if target is not connected anymore
                            out.writeByte ( TMPToken.PC | TMPToken.IDLE );
                            out.writeByte ( 0x00 );
                        }
                    }
                    
                    /* Get the command */
                    TMCommand command = commandQueue.poll();

                    /* Write it */
                    out.writeByte( command.getCommandId() );
                    out.writeByte( command.getLength() );
                    if( command.getLength() > 0)
                    {
                        LOGGER.log( Level.INFO, "Writing command");
                        out.write( command.getDataField(), 0, command.getLength() );
                        out.flush();
                    }

                    /* Close connection if user send disconnect signal */
                    if (command.getCommandId() == TMPToken.DISCONNECT)
                    {
                        LOGGER.log( Level.INFO, "Client disconnect");
                        break;
                    }
                    

            }
            
        } 
        catch( SocketException e)
        {
            LOGGER.log(Level.FINE, "Socket automatically closed because the Reader closed his InputStream");
        }
        catch( InterruptedException e)
        {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.FINE, "Interrupted!");
        } 
        catch( InterruptedIOException e ) 
        {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.FINE, "Interrupt via InterruptedIOException");
        } 
        catch( IOException e ) 
        {
            if( !Thread.currentThread().isInterrupted() ) {
                LOGGER.log( Level.FINE, "{0}", e);
            }
            else {
                LOGGER.log( Level.FINE, "Interrupted!");
            }
        } 
        finally 
        {
            closeOutputStream();
            threadFinishedSignal();
        }
    }
    
    public synchronized void  requestInformationForAllModules()
    {
        synchronized(commandQueue)
        {
            commandQueue.offer( new TMCommand(
                                (byte)(TMPToken.PC | TMPToken.MODULE),
                                0,
                                null
                                )
                            );
            commandQueue.notifyAll();
            LOGGER.log( Level.INFO, "About to write command: 'Request Information For All Modules'");
        }
    }

    public void requestInformationForASingleModule( final byte pModuleId )
    {
        synchronized(commandQueue)
        {
            commandQueue.offer( new TMCommand(
                                (byte)(TMPToken.PC | TMPToken.MODULE),
                                1,
                                new byte[] {(byte)pModuleId }
                                )
                            );
            commandQueue.notifyAll();
            LOGGER.log( Level.INFO, "About to write command: 'Request Information For A Single Module'");
        }
    }
    
    public void requestTraceLevelTable()
    {
        synchronized(commandQueue)
        {
            commandQueue.offer( new TMCommand(
                                (byte)(TMPToken.PC | TMPToken.LEVEL),
                                0,
                                null
                                )
                            );
            commandQueue.notifyAll();
            LOGGER.log( Level.INFO, "About to write command: 'Request TraceLevel Table'");
        }
    }
    
    public void requestModuleTraceLevelTable( final byte pModuleId)
    {
        synchronized(commandQueue)
        {
            commandQueue.offer( new TMCommand(
                                (byte)(TMPToken.PC | TMPToken.LEVEL),
                                1,
                                new byte[]{ (byte)pModuleId }
                                )
                            );
            commandQueue.notifyAll();
            LOGGER.log( Level.INFO, "About to write command: 'Request Module TraceLevel Table'");
        }
    }
    
    public void requestFileTraceLevel( final Integer pModuleId, final Integer pFileIndex )
    {
        synchronized(commandQueue)
        {
            Integer data;
            data  = (pModuleId  << TMP.MODULE_ID_SHIFT)     & TMP.MODULE_ID_BITMASK;
            data |= (pFileIndex << TMP.FILE_INDEX_SHIFT)    & TMP.FILE_INDEX_BITMASK;
            byte[] dataArray = BitUtils.byteArrayFromInt(data, 4);
            
            
            commandQueue.offer( new TMCommand(
                                (byte)(TMPToken.PC | TMPToken.LEVEL),
                                2,
                                new byte[] { dataArray[2], dataArray[3] }
                                )
                            );
            commandQueue.notifyAll();
            LOGGER.log( Level.INFO, "About to write command: 'Request File TraceLevel Table'");
        }
    }
    
    public void setFileTraceLevel( final Integer pModuleId, final Integer pFileIndex, final TraceLevel pLevel )
    {
        Integer ids;
        ids  = (pModuleId  << TMP.MODULE_ID_SHIFT)     & TMP.MODULE_ID_BITMASK;
        ids |= (pFileIndex << TMP.FILE_INDEX_SHIFT)    & TMP.FILE_INDEX_BITMASK;
        byte[] dataArray = BitUtils.byteArrayFromInt(ids, 4);
        
        synchronized(commandQueue)
        {
            commandQueue.offer( new TMCommand(
                                (byte)(TMPToken.PC | TMPToken.LEVEL),
                                3,
                                new byte[]{ dataArray[2], dataArray[3], (byte)( pLevel.getValue () & TMP.TRACE_LEVEL_BITMASK ) }
                                )
                            );
            commandQueue.notifyAll();
            LOGGER.log( Level.INFO, "About to write command: 'Set File TraceLevel'");
        }
    }
    
    public void closeConnection()
    {
        synchronized(commandQueue)
        {
            commandQueue.offer( new TMCommand((byte)(TMPToken.PC | TMPToken.DISCONNECT), 0, null));
            commandQueue.notifyAll();
            LOGGER.log( Level.INFO, "About to write command: 'Close Connection'");
        }
    }
    
    public void closeOutputStream()
    {
       try 
       {
           out.close();
           LOGGER.log( Level.FINE, "Closed OutputStream");
       } 
       catch( IOException e) 
       {
           // quiet close
       }
    }
      
    
    /* Listener registration */
    public final void addListener(final IThreadListener listener)
    {
        listeners.add( listener );
    
    }
    
    public final void removeListener( IThreadListener listener )
    {
        listeners.remove( listener  );
    }
    
    public final void threadFinishedSignal( )
    {
        for( IThreadListener listener : listeners)
        {
            listener.threadFinishedSlot( Thread.currentThread() );
        }
    }
}
