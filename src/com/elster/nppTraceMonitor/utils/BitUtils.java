/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.utils;

import com.elster.nppTraceMonitor.net.TMP;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A collection of bit- and byte-manipulating
 * functions.
 * 
 * @author wesselst
 */
public class BitUtils {
    
    public static byte[]    byteArrayFromInt(int value, int length) {
        byte[] data = new byte[length];
        
        // littleEndian
        if( length > 0 )
        {
            data[0] = (byte) value ;
        }
        if( length > 1 )
        {
            data[1] = (byte) (value >> 8);
        }
        if( length > 2 )
        {
            data[2] = (byte) (value >> 16 );
        }
        if( length > 3)
        {
            data[3] =  (byte) (value >> 24);
        }
        else
        {
            System.out.println("Length doesn't make any sense. Integer.SIZE = 32");
        }
        
        return data;
 
    }

    public static int       intFromByteArray(byte[] data, int offset, int length) {
        int value = 0;

        try
        {

            if (length > 3) {
                value += (data[offset + length - 4] & 0xFF) << 24;
            }
            if (length > 2) {
                value += (data[offset + length - 3] & 0xFF) << 16;
            }
            if (length > 1) {
                value += (data[offset + length - 2] & 0xFF) << 8;
            }
            if (length > 0) {
                value += (data[offset + length - 1] & 0xFF);
            }

        }
        catch( IndexOutOfBoundsException e)
        {
            System.out.println("IndexOutOfBoundsException: Byte array cannot be parsed to int ");
        }

        return value;
    }

    public static short     swap (short value)
    {
        int b1 = value & 0xff;
        int b2 = (value >> 8) & 0xff;

        return (short) (b1 << 8 | b2 << 0);
    }

    public static long      longFromByteArray( byte[] data, ByteOrder order )
    {
        long value = 0;
        for (int i = 0; i < data.length; i++)
        {
            if( order == ByteOrder.LITTLE_ENDIAN)
            {
                value += ((long) data[i] & 0xffL) << (8 * i);
            }
            else
            {
                value = (value << 8) + (data[i] & 0xff);
            }
        }
        return value;
    }
    

    /**
    * Byte swap a single int value.
    * 
    * @param value  Value to byte swap.
    * @return       Byte swapped representation.
    */
    public static int swap (int value)
    {
        int b1 = (value >>  0) & 0xff;
        int b2 = (value >>  8) & 0xff;
        int b3 = (value >> 16) & 0xff;
        int b4 = (value >> 24) & 0xff;

        return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
    }



    /**
    * Byte swap a single long value.
    * 
    * @param value  Value to byte swap.
    * @return       Byte swapped representation.
    */
    public static long swap (long value)
    {
        long b1 = (value >>  0) & 0xff;
        long b2 = (value >>  8) & 0xff;
        long b3 = (value >> 16) & 0xff;
        long b4 = (value >> 24) & 0xff;
        long b5 = (value >> 32) & 0xff;
        long b6 = (value >> 40) & 0xff;
        long b7 = (value >> 48) & 0xff;
        long b8 = (value >> 56) & 0xff;

        return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
            b5 << 24 | b6 << 16 | b7 <<  8 | b8 <<  0;
    }



    /**
    * Byte swap a single float value.
    * 
    * @param value  Value to byte swap.
    * @return       Byte swapped representation.
    */
    public static float swap (float value)
    {
        int intValue = Float.floatToIntBits (value);
        intValue = swap (intValue);
        return Float.intBitsToFloat (intValue);
    }



    /**
    * Byte swap a single double value.
    * 
    * @param value  Value to byte swap.
    * @return       Byte swapped representation.
    */
    public static double swap (double value)
    {
        long longValue = Double.doubleToLongBits (value);
        longValue = swap (longValue);
        return Double.longBitsToDouble (longValue);
    }



    /**
    * Byte swap an array of shorts. The result of the swapping
    * is put back into the specified array.
    *
    * @param array  Array of values to swap
    */
    public static void swap (short[] array)
    {
        for (int i = 0; i < array.length; i++)
        array[i] = swap (array[i]);
    }



    /**
    * Byte swap an array of ints. The result of the swapping
    * is put back into the specified array.
    * 
    * @param array  Array of values to swap
    */
    public static void swap (int[] array)
    {
        for (int i = 0; i < array.length; i++)
        array[i] = swap (array[i]);
    }



    /**
    * Byte swap an array of longs. The result of the swapping
    * is put back into the specified array.
    * 
    * @param array  Array of values to swap
    */
    public static void swap (long[] array)
    {
        for (int i = 0; i < array.length; i++)
        array[i] = swap (array[i]);
    }



    /**
    * Byte swap an array of floats. The result of the swapping
    * is put back into the specified array.
    * 
    * @param array  Array of values to swap
    */
    public static void swap (float[] array)
    {
        for (int i = 0; i < array.length; i++)
        array[i] = swap (array[i]);
    }



    /**
    * Byte swap an array of doubles. The result of the swapping
    * is put back into the specified array.
    * 
    * @param array  Array of values to swap
    */
    public static void swap (double[] array)
    {
        for (int i = 0; i < array.length; i++)
        array[i] = swap (array[i]);
    }
    
}
