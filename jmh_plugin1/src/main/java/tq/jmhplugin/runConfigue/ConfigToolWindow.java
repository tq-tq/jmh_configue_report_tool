// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package tq.jmhplugin.runConfigue;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowContentUiType;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.psi.PsiClass;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.openjdk.jmh.annotations.Mode;
import tq.jmhplugin.JmhParameter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.awt.GridBagConstraints.*;

public class ConfigToolWindow {

    private final JPanel myToolWindowContent = new JPanel();

    private final JLabel selectedClassLabel = new JLabel(JmhParameter.getSelectedClass() == null ? "No class selected" :
            JmhParameter.getSelectedClass().getName());
    private final JButton chooseClassButton = new JButton("Select Class");

    private final JLabel selectedMethodLabel = new JLabel("Select Methods");
    private final JPanel panel = getToolBar();

    private final JLabel chooseModeLabel = new JLabel("Benchmark mode:");
    ComboBox<Mode> modeComboBox =new ComboBox<>(Mode.values()) ;

    private final JLabel warmupIterationsLabel = new JLabel("Warmup iterations:");
    private final JTextField warmupIterationsTextField = new NumberTextField("5",10);

    private final JLabel warmupTimeLabel = new JLabel("Warmup Time:");
    private final JTextField warmupTimeTextField = new NumberTextField("1",10);
    ComboBox<TimeUnit> warmupTimeUnitComboBox =new ComboBox<>(TimeUnit.values()) ;

    private final JLabel measurementIterationsLabel = new JLabel("Measurement iterations:");
    private final JTextField measurementIterationsTextField = new NumberTextField("5",10);

    private final JLabel measurementTimeLabel = new JLabel("Measurement Time:");
    private final JTextField measurementTimeTextField = new NumberTextField("1",10);
    ComboBox<TimeUnit> measurementTimeUnitComboBox =new ComboBox<>(TimeUnit.values()) ;

    private final JLabel timeUnitLabel = new JLabel("Time unit:");
    ComboBox<TimeUnit> timeUnitComboBox =new ComboBox<>(TimeUnit.values()) ;

    private final JLabel forksLable = new JLabel("Forks:");
    private final JTextField forksTextField = new NumberTextField("3",10);

    private final JButton RunJmhButton = new JButton("Run JMH Test");

    public ConfigToolWindow(ToolWindow toolWindow) {
        toolWindow.setType(ToolWindowType.DOCKED, null);
        chooseClassButton.addActionListener(e -> {
            JmhParameter.setSelectedClass(chooseClass(JmhParameter.getProject()));
            if(JmhParameter.getSelectedClass() == null)
                selectedClassLabel.setText("No class selected");
            else
                selectedClassLabel.setText(JmhParameter.getSelectedClass().getQualifiedName());
        });

        GridBagLayout layout = new GridBagLayout();
        myToolWindowContent.setLayout(layout);
    }

    public JPanel getContent() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = NORTHWEST;
        gbc.fill = BOTH;

        addToToolWindow(gbc,selectedClassLabel,0,0,1);
        addToToolWindow(gbc,chooseClassButton,1,0,2);

        addToToolWindow(gbc,selectedMethodLabel,0,1,1);
        addToToolWindow(gbc,panel,1,1,2);

        addToToolWindow(gbc,chooseModeLabel,0,3,1);
        addToToolWindow(gbc,modeComboBox,1,3,2);

        addToToolWindow(gbc,warmupIterationsLabel,0,4,1);
        addToToolWindow(gbc,warmupIterationsTextField,1,4,2);

        addToToolWindow(gbc,warmupTimeLabel,0,5,1);
        addToToolWindow(gbc,warmupTimeTextField,1,5,1);
        addToToolWindow(gbc,warmupTimeUnitComboBox,2,5,1);

        addToToolWindow(gbc,measurementIterationsLabel,0,6,1);
        addToToolWindow(gbc,measurementIterationsTextField,1,6,2);

        addToToolWindow(gbc,measurementTimeLabel,0,7,1);
        addToToolWindow(gbc,measurementTimeTextField,1,7,1);
        addToToolWindow(gbc,measurementTimeUnitComboBox,2,7,1);


        addToToolWindow(gbc, timeUnitLabel,0,8,1);
        addToToolWindow(gbc,timeUnitComboBox,1,8,2);

        addToToolWindow(gbc,forksLable,0,9,1);
        addToToolWindow(gbc,forksTextField,1,9,2);

        return myToolWindowContent;
    }
    private void addToToolWindow(GridBagConstraints gbc, Component component, int x, int y, int gridWidth ){
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = gridWidth;
        myToolWindowContent.add(component, gbc);
    }

    public static PsiClass chooseClass(Project project) {
        TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                .createProjectScopeChooser("Select a class");
        chooser.showDialog();

        return chooser.getSelected();
    }

    private JPanel getToolBar() {
        JBList methodsList = new JBList(JmhParameter.getMethodMemberListModel());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(methodsList)
                .setAddAction(button -> {
                    if(JmhParameter.getSelectedClass() != null){
                        MemberChooser memberChooser =
                                new MemberChooser(JmhParameter.searchJMHMethodMember(JmhParameter.getSelectedClass()), false, true,
                                        JmhParameter.getProject());
                        memberChooser.show();
                        List<PsiMethodMember> selectedMethods = memberChooser.getSelectedElements();
                        if (selectedMethods != null) {
                            for (PsiMethodMember selectedMethod:
                                    selectedMethods) {
                                String name = selectedMethod.getElement().getName();
                                if (!JmhParameter.getMethodMemberListModel().getItems().contains(name)){
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

    private class NumberTextField extends JTextField {
        public NumberTextField(String text, int columns) {
            super(text, columns);
            this.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent ke) {
                    String value = NumberTextField.super.getText();
                    int l = value.length();
                    if (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9') {
                        NumberTextField.super.setText("");
                    } else {
                        NumberTextField.super.setEditable(false);
                    }
                }
            });
        }

    }

}
