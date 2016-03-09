package com.elster.nppTraceMonitor.db;

import com.elster.nppTraceMonitor.db.File;
import com.elster.nppTraceMonitor.db.ModuleTrace;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.0.v20110604-r9504", date="2012-07-05T17:17:29")
@StaticMetamodel(Module.class)
public class Module_ { 

    public static volatile SingularAttribute<Module, Integer> moduleId;
    public static volatile SingularAttribute<Module, String> name;
    public static volatile CollectionAttribute<Module, File> fileCollection;
    public static volatile CollectionAttribute<Module, ModuleTrace> moduleTraceCollection;

}