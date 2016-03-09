/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.nppTraceMonitor.net;

/**
 * A class that bundles attributes to
 * easily store a command in a queue
 * 
 * @author wesselst
 */
public class TMCommand {
    
    private byte commandId;
    private int length;
    private byte[] dataField;

    public TMCommand(byte commandId, int length, byte[] dataField) {
        this.commandId = commandId;
        this.length = length;
        this.dataField = dataField;
    }

    
    public byte getCommandId() {
        return commandId;
    }

    public void setCommandId(byte commandId) {
        this.commandId = commandId;
    }

    public byte[] getDataField() {
        return dataField;
    }

    public void setDataField(byte[] dataField) {
        this.dataField = dataField;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
    
    
    
}
