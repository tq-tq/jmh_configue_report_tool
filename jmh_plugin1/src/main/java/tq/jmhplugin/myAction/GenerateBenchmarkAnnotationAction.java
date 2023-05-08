package tq.jmhplugin.myAction;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import static tq.jmhplugin.JmhUtils.*;

public class GenerateBenchmarkAnnotationAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getProject();
        int offset = editor.getCaretModel().getOffset();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        PsiElement element = psiFile.findElementAt(offset);
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiAnnotation annotationFromText = factory.createAnnotationFromText("@"+JMH_ANNOTATION_NAME,
                containingMethod.getModifierList());
        if (!containingMethod.hasAnnotation(JMH_ANNOTATION_NAME)){
            WriteCommandAction.runWriteCommandAction(project, (Computable<PsiElement>) () ->
                    containingMethod.getModifierList().addBefore(annotationFromText,
                            containingMethod.getModifierList().getFirstChild())
            );
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        int offset = editor.getCaretModel().getOffset();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        PsiElement element = psiFile.findElementAt(offset);
        PsiMethod containingMethod = null;
        if (element != null) {
            containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);}
        e.getPresentation().setEnabledAndVisible(containingMethod != null);
    }

}
