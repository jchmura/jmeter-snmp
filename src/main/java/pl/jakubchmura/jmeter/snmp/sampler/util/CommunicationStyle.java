package pl.jakubchmura.jmeter.snmp.sampler.util;

public enum CommunicationStyle {

    RequestOnly("Request Only"),
    RequestResponse("Request Response");

    private final String name;

    CommunicationStyle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String[] getNames() {
        CommunicationStyle[] values = CommunicationStyle.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].getName();
        }
        return names;
    }

    public static CommunicationStyle fromName(String name) {
        for (CommunicationStyle communicationStyle : values()) {
            if (communicationStyle.getName().equals(name)) {
                return communicationStyle;
            }
        }
        throw new IllegalArgumentException("Unknown communication style: " + name);
    }
}
