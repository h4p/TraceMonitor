/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.db;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author lu1721
 */
@Entity
@Table(name = "TRACE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Trace.findAll", query = "SELECT t FROM Trace t"),
    @NamedQuery(name = "Trace.findByTraceId", query = "SELECT t FROM Trace t WHERE t.traceId = :traceId"),
    @NamedQuery(name = "Trace.findByTraceIndex", query = "SELECT t FROM Trace t WHERE t.traceIndex = :traceIndex"),
    @NamedQuery(name = "Trace.findByLine", query = "SELECT t FROM Trace t WHERE t.line = :line"),
    @NamedQuery(name = "Trace.findByFormatString", query = "SELECT t FROM Trace t WHERE t.formatString = :formatString")})
public class Trace implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "trace_id")
    private Integer traceId;
    @Basic(optional = false)
    @Column(name = "trace_index")
    private int traceIndex;
    @Column(name = "line")
    private Integer line;
    @Column(name = "format_string")
    private String formatString;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "traceId")
    private Collection<ModuleTrace> moduleTraceCollection;
    @JoinColumn(name = "file_id", referencedColumnName = "file_id")
    @ManyToOne(optional = false)
    private File fileId;

    public Trace() {
    }

    public Trace(Integer traceId) {
        this.traceId = traceId;
    }

    public Trace(Integer traceId, int traceIndex) {
        this.traceId = traceId;
        this.traceIndex = traceIndex;
    }

    public Integer getTraceId() {
        return traceId;
    }

    public void setTraceId(Integer traceId) {
        this.traceId = traceId;
    }

    public int getTraceIndex() {
        return traceIndex;
    }

    public void setTraceIndex(int traceIndex) {
        this.traceIndex = traceIndex;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    @XmlTransient
    public Collection<ModuleTrace> getModuleTraceCollection() {
        return moduleTraceCollection;
    }

    public void setModuleTraceCollection(Collection<ModuleTrace> moduleTraceCollection) {
        this.moduleTraceCollection = moduleTraceCollection;
    }

    public File getFileId() {
        return fileId;
    }

    public void setFileId(File fileId) {
        this.fileId = fileId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (traceId != null ? traceId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Trace)) {
            return false;
        }
        Trace other = (Trace) object;
        if ((this.traceId == null && other.traceId != null) || (this.traceId != null && !this.traceId.equals(other.traceId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elster.nppTraceMonitor.db.Trace[ traceId=" + traceId + " ]";
    }
    
}
