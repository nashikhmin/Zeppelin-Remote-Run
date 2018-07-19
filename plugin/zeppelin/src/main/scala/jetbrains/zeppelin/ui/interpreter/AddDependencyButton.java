package jetbrains.zeppelin.ui.interpreter;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AddDependencyButton extends DialogWrapper {
    private JPanel contentPane;
    private JTextField inputField;

    AddDependencyButton(JComponent parent) {
        super(parent, false);
        init();
        setTitle("Add Dependency");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    String getValue() {
        if (showAndGet()) {
            return inputField.getText();
        } else {
            return null;
        }
    }
}