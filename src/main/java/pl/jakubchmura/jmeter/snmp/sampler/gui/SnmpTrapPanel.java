package pl.jakubchmura.jmeter.snmp.sampler.gui;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jakubchmura.jmeter.snmp.sampler.SnmpSampler;
import pl.jakubchmura.jmeter.snmp.sampler.util.SimpleVariableBinding;
import pl.jakubchmura.jmeter.snmp.sampler.util.SnmpVariableType;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SnmpTrapPanel extends JPanel implements ActionListener {

    private static final Logger log = LoggerFactory.getLogger(SnmpTrapPanel.class);

    private static final String ADD_COMMAND = "Add";
    private static final String DELETE_COMMAND = "Delete";

    private static final int COLUMN_OID = 0;
    private static final int COLUMN_VALUE = 1;
    private static final int COLUMN_TYPE = 2;

    private final InnerTableModel tableModel;
    private JTable varbindTable;
    private JButton deleteButton;

    public SnmpTrapPanel() {
        tableModel = new InnerTableModel();
        init();
    }

    public void clearGui() {
        tableModel.clearData();
        deleteButton.setEnabled(false);
    }

    public void modifyTestElement(TestElement element) {
        GuiUtils.stopTableEditing(varbindTable);
        element.setProperty(new CollectionProperty(SnmpSampler.VARBINDS, tableModel.variableBindings));
    }

    public void configure(TestElement element) {
        tableModel.variableBindings.clear();
        JMeterProperty property = element.getProperty(SnmpSampler.VARBINDS);
        if (property instanceof CollectionProperty) {
            CollectionProperty collection = (CollectionProperty) property;
            List<TestElementProperty> testElementProperties = (List<TestElementProperty>) collection.getObjectValue();
            for (TestElementProperty testElementProperty : testElementProperties) {
                SimpleVariableBinding variableBinding = (SimpleVariableBinding) testElementProperty.getElement();
                tableModel.variableBindings.add(variableBinding);
            }
            if (tableModel.getRowCount() != 0) {
                deleteButton.setEnabled(true);
            }
        }
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(makeVarbindsPanel(), BorderLayout.CENTER);
    }

    private JPanel makeVarbindsPanel() {
        varbindTable = new JTable(tableModel);
        JMeterUtils.applyHiDPI(varbindTable);
        varbindTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        varbindTable.setPreferredScrollableViewportSize(new Dimension(100, 150));

        TableColumn typeColumn = varbindTable.getColumnModel().getColumn(COLUMN_TYPE);
        typeColumn.setCellEditor(new TypeCellEditor());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(varbindTable));
        panel.add(makeButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel makeButtonPanel() {
        boolean tableEmpty = tableModel.getRowCount() == 0;

        JButton addButton = createButton("add", 'A', ADD_COMMAND, true);
        deleteButton = createButton("delete", 'D', DELETE_COMMAND, !tableEmpty);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        return buttonPanel;
    }

    private JButton createButton(String resName, char mnemonic, String command, boolean enabled) {
        JButton button = new JButton(JMeterUtils.getResString(resName));
        button.setMnemonic(mnemonic);
        button.setActionCommand(command);
        button.setEnabled(enabled);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals(DELETE_COMMAND)) {
            if (tableModel.getRowCount() > 0) {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                GuiUtils.cancelEditing(varbindTable);

                int rowSelected = varbindTable.getSelectedRow();

                if (rowSelected != -1) {
                    tableModel.removeRow(rowSelected);
                    tableModel.fireTableDataChanged();

                    // Disable the DELETE and SAVE buttons if no rows remaining
                    // after delete.
                    if (tableModel.getRowCount() == 0) {
                        deleteButton.setEnabled(false);
                    }

                    // Table still contains one or more rows, so highlight
                    // (select) the appropriate one.
                    else {
                        int rowToSelect = rowSelected;

                        if (rowSelected >= tableModel.getRowCount()) {
                            rowToSelect = rowSelected - 1;
                        }

                        varbindTable.setRowSelectionInterval(rowToSelect, rowToSelect);
                    }
                }
            }
        } else if (action.equals(ADD_COMMAND)) {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            GuiUtils.stopTableEditing(varbindTable);

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently
            // disabled.
            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            varbindTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
    }

    private static class InnerTableModel extends AbstractTableModel {

        private List<SimpleVariableBinding> variableBindings;

        private InnerTableModel() {
            variableBindings = new ArrayList<>();
        }

        private void addNewRow() {
            variableBindings.add(new SimpleVariableBinding("", "", SnmpVariableType.Counter32));
        }

        private void removeRow(int row) {
            variableBindings.remove(row);
        }

        private void clearData() {
            variableBindings.clear();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return variableBindings.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SimpleVariableBinding varbind = variableBindings.get(rowIndex);

            switch (columnIndex) {
                case COLUMN_OID:
                    return varbind.getOid();
                case COLUMN_VALUE:
                    return varbind.getValue();
                case COLUMN_TYPE:
                    SnmpVariableType type = varbind.getType();
                    return type == null? null: type.name();
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case COLUMN_OID:
                    return "OID";
                case COLUMN_VALUE:
                    return "Value";
                case COLUMN_TYPE:
                    return "Type";
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            SimpleVariableBinding varbind = variableBindings.get(rowIndex);
            log.debug("Setting varbind: " + aValue);
            switch (columnIndex) {
                case COLUMN_OID:
                    varbind.setOid(aValue.toString());
                    break;
                case COLUMN_VALUE:
                    varbind.setValue(aValue.toString());
                    break;
                case COLUMN_TYPE:
                    varbind.setType((SnmpVariableType) aValue);
                    break;
            }
        }
    }

    private static class TypeCellEditor extends DefaultCellEditor {

        TypeCellEditor() {
            super(new JComboBox<>(SnmpVariableType.values()));
        }
    }
}
