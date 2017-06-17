package pl.jakubchmura.jmeter.snmp.sampler.util;

import org.snmp4j.smi.*;

public enum SnmpVariableType {

    Counter32() {
        @Override
        public Variable createVariable(String text) {
            return new Counter32(Long.parseLong(text));
        }
    },
    Counter64() {
        @Override
        public Variable createVariable(String text) {
            return new Counter64(Long.parseLong(text));
        }
    },
    Gauge32() {
        @Override
        public Variable createVariable(String text) {
            return new Gauge32(Long.parseLong(text));
        }
    },
    Integer32() {
        @Override
        public Variable createVariable(String text) {
            return new Integer32(Integer.parseInt(text));
        }
    },
    IpAddress() {
        @Override
        public Variable createVariable(String text) {
            return new IpAddress(text);
        }
    },
    Null() {
        @Override
        public Variable createVariable(String text) {
            return new Null();
        }
    },
    OctetString() {
        @Override
        public Variable createVariable(String text) {
            return new OctetString(text);
        }
    },
    OID() {
        @Override
        public Variable createVariable(String text) {
            return new OID(text);
        }
    },
    Opaque() {
        @Override
        public Variable createVariable(String text) {
            return new Opaque(text.getBytes());
        }
    },
    TimeTicks() {
        @Override
        public Variable createVariable(String text) {
            return new TimeTicks(Long.parseLong(text));
        }
    };

    public abstract Variable createVariable(String text);


}
