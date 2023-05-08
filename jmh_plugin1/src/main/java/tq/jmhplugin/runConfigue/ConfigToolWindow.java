// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package tq.jmhplugin.runConfigue;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.execution.ExecutionException;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.TimeValue;
import tq.jmhplugin.JmhParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.awt.GridBagConstraints.*;
import static tq.jmhplugin.JmhParameter.*;

public class ConfigToolWindow {

    public static final JPanel myToolWindowContent = new JPanel();

    private final JLabel selectedClassLabel = new JLabel(JmhParameter.getSelectedClass() == null ? "No class selected" :
            JmhParameter.getSelectedClass().getName());
    private final JButton chooseClassButton = new JButton("Select Class");

    private final JLabel selectedMethodLabel = new JLabel("Select Methods");
    private final JPanel panel = getToolBar();

    private final JLabel chooseModeLabel = new JLabel("Benchmark mode:");
    private static final ComboBox<Mode> modeComboBox = new ComboBox<>(Mode.values());

    private final JLabel warmupIterationsLabel = new JLabel("Warmup iterations:");
    private static final JTextField warmupIterationsTextField = new NumberTextField("5", 10);

    private final JLabel warmupTimeLabel = new JLabel("Warmup Time:");
    private static final JTextField warmupTimeTextField = new NumberTextField("1", 10);
    private static final ComboBox<TimeUnit> warmupTimeUnitComboBox = new ComboBox<>(TimeUnit.values());

    private final JLabel measurementIterationsLabel = new JLabel("Measurement iterations:");
    private static final JTextField measurementIterationsTextField = new NumberTextField("5", 10);

    private final JLabel measurementTimeLabel = new JLabel("Measurement Time:");
    private static final JTextField measurementTimeTextField = new NumberTextField("1", 10);
    private static final ComboBox<TimeUnit> measurementTimeUnitComboBox = new ComboBox<>(TimeUnit.values());

    private final JLabel timeUnitLabel = new JLabel("Time unit:");
    private static final ComboBox<TimeUnit> timeUnitComboBox = new ComboBox<>(TimeUnit.values());

    private final JLabel forksLable = new JLabel("Forks:");
    private static final JTextField forksTextField = new NumberTextField("3", 10);

    private static final JButton RunJmhButton = new JButton("Run JMH Test");

    public ConfigToolWindow(ToolWindow toolWindow) {
        toolWindow.setType(ToolWindowType.DOCKED, null);
        chooseClassButton.addActionListener(e -> {
            JmhParameter.setSelectedClass(chooseClass(JmhParameter.getProject()));
            if (JmhParameter.getSelectedClass() == null)
                selectedClassLabel.setText("No class selected");
            else
                selectedClassLabel.setText(JmhParameter.getSelectedClass().getQualifiedName());
        });
        RunJmhButton.addActionListener(e -> {
            if (
                    getMethodMemberListModel().getItems().isEmpty() ||
                            warmupIterationsTextField.getText() == null || warmupTimeTextField.getText() == null ||
                            measurementTimeTextField.getText() == null ||
                            measurementIterationsTextField.getText() == null ||
                            forksTextField.getText() == null
            ) {
                new NullWarningDialogWrapper().show();
                return;
            }
            if (flag==0) {
                runJMHTest();
            } else {
                new RunWarningDialogWrapper().show();
            }

        });

        GridBagLayout layout = new GridBagLayout();
        myToolWindowContent.setLayout(layout);
    }

    public static void runJMHTest() {
        NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(
                "runJmh.notification.tq.jmhplugin");
        Notification notification = notificationGroup.createNotification("Attention",
                "Start to run JMH test and create JSON file",
                NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
        try {
            runJmhTest(getParameters());
        } catch (RunnerException | ExecutionException | InterruptedException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public JPanel getContent() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = NORTHWEST;
        gbc.fill = BOTH;

        addToToolWindow(gbc, selectedClassLabel, 0, 0, 1);
        addToToolWindow(gbc, chooseClassButton, 1, 0, 2);

        addToToolWindow(gbc, selectedMethodLabel, 0, 1, 1);
        addToToolWindow(gbc, panel, 1, 1, 2);

        addToToolWindow(gbc, chooseModeLabel, 0, 3, 1);
        addToToolWindow(gbc, modeComboBox, 1, 3, 2);

        addToToolWindow(gbc, warmupIterationsLabel, 0, 4, 1);
        addToToolWindow(gbc, warmupIterationsTextField, 1, 4, 2);

        addToToolWindow(gbc, warmupTimeLabel, 0, 5, 1);
        addToToolWindow(gbc, warmupTimeTextField, 1, 5, 1);
        warmupTimeUnitComboBox.setSelectedItem(TimeUnit.SECONDS);
        addToToolWindow(gbc, warmupTimeUnitComboBox, 2, 5, 1);

        addToToolWindow(gbc, measurementIterationsLabel, 0, 6, 1);
        addToToolWindow(gbc, measurementIterationsTextField, 1, 6, 2);

        addToToolWindow(gbc, measurementTimeLabel, 0, 7, 1);
        addToToolWindow(gbc, measurementTimeTextField, 1, 7, 1);
        measurementTimeUnitComboBox.setSelectedItem(TimeUnit.SECONDS);
        addToToolWindow(gbc, measurementTimeUnitComboBox, 2, 7, 1);


        addToToolWindow(gbc, timeUnitLabel, 0, 8, 1);
        measurementTimeUnitComboBox.setSelectedItem(TimeUnit.MICROSECONDS);
        addToToolWindow(gbc, timeUnitComboBox, 1, 8, 2);

        addToToolWindow(gbc, forksLable, 0, 9, 1);
        addToToolWindow(gbc, forksTextField, 1, 9, 2);

        addToToolWindow(gbc, RunJmhButton, 2, 10, 1);

        return myToolWindowContent;
    }

    private void addToToolWindow(GridBagConstraints gbc, Component component, int x, int y, int gridWidth) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = gridWidth;
        myToolWindowContent.add(component, gbc);
    }

    public static PsiClass chooseClass(Project project) {
        TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                .createProjectScopeChooser("Select A Class");
        chooser.showDialog();

        return chooser.getSelected();
    }

    private JPanel getToolBar() {
        JBList methodsList = new JBList(JmhParameter.getMethodMemberListModel());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(methodsList)
                .setAddAction(button -> {
                    if (JmhParameter.getSelectedClass() != null) {
                        MemberChooser memberChooser =
                                new MemberChooser(JmhParameter.searchJMHMethodMember(JmhParameter.getSelectedClass()), false, true,
                                        JmhParameter.getProject());
                        memberChooser.show();
                        List<PsiMethodMember> selectedMethods = memberChooser.getSelectedElements();
                        if (selectedMethods != null) {
                            for (PsiMethodMember selectedMethod :
                                    selectedMethods) {
                                String name = PsiUtil.getMemberQualifiedName(selectedMethod.getElement());
                                if (!JmhParameter.getMethodMemberListModel().getItems().contains(name)) {
                                    JmhParameter.getMethodMemberListModel().add(name);
                                }
                            }
                        }
                    }
                })
                .setRemoveAction(button -> {
                    for (Object selectedValue : methodsList.getSelectedValuesList()) {
                        JmhParameter.getMethodMemberListModel().remove((String) selectedValue);
                    }
                });
        return decorator.createPanel();
    }

    private static class NumberTextField extends JTextField {
        public NumberTextField(String text, int columns) {
            super(text, columns);
            this.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    int keyChar = e.getKeyChar();
                    if (keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9) {

                    } else {
                        e.consume();
                    }
                }
            });
        }

    }

    public static JmhParameter.JMHParameters getParameters() {
        Mode mode = (Mode) modeComboBox.getSelectedItem();
        int warmupIterations =
                Integer.parseInt(warmupIterationsTextField.getText());
        TimeValue warmupTime = new TimeValue(Integer.parseInt(warmupTimeTextField.getText()),
                (TimeUnit) warmupTimeUnitComboBox.getSelectedItem());
        int measurementIterations = Integer.parseInt(measurementIterationsTextField.getText());
        TimeValue measurementTime = new TimeValue(Integer.parseInt(measurementTimeTextField.getText()),
                (TimeUnit) measurementTimeUnitComboBox.getSelectedItem());
        int forks = Integer.parseInt(forksTextField.getText());
        TimeUnit timeUnit = (TimeUnit) timeUnitComboBox.getSelectedItem();
        return new JmhParameter.JMHParameters(mode, warmupIterations, warmupTime, measurementIterations, measurementTime, timeUnit, forks);
    }
}

class NullWarningDialogWrapper extends DialogWrapper {
    public NullWarningDialogWrapper() {
        super(true); // use current window as parent
        init();
        setTitle("Warning");
        setOKActionEnabled(true);
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Please fill in all text fields!");
        label.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }
}

class RunWarningDialogWrapper extends DialogWrapper {
    public RunWarningDialogWrapper() {
        super(true); // use current window as parent
        init();
        setTitle("Warning");
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("JMH test is running!");
        label.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }
}

