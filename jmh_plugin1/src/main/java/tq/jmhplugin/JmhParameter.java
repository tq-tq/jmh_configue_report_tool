package tq.jmhplugin;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.CollectionListModel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.options.TimeValue;
import tq.jmhplugin.runConfigue.ConfigurationUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JmhParameter {

    private static JmhParameter jmhParameter;
    private static Project project;
    private static PsiClass selectedClass;
    private static final CollectionListModel<String> methodMemberListModel = new CollectionListModel<>();

    private static Mode mode = Mode.AverageTime;
    private static int warmupIterations = 5;
    private static TimeValue warmupTime = TimeValue.seconds(1);
    private static int measurementIterations = 5;
    private static TimeValue measurementTime = TimeValue.seconds(1);
    private static TimeUnit timeUnit = TimeUnit.NANOSECONDS;
    private static int forks = 3;
    List<PsiMethod> psiMethodList = new LinkedList<PsiMethod>();

    public static CollectionListModel<String> getMethodMemberListModel() {
        return methodMemberListModel;
    }

    public static PsiClass getSelectedClass() {
        return selectedClass;
    }


    public static void setSelectedClass(PsiClass selectedClass) {
        JmhParameter.selectedClass = selectedClass;
    }

    public static Project getProject() {
        return project;
    }

    public static PsiMethodMember[] searchJMHMethodMember(PsiClass cls) {
        if(cls==null)
            return null;
        return Arrays.stream(cls.getMethods())
                .filter(method -> method.hasAnnotation(ConfigurationUtils.JMH_ANNOTATION_NAME))
                .map(PsiMethodMember::new).toArray(PsiMethodMember[]::new);
    }

    public static void setProject(Project project) {
        JmhParameter.project = project;
    }


    public static JmhParameter getJmhParameter() {
        if(jmhParameter == null)
        {
            jmhParameter = new JmhParameter();
        }
        return jmhParameter;
    }

}