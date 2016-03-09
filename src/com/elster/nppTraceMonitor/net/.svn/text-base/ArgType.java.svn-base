/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.net;

/**
 * All possible datatypes the 
 * tracer is able to send.
 * 
 * @author wesselst
 */
public enum ArgType {
    
    NONE            (0),
    STRING          (1),    // 0-terminated
    DOUBLE          (2),    // 8 Byte
    LONG_INT        (3),    // 8 Byte
    INT             (4),    // 4 Byte
    SHORT_INT       (5),    // 2 Byte
    CHAR            (6);    // 1 Byte
    
    private final int type;
    
    ArgType(int type)
    {
        this.type = type;
    }
 
    public int getType()
    {
        return type;
    }
    
    public static ArgType getArgType( int type )
    {
        for( ArgType t : ArgType.values() )
        {
            if( t.getType() == type )
            {
                return t;
            }
        }
        return null;
    }
}
