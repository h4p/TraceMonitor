/*
 * For specification of the TM-Protocol, please have a look at:
 * http://10.49.120.53:8081/display/NPP/enCore+Trace+Monitor+Protocol
 * 
 */
package com.elster.nppTraceMonitor.net;

/**
 *
 * @author wesselst
 */
public class TMPToken {
    
    private byte token;

    public TMPToken ()
    {
        
    }
    public TMPToken( final byte pToken )
    {
        this.token = pToken;
    }
    
    /* Bitmasks */
    public static final byte DIRECTION_BITMASK = (byte)0x20;
    public static final byte NOTUSED_BITMASK   = (byte)0x80;
    public static final byte COMMAND_BITMASK   = (byte)0x5F;
    
    /* Directions */
    public static final byte PC         = (byte)0x00;
    public static final byte DEVICE     = (byte)0x20;
    
    /* Commands */
    public static final byte CONNECT    = (byte)0x43; 
    public static final byte IDLE       = (byte)0x49; // alive signal
    public static final byte TRACE      = (byte)0x54; // trace data
    public static final byte MODULE     = (byte)0x4D; // request or get module(list) from device
    public static final byte LEVEL      = (byte)0x4C; // set trace level for source file
    public static final byte DISCONNECT = (byte)0x44; // disconnect from device
    
    
    public void setToken( final byte pToken )
    {
        this.token = pToken;
    }
    
    public byte getToken()
    {
        return token;
    }
    
    public boolean isFromPC( )
    {
        return PC == (byte)( token & DIRECTION_BITMASK);
    }
    
    public boolean isFromDevice( )
    {
        return DEVICE   == (byte)(token & DIRECTION_BITMASK);
    }
    
    public boolean isConnectCommand( )
    {
        return CONNECT  == (byte)( token & TMPToken.COMMAND_BITMASK );
    }
    public boolean isIdleCommand( )
    {
        return IDLE     == (byte)( token & TMPToken.COMMAND_BITMASK );
    }
        
    public boolean isTraceCommand( )
    {
        return TRACE    == (byte)( token & TMPToken.COMMAND_BITMASK );
    }
            
    public boolean isModuleCommand( )
    {
        return MODULE   == (byte)( token & TMPToken.COMMAND_BITMASK );
    }
    
    public boolean isLevelCommand( )
    {
        return LEVEL    == (byte)( token & TMPToken.COMMAND_BITMASK );
    }
    
    public boolean isDisconnectCommand( )
    {
        return DISCONNECT == (byte)( token & TMPToken.COMMAND_BITMASK );
    }
    
}

