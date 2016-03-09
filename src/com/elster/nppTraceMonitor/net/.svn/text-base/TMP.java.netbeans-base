/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.net;

import java.nio.ByteOrder;

/**
 * The Trace Message Protocol.
 * 
 * @author wesselst
 */
public class TMP {
    
    public static final byte        VERSION                 = 0x20;
    public static final String      PORT                    = "19790";
    public static final ByteOrder   BYTE_ORDER              = ByteOrder.LITTLE_ENDIAN;  // JavaVM representation is always in Big-Endian
    
    // unique ids
    public static final int         MODULE_ID_BITMASK       = 0xFE000000; public static final int     MODULE_ID_SHIFT     = 25;
    public static final int         FILE_INDEX_BITMASK      = 0x01FF0000; public static final int     FILE_INDEX_SHIFT    = 16;
    public static final int         TRACE_INDEX_BITMASK     = 0x0000FFF0; public static final int     TRACE_INDEX_SHIFT   = 4;
    public static final int         TRACE_LEVEL_BITMASK     = 0x00000007; public static final int     TRACE_LEVEL_SHIFT   = 0;
    public static final int         MODULE_ID_SHORT_BITMASK = 0x7F;
    
    // timestamp
    public static final int         TIME_DST_BITMASK        = 0x0000FFFE; public static final int     TIME_DST_SHIFT      = 1;
    public static final int         TIME_DAYLIGHT_BITMASK   = 0x00000001; public static final int     TIME_DAYLIGHT_SHIFT = 0;
    // arguments
    public static final int         ARGS_LOST_BITMASK       = 0x80000000; public static final int     ARGS_LOST_SHIFT     = 31;
    public static final int         ARGS_FIRST_BITMASK      = 0x38000000; public static final int     ARGS_FIRST_SHIFT    = 27;
    public static final int         ARGS_SECOND_BITMASK     = 0x07000000; public static final int     ARGS_SECOND_SHIFT   = 24;
    public static final int         ARGS_THIRD_BITMASK      = 0x00E00000; public static final int     ARGS_THIRD_SHIFT    = 21;
    public static final int         ARGS_FOURTH_BITMASK     = 0x001C0000; public static final int     ARGS_FOURTH_SHIFT   = 18;
    public static final int         ARGS_FIFTH_BITMASK      = 0x00038000; public static final int     ARGS_FIFTH_SHIFT    = 15;
    public static final int         ARGS_SIXTH_BITMASK      = 0x00007000; public static final int     ARGS_SIXTH_SHIFT    = 12;
    public static final int         ARGS_SEVENTH_BITMASK    = 0x00000E00; public static final int     ARGS_SEVENTH_SHIFT  = 9;
    public static final int         ARGS_EIGHTH_BITMASK     = 0x000001C0; public static final int     ARGS_EIGHTH_SHIFT   = 6;
    public static final int         ARGS_NINTH_BITMASK      = 0x00000038; public static final int     ARGS_NINTH_SHIFT    = 3;
    public static final int         ARGS_TENTH_BITMASK      = 0x00000007; public static final int     ARGS_TENTH_SHIFT    = 0;
    
    
    
    /* Lengths */
    public static final byte        TIMESYNC1_LEN           = (byte)0x0A;
    public static final byte        TIMESYNC2_LEN           = (byte)0x0E;
    public static final byte        TYPELIST_LEN            = (byte)0x0E;
    
    
    
    /* Limits */
    public static final int         TRACE_MAX_AFBS          = 128;      // concurrently traced afbs
    public static final int         TRACE_MAX_FILES         = 128;      // max files of one afb
    public static final int         TRACE_MAX_KERNEL_FILES  = 500;
    public static final int         TRACE_INNER_LENGTH      = 120;      // pc->device
    public static final int         TRACE_OUTER_LENGTH      = 128;      // device->pc
    public static final int         IDLE_PERIOD             = 2000;     // after 5 seconds the device automatically disconnects
    public static final int         IDLE_PERIOD_OFFSET      = 1000;     // IDLE_PERIOD - IDLE_PERIOD_OFFSET = 3s 
    public static final int         SOCKET_TIMEOUT          = 200;     // Time till ConnectionRefusedException 
    public static final int         TRACE_MESSAGE_MIN_LENGTH= 10;       // includes timestamp(6 Bytes), unique ids(4 Bytes)
    public static final int         TRACE_LEVEL_MIN_LENGTH  = 3;        // includes moduleId, fileIndex and level
    public static final int         MODULE_MESSAGE_MIN_LENGTH=3;        // moduleId, moduleName(0-terminated)
    
}
