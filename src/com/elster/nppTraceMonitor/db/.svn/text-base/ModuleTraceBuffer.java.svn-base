/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.db;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author lu1721
 */
@Entity
@Table (name = "MODULE_TRACE_BUFFER")
@XmlRootElement
@NamedQueries (
{
    @NamedQuery (name = "ModuleTraceBuffer.findAll", query = "SELECT m FROM ModuleTraceBuffer m"),
    @NamedQuery (name = "ModuleTraceBuffer.findByModuleTraceBufferId", query = "SELECT m FROM ModuleTraceBuffer m WHERE m.moduleTraceBufferId = :moduleTraceBufferId"),
    @NamedQuery (name = "ModuleTraceBuffer.findByModuleId", query = "SELECT m FROM ModuleTraceBuffer m WHERE m.moduleId = :moduleId"),
    @NamedQuery (name = "ModuleTraceBuffer.findByFileIndex", query = "SELECT m FROM ModuleTraceBuffer m WHERE m.fileIndex = :fileIndex"),
    @NamedQuery (name = "ModuleTraceBuffer.findByTraceIndex", query = "SELECT m FROM ModuleTraceBuffer m WHERE m.traceIndex = :traceIndex"),
    @NamedQuery (name = "ModuleTraceBuffer.findByTicksSinceTimeSync", query = "SELECT m FROM ModuleTraceBuffer m WHERE m.ticksSinceTimeSync = :ticksSinceTimeSync"),
    @NamedQuery (name = "ModuleTraceBuffer.findByLogLevel", query = "SELECT m FROM ModuleTraceBuffer m WHERE m.logLevel = :logLevel"),
    @NamedQuery (name = "ModuleTraceBuffer.findByArgumentTypes", query = "SELECT m FROM ModuleTraceBuffer m WHERE m.argumentTypes = :argumentTypes")
})
public class ModuleTraceBuffer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Basic (optional = false)
    @Column (name = "module_trace_buffer_id")
    private Integer moduleTraceBufferId;
    @Basic (optional = false)
    @Column (name = "module_id")
    private int moduleId;
    @Basic (optional = false)
    @Column (name = "file_index")
    private int fileIndex;
    @Basic (optional = false)
    @Column (name = "trace_index")
    private int traceIndex;
    @Column (name = "ticksSinceTimeSync")
    private BigInteger ticksSinceTimeSync;
    @Column (name = "log_level")
    private Integer logLevel;
    @Column (name = "argument_types")
    private Integer argumentTypes;
    @Lob
    @Column (name = "argument_list")
    private Serializable argumentList;

    public ModuleTraceBuffer () {
    }

    public ModuleTraceBuffer (Integer moduleTraceBufferId) {
        this.moduleTraceBufferId = moduleTraceBufferId;
    }

    public ModuleTraceBuffer (Integer moduleTraceBufferId, int moduleId, int fileIndex, int traceIndex) {
        this.moduleTraceBufferId = moduleTraceBufferId;
        this.moduleId = moduleId;
        this.fileIndex = fileIndex;
        this.traceIndex = traceIndex;
    }

    public Integer getModuleTraceBufferId () {
        return moduleTraceBufferId;
    }

    public void setModuleTraceBufferId (Integer moduleTraceBufferId) {
        this.moduleTraceBufferId = moduleTraceBufferId;
    }

    public int getModuleId () {
        return moduleId;
    }

    public void setModuleId (int moduleId) {
        this.moduleId = moduleId;
    }

    public int getFileIndex () {
        return fileIndex;
    }

    public void setFileIndex (int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public int getTraceIndex () {
        return traceIndex;
    }

    public void setTraceIndex (int traceIndex) {
        this.traceIndex = traceIndex;
    }

    public BigInteger getTicksSinceTimeSync () {
        return ticksSinceTimeSync;
    }

    public void setTicksSinceTimeSync (BigInteger ticksSinceTimeSync) {
        this.ticksSinceTimeSync = ticksSinceTimeSync;
    }

    public Integer getLogLevel () {
        return logLevel;
    }

    public void setLogLevel (Integer logLevel) {
        this.logLevel = logLevel;
    }

    public Integer getArgumentTypes () {
        return argumentTypes;
    }

    public void setArgumentTypes (Integer argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    public Serializable getArgumentList () {
        return argumentList;
    }

    public void setArgumentList (Serializable argumentList) {
        this.argumentList = argumentList;
    }

    @Override
    public int hashCode () {
        int hash = 0;
        hash += (moduleTraceBufferId != null ? moduleTraceBufferId.hashCode () : 0);
        return hash;
    }

    @Override
    public boolean equals (Object object) {
        if (!(object instanceof ModuleTraceBuffer))
        {
            return false;
        }
        ModuleTraceBuffer other = (ModuleTraceBuffer) object;
        if ((this.moduleTraceBufferId == null && other.moduleTraceBufferId != null) || (this.moduleTraceBufferId != null && !this.moduleTraceBufferId.equals (other.moduleTraceBufferId)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString () {
        return "com.elster.nppTraceMonitor.db.ModuleTraceBuffer[ moduleTraceBufferId=" + moduleTraceBufferId + " ]";
    }
    
}
