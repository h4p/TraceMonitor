package com.elster.nppTraceMonitor.db;

import java.io.Serializable;
import java.math.BigInteger;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.0.v20110604-r9504", date="2012-07-05T17:17:29")
@StaticMetamodel(ModuleTraceBuffer.class)
public class ModuleTraceBuffer_ { 

    public static volatile SingularAttribute<ModuleTraceBuffer, Integer> fileIndex;
    public static volatile SingularAttribute<ModuleTraceBuffer, Integer> moduleId;
    public static volatile SingularAttribute<ModuleTraceBuffer, Integer> logLevel;
    public static volatile SingularAttribute<ModuleTraceBuffer, Integer> argumentTypes;
    public static volatile SingularAttribute<ModuleTraceBuffer, Integer> traceIndex;
    public static volatile SingularAttribute<ModuleTraceBuffer, BigInteger> ticksSinceTimeSync;
    public static volatile SingularAttribute<ModuleTraceBuffer, Serializable> argumentList;
    public static volatile SingularAttribute<ModuleTraceBuffer, Integer> moduleTraceBufferId;

}