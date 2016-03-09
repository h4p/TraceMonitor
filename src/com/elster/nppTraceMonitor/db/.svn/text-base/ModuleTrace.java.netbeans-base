/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.db;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author lu1721
 */
@Entity
@Table(name = "MODULE_TRACE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ModuleTrace.findAll", query = "SELECT m FROM ModuleTrace m"),
    @NamedQuery(name = "ModuleTrace.findByModuleTraceId", query = "SELECT m FROM ModuleTrace m WHERE m.moduleTraceId = :moduleTraceId"),
    @NamedQuery(name = "ModuleTrace.findByTimestamp", query = "SELECT m FROM ModuleTrace m WHERE m.timestamp = :timestamp"),
    @NamedQuery(name = "ModuleTrace.findByLogLevel", query = "SELECT m FROM ModuleTrace m WHERE m.logLevel = :logLevel"),
    @NamedQuery(name = "ModuleTrace.findByArgumentTypes", query = "SELECT m FROM ModuleTrace m WHERE m.argumentTypes = :argumentTypes"),
    @NamedQuery(name = "ModuleTrace.findByDecodedMessage", query = "SELECT m FROM ModuleTrace m WHERE m.decodedMessage = :decodedMessage")})
public class ModuleTrace implements Serializable {
    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "module_trace_id")
    private Integer moduleTraceId;
    @Column(name = "timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    @Column(name = "log_level")
    private Integer logLevel;
    @Column(name = "argument_types")
    private Integer argumentTypes;
    @Lob
    @Column(name = "argument_list")
    private Serializable argumentList;
    @Column(name = "decoded_message")
    private String decodedMessage;
    @JoinColumn(name = "trace_id", referencedColumnName = "trace_id")
    @ManyToOne(optional = false)
    private Trace traceId;
    @JoinColumn(name = "module_id", referencedColumnName = "module_id")
    @ManyToOne(optional = false)
    private Module moduleId;

    public ModuleTrace() {
    }

    public ModuleTrace(Integer moduleTraceId) {
        this.moduleTraceId = moduleTraceId;
    }

    public Integer getModuleTraceId() {
        return moduleTraceId;
    }

    public void setModuleTraceId(Integer moduleTraceId) {
        Integer oldModuleTraceId = this.moduleTraceId;
        this.moduleTraceId = moduleTraceId;
        changeSupport.firePropertyChange("moduleTraceId", oldModuleTraceId, moduleTraceId);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        Date oldTimestamp = this.timestamp;
        this.timestamp = timestamp;
        changeSupport.firePropertyChange("timestamp", oldTimestamp, timestamp);
    }

    public Integer getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Integer logLevel) {
        Integer oldLogLevel = this.logLevel;
        this.logLevel = logLevel;
        changeSupport.firePropertyChange("logLevel", oldLogLevel, logLevel);
    }

    public Integer getArgumentTypes() {
        return argumentTypes;
    }

    public void setArgumentTypes(Integer argumentTypes) {
        Integer oldArgumentTypes = this.argumentTypes;
        this.argumentTypes = argumentTypes;
        changeSupport.firePropertyChange("argumentTypes", oldArgumentTypes, argumentTypes);
    }

    public Serializable getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(Serializable argumentList) {
        Serializable oldArgumentList = this.argumentList;
        this.argumentList = argumentList;
        changeSupport.firePropertyChange("argumentList", oldArgumentList, argumentList);
    }

    public String getDecodedMessage() {
        return decodedMessage;
    }

    public void setDecodedMessage(String decodedMessage) {
        String oldDecodedMessage = this.decodedMessage;
        this.decodedMessage = decodedMessage;
        changeSupport.firePropertyChange("decodedMessage", oldDecodedMessage, decodedMessage);
    }

    public Trace getTraceId() {
        return traceId;
    }

    public void setTraceId(Trace traceId) {
        Trace oldTraceId = this.traceId;
        this.traceId = traceId;
        changeSupport.firePropertyChange("traceId", oldTraceId, traceId);
    }

    public Module getModuleId() {
        return moduleId;
    }

    public void setModuleId(Module moduleId) {
        Module oldModuleId = this.moduleId;
        this.moduleId = moduleId;
        changeSupport.firePropertyChange("moduleId", oldModuleId, moduleId);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (moduleTraceId != null ? moduleTraceId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ModuleTrace)) {
            return false;
        }
        ModuleTrace other = (ModuleTrace) object;
        if ((this.moduleTraceId == null && other.moduleTraceId != null) || (this.moduleTraceId != null && !this.moduleTraceId.equals(other.moduleTraceId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elster.nppTraceMonitor.db.ModuleTrace[ moduleTraceId=" + moduleTraceId + " ]";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    
}
