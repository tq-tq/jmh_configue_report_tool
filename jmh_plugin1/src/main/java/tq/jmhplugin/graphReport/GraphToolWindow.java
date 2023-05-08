// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package tq.jmhplugin.graphReport;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.ide.util.*;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBScrollPane;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartPanel;
import tq.jmhplugin.JmhParameter;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
public class GraphToolWindow {
    private PsiFile selectedFile;
    public static final JPanel myToolWindowContent = new JPanel();
    static final JLabel selectedFileLabel = new JLabel("No Json File selected");
    static JSONArray selectedJsonArray;
    static final JButton chooseFileButton = new JButton("Select Json File");

    public GraphToolWindow() {
        selectedFileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chooseFileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        myToolWindowContent.add(selectedFileLabel);
        myToolWindowContent.add(chooseFileButton);

        chooseFileButton.addActionListener(e -> {
            selectedFile = chooseFile(JmhParameter.getProject());
            if (selectedFile == null) {
                selectedFileLabel.setText("No Json file selected");
                selectedJsonArray = null;
            } else {
                selectedFileLabel.setText(selectedFile.getVirtualFile().getPath());
                try {
                    selectedJsonArray = getJsonArray(selectedFileLabel.getText());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            refreshBarChart();

        });

        BoxLayout layout = new BoxLayout(myToolWindowContent, BoxLayout.Y_AXIS);
        myToolWindowContent.setLayout(layout);
    }

    public JPanel getContent() {
        JScrollPane scrollPane = new JBScrollPane(myToolWindowContent);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(10, 10, 880, 600);
        JPanel contentPane = new JPanel(null);
        contentPane.add(scrollPane);

        return contentPane;
    }

    public static PsiFile chooseFile(Project project) {
        TreeFileChooser chooser = TreeFileChooserFactory.getInstance(project)
                .createFileChooser("Select a File", null, JsonFileType.INSTANCE, null);
        chooser.showDialog();

        return chooser.getSelectedFile();
    }

    private JSONArray getJsonArray(String selectedFilePath) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(selectedFilePath));
        String conf = IOUtils.toString(in, StandardCharsets.UTF_8);
        return JSON.parseArray(conf);
    }

    private static HashMap<String, Map<String, Double>> getScoreMap() {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        HashMap<String, Map<String, Double>> scoreMap = new LinkedHashMap<>();
        for (int i = 0; i < selectedJsonArray.size(); i++) {
            JSONObject resultObject = selectedJsonArray.getJSONObject(i);
            JSONObject primaryMetricObject = resultObject.getJSONObject("primaryMetric");
            String modeAndScoreUnit =
                    "mode: "+resultObject.getString("mode")+", Score Unit: "+primaryMetricObject.getString(
                    "scoreUnit");
            String format = nf.format(primaryMetricObject.getDouble("score")).replace(",", "");
            String[] benchmarkName = resultObject.getString("benchmark").split("\\.");
            if(scoreMap.containsKey(modeAndScoreUnit)){
                scoreMap.get(modeAndScoreUnit).put(benchmarkName[benchmarkName.length - 1], Double.parseDouble(format));
            }
            else{
                LinkedMap valueMap = new LinkedMap();
                valueMap.put(benchmarkName[benchmarkName.length - 1], Double.parseDouble(format));
                scoreMap.put(modeAndScoreUnit, valueMap);
            }
        }
        return scoreMap;
    }

    public static void refreshBarChart() {
        System.out.println(myToolWindowContent.getComponentCount());
        myToolWindowContent.remove(0);

        myToolWindowContent.removeAll();

        myToolWindowContent.add(selectedFileLabel);
        myToolWindowContent.add(chooseFileButton);
        if (selectedJsonArray != null) {
            for (String modeAndScoreUnit:
                 getScoreMap().keySet()
                 ) {
                JLabel modeScoreUnitJLabel = new JLabel(modeAndScoreUnit);
                modeScoreUnitJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                ChartPanel scoreBarChart = new BarChart(getScoreMap().get(modeAndScoreUnit)).createChartPanel();
                scoreBarChart.setAlignmentX(Component.LEFT_ALIGNMENT);
                scoreBarChart.setBounds(0,0,800,400);
                myToolWindowContent.add(modeScoreUnitJLabel);
                myToolWindowContent.add(scoreBarChart);
            }
        }

    }
}


