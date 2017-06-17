package pl.jakubchmura.jmeter.snmp.sampler;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import pl.jakubchmura.jmeter.snmp.sampler.snmp.SnmpReceiver;
import pl.jakubchmura.jmeter.snmp.sampler.util.CommunicationStyle;
import pl.jakubchmura.jmeter.snmp.sampler.util.SimpleVariableBinding;
import pl.jakubchmura.jmeter.snmp.sampler.util.SnmpVariableType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SnmpSampler extends AbstractSampler implements ThreadListener {

    public static final String COMMUNICATION_STYLE = "SnmpSampler.communicationStyle";
    public static final String DESTINATION_IP = "SnmpSampler.destinationIP";
    public static final String DESTINATION_PORT = "SnmpSampler.destinationPort";
    public static final String LISTENING_IP = "SnmpSampler.listeningIP";
    public static final String LISTENING_PORT = "SnmpSampler.listeningPort";
    public static final String COMMUNITY = "SnmpSampler.community";
    public static final String CORRELATION_OID = "SnmpSampler.correlationOid";
    public static final String TIMEOUT = "SnmpSampler.timeout";
    public static final String VARBINDS = "SnmpSampler.varbinds";

    private static final Logger log = LoggerFactory.getLogger(SnmpSampler.class);

    private CommunicationStyle communicationStyle;
    private OID correlationOid;
    private Snmp snmp;
    private SnmpReceiver snmpReceiver;

    public SampleResult sample(Entry e) {

        SampleResult res = new SampleResult();
        res.setSampleLabel("SNMP Trap - " + communicationStyle);
        res.setSuccessful(false);
        res.sampleStart();
        try {
            PDU pdu = new PDU();
            pdu.setType(PDU.NOTIFICATION);
            pdu.addAll(getVariableBinding());
            log.info("Sending trap: " + pdu);

            CommunityTarget target = new CommunityTarget();
            target.setVersion(SnmpConstants.version2c);
            target.setCommunity(getCommunity());
            target.setAddress(getAddress());

            if (communicationStyle == CommunicationStyle.RequestOnly) {
                snmp.notify(pdu, target);
                res.setResponseOK();
            } else {
                String value = getCorrelationValue(pdu);
                CountDownLatch latch = new CountDownLatch(1);
                snmpReceiver.addLatch(value, latch);
                snmp.notify(pdu, target);
                boolean await = latch.await(getPropertyAsLong(TIMEOUT), TimeUnit.MILLISECONDS);
                if (await) {
                    log.debug("Received matching return trap");
                    res.setResponseOK();
                } else {
                    log.warn("Timeout occurred while waiting for incoming trap with value " + value);
                }
            }
        } catch (Exception ex) {
            log.warn("", ex);
            res.setResponseMessage(ex.getLocalizedMessage());
        }
        res.sampleEnd();

        return res;
    }

    private List<VariableBinding> getVariableBinding() {
        List<VariableBinding> list = new ArrayList<>();
        JMeterProperty property = getProperty(SnmpSampler.VARBINDS);
        if (property instanceof CollectionProperty) {
            CollectionProperty collection = (CollectionProperty) property;
            List<TestElementProperty> testElementProperties = (List<TestElementProperty>) collection.getObjectValue();
            for (TestElementProperty testElementProperty : testElementProperties) {
                SimpleVariableBinding simple = (SimpleVariableBinding) testElementProperty.getElement();
                OID oid = new OID(simple.getOid());
                String value = simple.getValue();
                SnmpVariableType type = simple.getType();
                list.add(new VariableBinding(oid, type.createVariable(value)));
            }
        }
        return list;
    }

    private Address getAddress() throws UnknownHostException {
        InetAddress host = InetAddress.getByName(getPropertyAsString(DESTINATION_IP));
        int port = getPropertyAsInt(DESTINATION_PORT);
        return new UdpAddress(host, port);
    }

    private OctetString getCommunity() {
        return new OctetString(getPropertyAsString(COMMUNITY));
    }

    private CommunicationStyle getCommunicationStyle() {
        return CommunicationStyle.fromName(getPropertyAsString(COMMUNICATION_STYLE));
    }

    private Address getListeningAddress() throws UnknownHostException {
        InetAddress host = InetAddress.getByName(getPropertyAsString(LISTENING_IP));
        int port = getPropertyAsInt(LISTENING_PORT);
        return new UdpAddress(host, port);
    }

    private OID getCorrelationOid() {
        return new OID(getPropertyAsString(CORRELATION_OID));
    }

    private String getCorrelationValue(PDU pdu) {
        Variable variable = pdu.getVariable(correlationOid);
        return variable.toString();
    }

    @Override
    public void threadStarted() {
        log.info("Thread started");
        communicationStyle = getCommunicationStyle();
        try {
            snmp = new Snmp(new DefaultUdpTransportMapping());
        } catch (IOException e) {
            log.error("Open SNMP Sender", e);
        }
        if (communicationStyle == CommunicationStyle.RequestResponse) {
            correlationOid = getCorrelationOid();
            try {
                snmpReceiver = SnmpReceiver.getInstance(getListeningAddress(), correlationOid);
            } catch (IOException e) {
                log.error("Open SNMP Listener", e);
            }
        }
    }

    @Override
    public void threadFinished() {
        log.info("Thread finished");
        if (snmp != null) {
            try {
                snmp.close();
            } catch (IOException e) {
                log.warn("Close SNMP", e);
            }
        }
    }
}
