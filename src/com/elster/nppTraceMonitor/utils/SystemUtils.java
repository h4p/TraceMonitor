/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.utils;

import java.io.File;

/**
 *
 * @author lu1721
 */
public class SystemUtils {
    
public static String appName = "TraceMonitor";

public static String defaultPropertiesDirectory()
{
    String OS = System.getProperty("os.name").toUpperCase();
    if (OS.contains("WIN")) {
        if(! new File( System.getenv("APPDATA"), appName).exists() )
            new File( System.getenv("APPDATA"), appName).mkdirs ();
        return new File( System.getenv("APPDATA"), appName).getAbsolutePath ();
    }
    else if (OS.contains("NUX")) {
        if(! new File( System.getenv("user.home"), "."+appName).exists() )
            new File( System.getenv("user.home"), "."+appName).mkdirs ();
        new File( System.getenv("user.home"), "."+appName).mkdirs ();
        return new File( System.getProperty("user.home"), "."+appName).getAbsolutePath ();
    }
    
    if(! new File( System.getenv("user.dir"), appName).exists() )
        new File( System.getenv("user.dir"), appName).mkdirs ();
    new File( System.getProperty ("user.dir")).mkdirs ();
    return new File( System.getProperty("user.dir"), appName ).getAbsolutePath ();
    
}
}
