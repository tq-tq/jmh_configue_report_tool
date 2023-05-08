package tq.jmhplugin;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.CollectionListModel;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JmhParameter {
    private static Project project;
    private static PsiClass selectedClass;
    private static final CollectionListModel<String> methodMemberListModel = new CollectionListModel<>();
    public static int flag;
    public static final String JMH_ANNOTATION_NAME = "org.openjdk.jmh.annotations.Benchmark";

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
        if (cls == null)
            return null;
        return Arrays.stream(cls.getMethods())
                .filter(method -> method.hasAnnotation(JMH_ANNOTATION_NAME))
                .map(PsiMethodMember::new).toArray(PsiMethodMember[]::new);
    }

    public static void setProject(Project project) {
        JmhParameter.project = project;
    }

    public static class JMHParameters {
        Mode mode;
        int warmupIterations;
        TimeValue warmupTime;
        int measurementIterations;
        TimeValue measurementTime;
        TimeUnit timeUnit;
        int forks;

        public JMHParameters(Mode mode, int warmupIterations, TimeValue warmupTime, int measurementIterations, TimeValue measurementTime, TimeUnit timeUnit, int forks) {
            this.mode = mode;
            this.warmupIterations = warmupIterations;
            this.warmupTime = warmupTime;
            this.measurementIterations = measurementIterations;
            this.measurementTime = measurementTime;
            this.timeUnit = timeUnit;
            this.forks = forks;
        }
    }

    public static void runJmhTest(JMHParameters jmhParameters) throws RunnerException, ExecutionException, InterruptedException, IOException, RuntimeException {
        //java -jar target/benchmarks.jar org.sample.JmhTestApp1.arrayListAdd -bm thrpt -f 1 -i 5 -r 1s -tu ms -w 1s -wi 5 -rf json
        flag=1;
        GeneralCommandLine generalCommandLine = new GeneralCommandLine("java");
        ArrayList<String> parameters = new ArrayList<>(Arrays.asList("-jar", "target/benchmarks.jar"));
        parameters.addAll(methodMemberListModel.getItems());
        parameters.addAll(Arrays.asList("-bm", jmhParameters.mode.shortLabel(),
                "-f", String.valueOf(jmhParameters.forks),
                "-i", String.valueOf(jmhParameters.measurementIterations),
                "-r", jmhParameters.measurementTime.toString().replace(" ", ""),
                "-tu", TimeValue.tuToString(jmhParameters.timeUnit),
                "-w", jmhParameters.warmupTime.toString().replace(" ", ""),
                "-wi", String.valueOf(jmhParameters.warmupIterations),
                "-rf","json",
                "-rff","jmhOut.json",
                "-o", "jmhReadableResult.txt"));
        generalCommandLine.addParameters(parameters);
        generalCommandLine.setCharset(StandardCharsets.UTF_8);
        generalCommandLine.setWorkDirectory(project.getBasePath());
        Process process = generalCommandLine.createProcess();
        process.onExit().thenAccept((Process p)-> {
            flag=0;
            NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(
                    "runJmh.notification.tq.jmhplugin");
            Notification notification = notificationGroup.createNotification("Attention",
                    "Finish running JMH test and create JSON file",
                    NotificationType.INFORMATION);
            Notifications.Bus.notify(notification);
        });
        process.getInputStream();
    }
}
