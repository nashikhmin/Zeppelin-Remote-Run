package jetbrains.zeppelin.configuration;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import jetbrains.zeppelin.components.ZeppelinConnection;
import jetbrains.zeppelin.components.ZeppelinConnection$;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConfigurationForm implements SearchableConfigurable {
    private final Project myProject;
    private JPanel settingForm;
    private JTextField addressField;
    private JTextField portField;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JCheckBox anonymousCheckBox;

    public ConfigurationForm(@NotNull Project project) {
        myProject = project;
        setDefaultValues(project);
    }

    private void setDefaultValues(@NotNull Project project) {
        ZeppelinConnection connection = ZeppelinConnection$.MODULE$.connectionFor(project);
        passwordField.setText(connection.password());
        usernameField.setText(connection.username());
        portField.setText(String.valueOf(connection.port()));
        addressField.setText(connection.uri());
        portField.setText(String.valueOf(connection.port()));
        anonymousCheckBox.setSelected(connection.anonymousAccess());
    }

    @Override
    public void apply() {
        if (!settingForm.isVisible()) {
            return;
        }

        final String newUsername = usernameField.getText();
        final String newPassword = String.valueOf(passwordField.getPassword());
        final String newUri = addressField.getText();
        final Integer newPort = Integer.valueOf(portField.getText());
        final Boolean newAnonAccess = anonymousCheckBox.isSelected();

        final ZeppelinConnection connection = ZeppelinConnection$.MODULE$.connectionFor(myProject);

        if (isModified()) {
            connection.setUri(newUri);
            connection.setPort(newPort);
            connection.setUsername(newUsername);
            connection.setPassword(newPassword);
            connection.setAnonymousAccess(newAnonAccess);
            connection.resetApi();
        }
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Zeppelin Notebook";
    }

    @Override
    public String getHelpTopic() {
        return "";
    }

    @Override
    public JComponent createComponent() {
        return settingForm;
    }

    @Override
    public boolean isModified() {
        final ZeppelinConnection connection = ZeppelinConnection$.MODULE$.connectionFor(myProject);
        final String oldUsername = connection.username();
        final String oldPassword = connection.password();
        final String oldUri = connection.uri();
        final Integer oldPort = connection.port();
        final Boolean oldAnonAccess = connection.anonymousAccess();


        final String newUsername = usernameField.getText();
        final String newPassword = String.valueOf(passwordField.getPassword());
        final String newUri = addressField.getText();
        final Integer newPort = Integer.valueOf(portField.getText());
        final Boolean newAnonAccess = anonymousCheckBox.isSelected();

        return !oldUsername.equals(newUsername) || !oldPassword.equals(newPassword) || !oldUri.equals(newUri)
                || !oldPort.equals(newPort) || !oldAnonAccess.equals(newAnonAccess);
    }

    @NotNull
    @Override
    public String getId() {
        return "ZeppelinConfigurable";
    }
}
