package org.project.openbaton.nfvo.catalogue.nfvo;

import org.project.openbaton.nfvo.catalogue.util.IdGenerator;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Created by lto on 01/07/15.
 */
@Entity
public class EventEndpoint implements Serializable{
    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    private String name;
    private String networkServiceId;
    private String virtualNetworkFunctionId;
    private EndpointType type;
    private String endpoint;
    private Action event;

    public String getNetworkServiceId() {
        return networkServiceId;
    }

    public void setNetworkServiceId(String networkServiceId) {
        this.networkServiceId = networkServiceId;
    }

    public String getVirtualNetworkFunctionId() {
        return virtualNetworkFunctionId;
    }

    public void setVirtualNetworkFunctionId(String virtualNetworkFunctionId) {
        this.virtualNetworkFunctionId = virtualNetworkFunctionId;
    }

    public Action getEvent() {
        return event;
    }

    public void setEvent(Action event) {
        this.event = event;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EndpointType getType() {
        return type;
    }

    public void setType(EndpointType type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "EventEndpoint{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", networkServiceId='" + networkServiceId + '\'' +
                ", virtualNetworkFunctionId='" + virtualNetworkFunctionId + '\'' +
                ", type=" + type +
                ", endpoint='" + endpoint + '\'' +
                ", event=" + event +
                '}';
    }

}