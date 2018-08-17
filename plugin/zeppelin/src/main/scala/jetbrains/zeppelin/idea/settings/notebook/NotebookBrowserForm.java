package jetbrains.zeppelin.idea.settings.notebook;

import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.ui.components.JBList.createDefaultListModel;

public class NotebookBrowserForm extends JDialog {
    private JPanel notebookPanel;
    private JPanel contentPane;
    private List<String> notebookNames = new ArrayList<>();
    private JBList<String> notebookList;

    public NotebookBrowserForm() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
    }


    public void initDataModel(List<String> notebookNames) {
        this.notebookNames = notebookNames;
        updateModelList();
    }

    public List<String> getNotebookNames() {
        return notebookNames;
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
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
                    updateModelList();
                })
                .createPanel();
    }


    private void updateModelList() {
        String[] array = notebookNames.toArray(new String[0]);
        final DefaultListModel<String> model = createDefaultListModel(array);
        notebookList.setModel(model);
    }
}
