package pl.jakubchmura.jmeter.snmp.sampler.snmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SnmpReceiver implements CommandResponder {

    private static volatile SnmpReceiver instance;
    private static final Object lock = new Object();

    private static final Logger log = LoggerFactory.getLogger(SnmpReceiver.class);

    private final Map<String, CountDownLatch> latches = Collections.synchronizedMap(new HashMap<>());
    private final Snmp snmp;
    private final OID correlationOid;

    private SnmpReceiver(Address listenAddress, OID correlationOid) throws IOException {
        this.snmp = new Snmp(new DefaultUdpTransportMapping());
        boolean b = this.snmp.addNotificationListener(listenAddress, this);
        log.debug("Adding notification listener: " + b);
        this.correlationOid = correlationOid;
    }

    public static SnmpReceiver getInstance(Address listenAddress, OID correlationOid) throws IOException {
        SnmpReceiver r = instance;
        if (r == null) {
            synchronized (lock) {
                r = instance;
                if (r == null) {
                    r = new SnmpReceiver(listenAddress, correlationOid);
                    instance = r;
                }
            }
        }
        return r;
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        log.debug("Received event: " + event);
        PDU pdu = event.getPDU();
        String value = getCorrelationValue(pdu);
        CountDownLatch countDownLatch = latches.remove(value);
        if (countDownLatch == null) {
            log.warn("Received a trap for which there is no waiting sample");
        } else {
            countDownLatch.countDown();
        }
    }

    public synchronized void addLatch(String value, CountDownLatch latch) {
        latches.put(value, latch);
    }

    private String getCorrelationValue(PDU pdu) {
        Variable variable = pdu.getVariable(correlationOid);
        return variable.toString();
    }
}
