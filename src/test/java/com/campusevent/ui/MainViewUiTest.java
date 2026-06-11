package com.campusevent.ui;

import com.campusevent.model.Organizer;
import com.campusevent.model.Student;
import javafx.scene.Node;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@EnabledIfSystemProperty(named = "javafx.ui.tests", matches = "true")
class MainViewUiTest {

    @Test
    void mainViewShowsStudentViewForStudentUser() {
        Node center = JavaFxTestSupport.runOnFxThread(() -> {
            AppContext context = new AppContext(null);
            context.setCurrentUser(new Student("S001", "Alice", "pass", "S001", "Design"));

            return new MainView(context).getCenter();
        });

        assertInstanceOf(StudentView.class, center);
    }

    @Test
    void mainViewShowsOrganizerViewForOrganizerUser() {
        Node center = JavaFxTestSupport.runOnFxThread(() -> {
            AppContext context = new AppContext(null);
            context.setCurrentUser(new Organizer("O001", "Bob", "pass", "E001", "Campus Office"));

            return new MainView(context).getCenter();
        });

        assertInstanceOf(OrganizerView.class, center);
    }
}
