package pl.jakubchmura.jmeter.snmp.sampler.gui;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import pl.jakubchmura.jmeter.snmp.sampler.SnmpSampler;
import pl.jakubchmura.jmeter.snmp.sampler.util.CommunicationStyle;

import javax.swing.*;
import java.awt.*;

import static pl.jakubchmura.jmeter.snmp.sampler.SnmpSampler.*;

public class SnmpSamplerGui extends AbstractSamplerGui {

    private final JComboBox<String> communicationStyleCombo = new JComboBox<>(CommunicationStyle.getNames());
    private final JTextField destinationIpField = new JTextField();
    private final JTextField destinationPortField = new JTextField();
    private final JTextField listeningIpField = new JTextField();
    private final JTextField listeningPortField = new JTextField();
    private final JTextField correlationOid = new JTextField();
    private final JTextField timeoutField = new JTextField();
    private final JTextField communityField = new JTextField();
    private final SnmpTrapPanel snmpTrapPanel = new SnmpTrapPanel();

    public SnmpSamplerGui() {
        initGui();
    }

    private void initGui() {
        namePanel.setName(getStaticLabel());

        setLayout(new BorderLayout());
        setBorder(makeBorder());
        Box header = Box.createVerticalBox();
        header.add(makeTitlePanel());
        add(header, BorderLayout.NORTH);

        Box body = Box.createHorizontalBox();
        body.add(makeBodyPanel());
        add(body, BorderLayout.CENTER);

    }

    private Component makeBodyPanel() {
        VerticalPanel bodyPanel = new VerticalPanel();
        bodyPanel.add(makeConnectionPanel());
        bodyPanel.add(makeTrapPanel());
        return bodyPanel;
    }

    private Component makeConnectionPanel() {
        VerticalPanel connectionPanel = new VerticalPanel();
        connectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Connection"));

        connectionPanel.add(makeLabeledPanel("Communication style", communicationStyleCombo));
        connectionPanel.add(makeLabeledPanel("Destination IP", destinationIpField));
        connectionPanel.add(makeLabeledPanel("Destination port", destinationPortField));
        connectionPanel.add(makeLabeledPanel("Listening IP", listeningIpField));
        connectionPanel.add(makeLabeledPanel("Listening port", listeningPortField));
        connectionPanel.add(makeLabeledPanel("Correlation OID", correlationOid));
        connectionPanel.add(makeLabeledPanel("Timeout [ms]", timeoutField));

        communicationStyleCombo.addActionListener(e -> {
            String selectedItem = (String) communicationStyleCombo.getSelectedItem();
            CommunicationStyle style = CommunicationStyle.fromName(selectedItem);
            boolean listening = CommunicationStyle.RequestResponse == style;
            listeningIpField.setEnabled(listening);
            listeningPortField.setEnabled(listening);
            correlationOid.setEnabled(listening);
            timeoutField.setEnabled(listening);
        });
        communicationStyleCombo.setSelectedIndex(0);

        return connectionPanel;
    }

    private Component makeTrapPanel() {
        VerticalPanel trapPanel = new VerticalPanel();
        trapPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Notification"));
        trapPanel.add(makeLabeledPanel("Community", communityField));
        trapPanel.add(snmpTrapPanel);
        return trapPanel;
    }

    private Component makeLabeledPanel(String label, Component component) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel jLabel = new JLabel(label);
        panel.add(jLabel, BorderLayout.WEST);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "SNMP Sampler";
    }

    public TestElement createTestElement() {
        SnmpSampler sampler = new SnmpSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        element.setProperty(COMMUNICATION_STYLE, (String) communicationStyleCombo.getSelectedItem());
        element.setProperty(DESTINATION_IP, destinationIpField.getText());
        element.setProperty(DESTINATION_PORT, destinationPortField.getText());
        element.setProperty(LISTENING_IP, listeningIpField.getText());
        element.setProperty(LISTENING_PORT, listeningPortField.getText());
        element.setProperty(CORRELATION_OID, correlationOid.getText());
        element.setProperty(TIMEOUT, timeoutField.getText());
        element.setProperty(COMMUNITY, communityField.getText());
        snmpTrapPanel.modifyTestElement(element);
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        communicationStyleCombo.setSelectedItem(element.getPropertyAsString(COMMUNICATION_STYLE));
        destinationIpField.setText(element.getPropertyAsString(DESTINATION_IP));
        destinationPortField.setText(element.getPropertyAsString(DESTINATION_PORT));
        listeningIpField.setText(element.getPropertyAsString(LISTENING_IP));
        listeningPortField.setText(element.getPropertyAsString(LISTENING_PORT));
        correlationOid.setText(element.getPropertyAsString(CORRELATION_OID));
        timeoutField.setText(element.getPropertyAsString(TIMEOUT));
        communityField.setText(element.getPropertyAsString(COMMUNITY));
        snmpTrapPanel.configure(element);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        communicationStyleCombo.setSelectedIndex(0);
        destinationIpField.setText("");
        destinationPortField.setText("");
        listeningIpField.setText("");
        listeningPortField.setText("");
        communityField.setText("");
        snmpTrapPanel.clearGui();
    }
}
