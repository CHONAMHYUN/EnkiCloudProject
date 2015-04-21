package docker.Utility;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by nhcho on 2015-04-16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ResourceData {
    @JsonProperty("cpu")
    private Cpu cpu;

    @JsonProperty("memory")
    private Memory memory;



    @JsonIgnoreProperties(ignoreUnknown=true)
    private class Cpu {
        @JsonProperty("user")
        String user;
        @JsonProperty("system")
        String system;
        @JsonProperty("idle")
        String idle;
        @JsonProperty("wait")
        String wait;
        @JsonProperty("stolen")
        String stolen;

        public String getUser() {return user;}
        public String getSystem() {return system;}
        public String getIdle() {return idle;}
        public String getWait() {return wait;}
        public String getStolen() {return stolen;}

        public void setUser(String user) {this.user = user;}
        public void setSystem(String system) {this.system = system;}
        public void setIdle(String idle) {this.user = idle;}
        public void setWait(String wait) {this.user = wait;}
        public void setStolen(String stolen) {this.user = stolen;}

        @Override
        public String toString() {
            return "CPU USAGE : user=" + user + ",system=" + system;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    private class Memory {
        @JsonProperty("swpd")
        String swpd;
        @JsonProperty("free")
        String free;
        @JsonProperty("used")
        String used;
        @JsonProperty("cache")
        String cache;

        public String getSwpd() {return swpd;}
        public String getFree() {return free;}
        public String getUsed() {return used;}
        public String getCache() {return cache;}

        public void setSwpd(String swpd) {this.swpd = swpd;}
        public void setFree(String free) {this.free = free;}
        public void setUsed(String used) {this.used = used;}
        public void setCache(String cache) {this.cache = cache;}

    }
}
