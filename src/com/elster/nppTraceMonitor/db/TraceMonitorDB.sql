-- This SQL file was created manually to create a derby database
-- and fill it with some data. In Netbeans you can create a database
-- by clicking 'Services' and then right-click database and select
-- 'New Connection'.  In the dialog append ";create=true" behind the jdbc-url

-- You only need these tables if you want to create Entity Classes from it.
-- If those already exists, which they do in the com.elster.nppTraceMonitor.db package,
-- derby will know how to save them to a table. On startup it even creates the whole
-- database from these Entity Classes. Look into the persistence.xml file and you'll
-- see that there are all Entity Classes listed

CREATE TABLE "MODULE"
(
"module_id" INT not null primary key,  
"name" VARCHAR(64)
);


CREATE TABLE "FILE"
(
"file_id" INT not null primary key
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),
"file_index" INT not null,
"file_name" VARCHAR(256),
"log_level" INT
);


CREATE TABLE "MODULE_FILE"
(
"module_id" INT not null references MODULE("module_id"),
"file_id" INT not null references FILE("file_id"),
PRIMARY KEY("module_id", "file_id")
);



CREATE TABLE "TRACE"
(
"trace_id" INT not null primary key
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),   
"trace_index" INT not null,
"file_id" INT not null references FILE("file_id"),   
"line" INT,
"format_string" VARCHAR(255)
);


CREATE TABLE "MODULE_TRACE"
(
"module_trace_id" INT not null primary key
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),
"module_id" INT not null references MODULE("module_id"),
"trace_id" INT not null references TRACE("trace_id"),
"timestamp" TIMESTAMP,
"log_level" INT,
"argument_types" INT,
"argument_list" BLOB(250),
"decoded_message" VARCHAR(512)
);

CREATE TABLE "MODULE_TRACE_BUFFER"
(
"module_trace_buffer_id" INT not null primary key
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),
"module_id" INT not null,
"file_index" INT not null,
"trace_index" INT not null,
"ticksSinceTimeSync" BIGINT,
"log_level" INT,
"argument_types" INT,
"argument_list" BLOB(120)
);


INSERT INTO TRACEMONITOR."MODULE" ("module_id", "name") 
	VALUES (1, 'NPP_AFB_FlowConv');

INSERT INTO TRACEMONITOR."MODULE" ("module_id", "name") 
	VALUES (2, 'NPP_AFB_GasQuality');


INSERT INTO TRACEMONITOR."FILE" ("file_index", "file_name", "log_level") 
	VALUES (176, 'src/FlowConv.c', 7);

INSERT INTO TRACEMONITOR."FILE" ("file_index", "file_name", "log_level") 
	VALUES (186, 'src/GasQuality.c', 7);

INSERT INTO TRACEMONITOR.MODULE_FILE ("module_id", "file_id") 
	VALUES (1, 1);

INSERT INTO TRACEMONITOR.MODULE_FILE ("module_id", "file_id") 
	VALUES (2, 2);

INSERT INTO TRACEMONITOR.TRACE ("trace_index", "file_id", "line", "format_string") 
	VALUES (1, 2, 75, 'Parameter changed!');

INSERT INTO TRACEMONITOR.TRACE ("trace_index", "file_id", "line", "format_string") 
	VALUES (2, 2, 106, 'InitializeDOs in GasQuality');

INSERT INTO TRACEMONITOR.TRACE ("trace_index", "file_id", "line", "format_string") 
	VALUES (3, 2, 109, '(%d) %s');