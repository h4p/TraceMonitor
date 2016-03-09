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
@Table(name = "MODULE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Module.findAll", query = "SELECT m FROM Module m"),
    @NamedQuery(name = "Module.findByModuleId", query = "SELECT m FROM Module m WHERE m.moduleId = :moduleId"),
    @NamedQuery(name = "Module.findByName", query = "SELECT m FROM Module m WHERE m.name = :name")})
public class Module implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "module_id")
    private Integer moduleId;
    @Column(name = "name")
    private String name;
    @ManyToMany(mappedBy = "moduleCollection")
    private Collection<File> fileCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "moduleId")
    private Collection<ModuleTrace> moduleTraceCollection;

    public Module() {
    }

    public Module(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public Collection<File> getFileCollection() {
        return fileCollection;
    }

    public void setFileCollection(Collection<File> fileCollection) {
        this.fileCollection = fileCollection;
    }

    @XmlTransient
    public Collection<ModuleTrace> getModuleTraceCollection() {
        return moduleTraceCollection;
    }

    public void setModuleTraceCollection(Collection<ModuleTrace> moduleTraceCollection) {
        this.moduleTraceCollection = moduleTraceCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (moduleId != null ? moduleId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Module)) {
            return false;
        }
        Module other = (Module) object;
        if ((this.moduleId == null && other.moduleId != null) || (this.moduleId != null && !this.moduleId.equals(other.moduleId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elster.nppTraceMonitor.db.Module[ moduleId=" + moduleId + " ]";
    }
    
}
