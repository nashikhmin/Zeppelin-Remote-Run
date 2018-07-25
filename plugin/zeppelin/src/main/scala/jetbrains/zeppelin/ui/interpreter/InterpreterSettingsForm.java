package jetbrains.zeppelin.ui.interpreter;

import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import jetbrains.zeppelin.api.InterpreterOption;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.intellij.ui.components.JBList.createDefaultListModel;

public class InterpreterSettingsForm extends JDialog {
    final static private String GLOBALLY = "Globally";
    final static private String CUSTOM = "Custom";


    private JPanel contentPane;
    private JBList<String> dependenciesList;
    @SuppressWarnings("unused")
    private JPanel dependenciesPanel;

    private JComboBox<String> instantiationBox;
    private JComboBox<String> perNoteBox;
    private JComboBox<String> perUserBox;
    private JPanel customInstantiationPanel;

    private CopyOnWriteArrayList<String> modelList;

    public InterpreterSettingsForm() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    public List<String> getModelList() {
        return modelList;
    }

    public String getPerNoteValue() {
        return (String) perNoteBox.getSelectedItem();
    }

    public String getPerUserValue() {
        return (String) perUserBox.getSelectedItem();
    }

    public void initDataModel(List<String> list) {
        modelList = new CopyOnWriteArrayList<>(list);
        updateModelList();
    }

    public void initInstantiationTypes(List<String> values, InterpreterOption option) {
        ArrayList<String> instantiationValues = new ArrayList<>();
        instantiationValues.add(GLOBALLY);
        instantiationValues.add(CUSTOM);

        initComboBox(instantiationBox, instantiationValues, option.isGlobally() ? GLOBALLY : CUSTOM);
        customInstantiationPanel.setVisible(!option.isGlobally());
        instantiationBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                boolean isGlobal = event.getItem().equals(GLOBALLY);
                customInstantiationPanel.setVisible(!isGlobal);
            }
        });

        initComboBox(perUserBox, values, option.perUserAsString());
        initComboBox(perNoteBox, values, option.perNoteAsString());
    }

    public boolean isGlobally() {
        return Objects.equals(instantiationBox.getSelectedItem(), GLOBALLY);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(6, 5, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.add(dependenciesPanel, new GridConstraints(4, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Interpreter dependencies:");
        contentPane.add(label1, new GridConstraints(2, 0, 2, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Instantiation:");
        contentPane.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        instantiationBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Globally");
        defaultComboBoxModel1.addElement("Per note (scoped)");
        defaultComboBoxModel1.addElement("Per note (isolated)");
        instantiationBox.setModel(defaultComboBoxModel1);
        contentPane.add(instantiationBox, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        customInstantiationPanel = new JPanel();
        customInstantiationPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(customInstantiationPanel, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Per Note:");
        customInstantiationPanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        perNoteBox = new JComboBox();
        customInstantiationPanel.add(perNoteBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Per User:");
        customInstantiationPanel.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        perUserBox = new JComboBox();
        customInstantiationPanel.add(perUserBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void createUIComponents() {
        dependenciesList = new JBList<>();
        dependenciesList.setEmptyText("There aren't dependencies");

        dependenciesPanel = ToolbarDecorator.createDecorator(dependenciesList).setAddAction(anActionButton -> {
            String newValue = new AddDependencyButton(contentPane).getValue();
            modelList.add(newValue);
            updateModelList();
        }).setRemoveAction(anActionButton -> {
            int selectedIndex = dependenciesList.getSelectedIndex();
            modelList.remove(selectedIndex);
            updateModelList();
        }).createPanel();
    }

    private void initComboBox(JComboBox<String> comboBox, List<String> values, String defaultValue) {
        String[] array = values.toArray(new String[0]);
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(array);
        model.setSelectedItem(defaultValue);
        comboBox.setModel(model);
    }

    private void updateModelList() {
        String[] array = modelList.toArray(new String[0]);
        final DefaultListModel<String> model = createDefaultListModel(array);
        dependenciesList.setModel(model);
    }
}