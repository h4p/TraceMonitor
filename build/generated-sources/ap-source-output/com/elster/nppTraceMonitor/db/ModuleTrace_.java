package com.elster.nppTraceMonitor.db;

import com.elster.nppTraceMonitor.db.Module;
import com.elster.nppTraceMonitor.db.Trace;
import java.io.Serializable;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.0.v20110604-r9504", date="2012-07-05T17:17:29")
@StaticMetamodel(ModuleTrace.class)
public class ModuleTrace_ { 

    public static volatile SingularAttribute<ModuleTrace, Date> timestamp;
    public static volatile SingularAttribute<ModuleTrace, Integer> moduleTraceId;
    public static volatile SingularAttribute<ModuleTrace, Module> moduleId;
    public static volatile SingularAttribute<ModuleTrace, Integer> logLevel;
    public static volatile SingularAttribute<ModuleTrace, Integer> argumentTypes;
    public static volatile SingularAttribute<ModuleTrace, Trace> traceId;
    public static volatile SingularAttribute<ModuleTrace, String> decodedMessage;
    public static volatile SingularAttribute<ModuleTrace, Serializable> argumentList;

}