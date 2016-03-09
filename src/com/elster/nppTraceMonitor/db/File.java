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
@Table(name = "FILE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "File.findAll", query = "SELECT f FROM File f"),
    @NamedQuery(name = "File.findByFileId", query = "SELECT f FROM File f WHERE f.fileId = :fileId"),
    @NamedQuery(name = "File.findByFileIndex", query = "SELECT f FROM File f WHERE f.fileIndex = :fileIndex"),
    @NamedQuery(name = "File.findByFileName", query = "SELECT f FROM File f WHERE f.fileName = :fileName"),
    @NamedQuery(name = "File.findByLogLevel", query = "SELECT f FROM File f WHERE f.logLevel = :logLevel")})
public class File implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "file_id")
    private Integer fileId;
    @Basic(optional = false)
    @Column(name = "file_index")
    private int fileIndex;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "log_level")
    private Integer logLevel;
    @JoinTable(name = "MODULE_FILE", joinColumns = {
        @JoinColumn(name = "file_id", referencedColumnName = "file_id")}, inverseJoinColumns = {
        @JoinColumn(name = "module_id", referencedColumnName = "module_id")})
    @ManyToMany
    private Collection<Module> moduleCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fileId")
    private Collection<Trace> traceCollection;

    public File() {
    }

    public File(Integer fileId) {
        this.fileId = fileId;
    }

    public File(Integer fileId, int fileIndex) {
        this.fileId = fileId;
        this.fileIndex = fileIndex;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public int getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }

    @XmlTransient
    public Collection<Module> getModuleCollection() {
        return moduleCollection;
    }

    public void setModuleCollection(Collection<Module> moduleCollection) {
        this.moduleCollection = moduleCollection;
    }

    @XmlTransient
    public Collection<Trace> getTraceCollection() {
        return traceCollection;
    }

    public void setTraceCollection(Collection<Trace> traceCollection) {
        this.traceCollection = traceCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fileId != null ? fileId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof File)) {
            return false;
        }
        File other = (File) object;
        if ((this.fileId == null && other.fileId != null) || (this.fileId != null && !this.fileId.equals(other.fileId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elster.nppTraceMonitor.db.File[ fileId=" + fileId + " ]";
    }
    
}
