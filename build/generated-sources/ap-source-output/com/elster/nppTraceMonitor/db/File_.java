package com.elster.nppTraceMonitor.db;

import com.elster.nppTraceMonitor.db.Module;
import com.elster.nppTraceMonitor.db.Trace;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.0.v20110604-r9504", date="2012-07-05T17:17:29")
@StaticMetamodel(File.class)
public class File_ { 

    public static volatile SingularAttribute<File, Integer> fileId;
    public static volatile SingularAttribute<File, Integer> fileIndex;
    public static volatile SingularAttribute<File, Integer> logLevel;
    public static volatile SingularAttribute<File, String> fileName;
    public static volatile CollectionAttribute<File, Trace> traceCollection;
    public static volatile CollectionAttribute<File, Module> moduleCollection;

}