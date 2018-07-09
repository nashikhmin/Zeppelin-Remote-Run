package jetbrains.zeppelin.settings;

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
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private boolean isAnonymous;

    public ConfigurationForm(@NotNull Project project) {
        myProject = project;
        setDefaultValues(project);
    }

    private void setDefaultValues(@NotNull Project project) {
        ZeppelinConnection connection = ZeppelinConnection$.MODULE$.connectionFor(project);
        ZeppelinSettings zeppelinSettings = connection.getZeppelinSettings();
        passwordField.setText(zeppelinSettings.password());
        usernameField.setText(zeppelinSettings.login());
        portField.setText(String.valueOf(zeppelinSettings.port()));
        addressField.setText(zeppelinSettings.address());


        isAnonymous = zeppelinSettings.isAnonymous();
        anonymousCheckBox.setSelected(isAnonymous);
        setShowAuthPanel(!isAnonymous);
        anonymousCheckBox.addItemListener(e -> {
            isAnonymous = !isAnonymous;
            setShowAuthPanel(!isAnonymous);
        });

    }

    private void setShowAuthPanel(boolean isAuth) {
        passwordField.setVisible(isAuth);
        usernameField.setVisible(isAuth);
        usernameLabel.setVisible(isAuth);
        passwordLabel.setVisible(isAuth);
    }

    @Override
    public void apply() {
        if (!settingForm.isVisible()) {
            return;
        }

        final ZeppelinConnection connection = ZeppelinConnection$.MODULE$.connectionFor(myProject);

        ZeppelinSettings newZeppelinSettings = getZeppelinSettingsFromForm();
        if (isModified()) {
            connection.updateSettings(newZeppelinSettings);
        }
    }

    @NotNull
    private ZeppelinSettings getZeppelinSettingsFromForm() {
        ZeppelinSettings newZeppelinSettings = new ZeppelinSettings();
        newZeppelinSettings.setAddress(addressField.getText());
        newZeppelinSettings.setPort(Integer.valueOf(portField.getText()));
        newZeppelinSettings.setIsAnonymous(anonymousCheckBox.isSelected());
        newZeppelinSettings.setLogin(usernameField.getText());
        newZeppelinSettings.setPassword(String.valueOf(passwordField.getPassword()));
        return newZeppelinSettings;
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

        final ZeppelinSettings zeppelinSettings = connection.getZeppelinSettings();
        final ZeppelinSettings newZeppelinSettings = getZeppelinSettingsFromForm();

        return !zeppelinSettings.equals(newZeppelinSettings);
    }

    @NotNull
    @Override
    public String getId() {
        return "ZeppelinConfigurable";
    }
}
