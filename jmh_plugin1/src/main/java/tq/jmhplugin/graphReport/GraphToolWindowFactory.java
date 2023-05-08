// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package tq.jmhplugin.graphReport;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import tq.jmhplugin.JmhParameter;

public class GraphToolWindowFactory implements ToolWindowFactory {

  /**
   * Create the tool window content.
   *
   * @param project    current project
   * @param toolWindow current tool window
   */

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    JmhParameter.setProject(project);
    GraphToolWindow configToolWindow = new GraphToolWindow();
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(configToolWindow.getContent(), "", false);
    toolWindow.setType(ToolWindowType.WINDOWED,null);
    toolWindow.setTitle("JMH Result Chart");
    toolWindow.getContentManager().addContent(content);
  }

}
