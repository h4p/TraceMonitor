package com.elster.nppTraceMonitor.db;

import com.elster.nppTraceMonitor.db.File;
import com.elster.nppTraceMonitor.db.ModuleTrace;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.0.v20110604-r9504", date="2012-07-05T17:17:29")
@StaticMetamodel(Trace.class)
public class Trace_ { 

    public static volatile SingularAttribute<Trace, File> fileId;
    public static volatile SingularAttribute<Trace, String> formatString;
    public static volatile SingularAttribute<Trace, Integer> traceId;
    public static volatile SingularAttribute<Trace, Integer> traceIndex;
    public static volatile SingularAttribute<Trace, Integer> line;
    public static volatile CollectionAttribute<Trace, ModuleTrace> moduleTraceCollection;

}