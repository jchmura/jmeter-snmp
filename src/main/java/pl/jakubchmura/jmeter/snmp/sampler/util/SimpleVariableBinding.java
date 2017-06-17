package pl.jakubchmura.jmeter.snmp.sampler.util;

import org.apache.jmeter.testelement.AbstractTestElement;

public class SimpleVariableBinding extends AbstractTestElement {

    private static final String PROP_OID = "SimpleVariableBinding.oid";
    private static final String PROP_VALUE = "SimpleVariableBinding.value";
    private static final String PROP_TYPE = "SimpleVariableBinding.type";

    public SimpleVariableBinding() {
    }

    public SimpleVariableBinding(String oid, String value, SnmpVariableType type) {
        setOid(oid);
        setValue(value);
        setType(type);
    }

    public String getOid() {
        return getPropertyAsString(PROP_OID);
    }

    public void setOid(String oid) {
        setProperty(PROP_OID, oid);
    }

    public String getValue() {
        return getPropertyAsString(PROP_VALUE);
    }

    public void setValue(String value) {
        setProperty(PROP_VALUE, value);
    }

    public SnmpVariableType getType() {
        String type = getPropertyAsString(PROP_TYPE);
        return SnmpVariableType.valueOf(type);
    }

    public void setType(SnmpVariableType type) {
        setProperty(PROP_TYPE, type.name());
    }

    @Override
    public String getName() {
        return super.getName();
    }
}
