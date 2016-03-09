/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.net;

/**
 *
 * @author wesselst
 */
public interface IConnectionReaderListener {
    
    public void idleSlot();
    public void timestampSlot( final byte[] timestamp );
    public void traceSlot( final byte[] trace );
    public void moduleSlot( final byte[] module );
    public void traceLevelSlot( final byte[] tracelevel );
    public void serverDisconnectedSlot( );
    
}
