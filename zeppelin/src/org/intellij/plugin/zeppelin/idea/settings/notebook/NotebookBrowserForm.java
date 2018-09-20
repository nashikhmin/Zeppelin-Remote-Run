package org.intellij.plugin.zeppelin.idea.settings.notebook;

import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.lang.ArrayUtils;
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants;
import org.intellij.plugin.zeppelin.idea.common.AddStringValueButton;
import org.intellij.plugin.zeppelin.models.Notebook;
import org.intellij.plugin.zeppelin.models.ZeppelinModelFactory;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.intellij.ui.components.JBList.createDefaultListModel;

public class NotebookBrowserForm extends JDialog {
    private JPanel notebookPanel;
    private JPanel contentPane;
    private List<Notebook> notebooks = new ArrayList<>();
    private JBList<String> notebookList;

    public NotebookBrowserForm() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    public List<Notebook> getNotebooks() {
        return notebooks;
    }

    public void setNotebooks(List<Notebook> notebooks) {
        int i = notebookList.getSelectedIndex();
        this.notebooks = notebooks;
        if (i >= notebooks.size())
            i = -1;
        updateModelList();
        notebookList.setSelectedIndex(i);
    }

    @Nullable
    public Notebook getSelectedValue() {
        int index = notebookList.getSelectedIndex();
        if (index<0) return null;
        return notebooks.get(index);
    }

    public void initDataModel(List<Notebook> notebooks) {
        this.notebooks = new ArrayList<>(notebooks);
        updateModelList();
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
        contentPane.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Notebooks:");
        contentPane.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        contentPane.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        contentPane.add(notebookPanel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void createUIComponents() {
        notebookList = new JBList<>();
        notebookList.setEmptyText("There aren't notebooks");

        notebookPanel = ToolbarDecorator.createDecorator(notebookList)
                .setAddAction(anActionButton -> {
                    String newValue = new AddStringValueButton(
                            contentPane,
                            ZeppelinConstants.ADD_NOTEBOOK_TITLE,
                            ZeppelinConstants.NOTEBOOK_NAME_LABEL
                    ).getValue();

                    int selectedIndex = notebookList.getSelectedIndex();
                    Notebook notebook = ZeppelinModelFactory.createNotebook(newValue);
                    notebooks.add(selectedIndex + 1, notebook);

                    updateModelList();
                }).setRemoveAction(anActionButton -> {
                    int[] selectedIndices = notebookList.getSelectedIndices();
                    Arrays.sort(selectedIndices);
                    ArrayUtils.reverse(selectedIndices);
                    for (int i : selectedIndices) {
                        notebooks.remove(i);
                    }

                    updateModelList();
                })
                .createPanel();
    }

    private void updateModelList() {
        String[] array = notebooks.stream().map(Notebook::getName).toArray(String[]::new);
        final DefaultListModel<String> model = createDefaultListModel(array);
        notebookList.setModel(model);
    }
}