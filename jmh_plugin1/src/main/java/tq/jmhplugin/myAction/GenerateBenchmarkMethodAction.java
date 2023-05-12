package tq.jmhplugin.myAction;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import static tq.jmhplugin.JmhUtils.findTargetClass;
import static tq.jmhplugin.JmhUtils.findTargetMethod;

/**
 * User: nikart
 * Date: 10/03/14
 * Time: 16:43
 */
public class GenerateBenchmarkMethodAction extends BaseGenerateAction {

    public GenerateBenchmarkMethodAction() {
        super(new BenchmarkMethodHandler());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if(psiFile != null)
        {
            PsiClass targetClass = findTargetClass(editor, psiFile);
            PsiMethod targetMethod = findTargetMethod(editor, psiFile);
            e.getPresentation().setEnabledAndVisible(targetClass != null || targetMethod != null);
        }
        else e.getPresentation().setEnabledAndVisible(false);

    }

}
