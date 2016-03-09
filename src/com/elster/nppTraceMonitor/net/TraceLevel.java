/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.net;

import java.awt.Color;

/**
 *
 * @author wesselst
 */
public enum TraceLevel {
    OFF     (0),
    EMERG   (1),
    CRIT    (2),
    ERR     (3),
    WARN    (4),
    NOTICE  (5),
    INFO    (6),
    DEBUG   (7),
    ALL     (8);
    
    

    
    private final int level;
    
    TraceLevel(int level) 
    { 
        this.level = level; 
    }
    
    public int getValue()
    {
        return level;
    }
    
    public static TraceLevel getLevel( int value )
    {
        for( TraceLevel l : TraceLevel.values () )
        {
            if(l.getValue () == value )
            {
                return l;
            }
        }
        return null;
    }
}
