package tq.jmhplugin.myAction;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.psi.PsiClass;

import static tq.jmhplugin.JmhUtils.JMH_ANNOTATION_NAME;

/**
 * User: nikart
 * Date: 18/03/14
 * Time: 13:01
 */
public class CreateTemplateFactory {
    public static Template createMethod(PsiClass psiClass) {
        Template template = TemplateManager.getInstance(psiClass.getProject()).createTemplate("", "");
        template.addTextSegment("@" + JMH_ANNOTATION_NAME + "\n");
        template.addTextSegment("public void measure");
        Expression nameExpr = new ConstantNode("Name");
        template.addVariable("name", nameExpr, nameExpr, true);
        template.addTextSegment("(org.openjdk.jmh.infra.Blackhole bh) {\n}");

        template.setToIndent(true);
        template.setToReformat(true);
        template.setToShortenLongNames(true);

        return template;
    }

}
