package org.example.jmeter.plugins;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;


/**
 * Simple GUI for the TPS Calculator Config Element.
 * Appears under Add → Config Element → TPS Calculator.
 */
public class TPSCalculatorGui extends AbstractConfigGui {

    private static final long serialVersionUID = 1L;

    private JTextField targetTpsField;
    private JTextField expectedRespMsField;
    private JTextField desiredThinkMsField;
    private JTextField fixedUsersField;
    private JTextField txPerIterField;

    public TPSCalculatorGui() { init(); }

    @Override
    public String getLabelResource() { return "TPS Calculator"; }

    @Override
    public String getStaticLabel() { return "TPS Calculator"; }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);

        int y = 0;
        addRow(grid, gbc, y++, "Target TPS (req/sec):", targetTpsField = new JTextField(10));
        addRow(grid, gbc, y++, "Expected response (ms):", expectedRespMsField = new JTextField(10));
        addRow(grid, gbc, y++, "Desired think time (ms) [optional]:", desiredThinkMsField = new JTextField(10));
        addRow(grid, gbc, y++, "Fixed users (threads) [optional]:", fixedUsersField = new JTextField(10));
        addRow(grid, gbc, y++, "Transactions per iteration (k):", txPerIterField = new JTextField(10));

        mainPanel.add(grid);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void addRow(JPanel grid, GridBagConstraints gbc, int y, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE;
        grid.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(field, gbc);
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        TPSCalculatorConfig cfg = (TPSCalculatorConfig) element;
        targetTpsField.setText(cfg.getTargetTPS());
        expectedRespMsField.setText(cfg.getExpectedResponseMs());
        desiredThinkMsField.setText(cfg.getDesiredThinkMs());
        fixedUsersField.setText(cfg.getFixedUsers());
        txPerIterField.setText(cfg.getTransactionsPerIteration());
    }

    @Override
    public TestElement createTestElement() {
        TPSCalculatorConfig cfg = new TPSCalculatorConfig();
        modifyTestElement(cfg);
        return cfg;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        TPSCalculatorConfig cfg = (TPSCalculatorConfig) element;
        cfg.setTargetTPS(targetTpsField.getText());
        cfg.setExpectedResponseMs(expectedRespMsField.getText());
        cfg.setDesiredThinkMs(desiredThinkMsField.getText());
        cfg.setFixedUsers(fixedUsersField.getText());
        cfg.setTransactionsPerIteration(txPerIterField.getText());
    }

    @Override
    public void clearGui() {
        super.clearGui();
        targetTpsField.setText("10");
        expectedRespMsField.setText("200");
        desiredThinkMsField.setText("");
        fixedUsersField.setText("");
        txPerIterField.setText("1");
    }
}
