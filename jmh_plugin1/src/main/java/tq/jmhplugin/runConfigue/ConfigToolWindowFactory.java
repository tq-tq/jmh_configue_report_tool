// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package tq.jmhplugin.runConfigue;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import tq.jmhplugin.JmhParameter;
import java.util.Objects;

import static tq.jmhplugin.JmhUtils.JMH_ANNOTATION_NAME;

public class ConfigToolWindowFactory implements ToolWindowFactory {
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    PsiManager.getInstance(project).addPsiTreeChangeListener(
            new PsiTreeChangeAdapter() {
              @Override
              public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {
                  if(event.getChild().getText().equals("@"+JMH_ANNOTATION_NAME)||event.getChild().getText().equals("@Benchmark") ){
                    PsiMethod containingMethod = PsiTreeUtil.getParentOfType(event.getChild(),
                              PsiMethod.class);
                    if (containingMethod != null) {
                        JmhParameter.getMethodMemberListModel().remove(Objects.requireNonNull(PsiUtil.getMemberQualifiedName(containingMethod)));
                    }
                }
              }
            });
    JmhParameter.setProject(project);
    ConfigToolWindow configToolWindow = new ConfigToolWindow(toolWindow);
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(configToolWindow.getContent(), "", false);
    toolWindow.getContentManager().addContent(content);
  }

}
