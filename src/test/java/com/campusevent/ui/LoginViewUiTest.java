package com.campusevent.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "javafx.ui.tests", matches = "true")
class LoginViewUiTest {

    @Test
    void loginViewBuildsExpectedControls() {
        LoginViewSnapshot snapshot = JavaFxTestSupport.runOnFxThread(() -> {
            LoginView view = new LoginView(new AppContext(null));
            List<Node> children = view.getChildren();

            List<String> labelTexts = children.stream()
                    .filter(node -> node instanceof Label)
                    .map(node -> ((Label) node).getText())
                    .collect(Collectors.toList());

            ComboBox<?> accountBox = (ComboBox<?>) children.get(2);
            PasswordField passwordField = (PasswordField) children.get(3);
            Button loginButton = (Button) children.get(4);
            TextField demoAccountField = (TextField) children.get(5);

            return new LoginViewSnapshot(
                    children.size(),
                    labelTexts,
                    accountBox.getPromptText(),
                    passwordField.getPromptText(),
                    loginButton.getText(),
                    demoAccountField.getText()
            );
        });

        assertEquals(6, snapshot.childCount);
        assertEquals("Campus Event Management System", snapshot.labelTexts.get(0));
        assertEquals("校園活動管理系統", snapshot.labelTexts.get(1));
        assertEquals("學號 / 教師編號", snapshot.accountPrompt);
        assertEquals("密碼", snapshot.passwordPrompt);
        assertEquals("登入", snapshot.loginButtonText);
        assertTrue(snapshot.demoText.contains("A11423011"));
        assertTrue(snapshot.demoText.contains("001"));
    }

    private static final class LoginViewSnapshot {
        private final int childCount;
        private final List<String> labelTexts;
        private final String accountPrompt;
        private final String passwordPrompt;
        private final String loginButtonText;
        private final String demoText;

        private LoginViewSnapshot(
                int childCount,
                List<String> labelTexts,
                String accountPrompt,
                String passwordPrompt,
                String loginButtonText,
                String demoText
        ) {
            this.childCount = childCount;
            this.labelTexts = labelTexts;
            this.accountPrompt = accountPrompt;
            this.passwordPrompt = passwordPrompt;
            this.loginButtonText = loginButtonText;
            this.demoText = demoText;
        }
    }
}
