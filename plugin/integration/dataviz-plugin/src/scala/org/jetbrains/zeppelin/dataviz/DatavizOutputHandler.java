package org.jetbrains.zeppelin.dataviz;

import com.intellij.openapi.project.Project;
import jetbrains.zeppelin.service.TableOutputHandler;
import jetbrains.zeppelin.utils.ZeppelinLogger;
import org.intellij.datavis.data.DataProvider;
import org.intellij.datavis.data.DataProviderWrapper;

public class DatavizOutputHandler implements TableOutputHandler {
    @Override
    public void invoke(Project project, String msg) {
        String id = "Zeppelin";
        String name = "Result";
        DataProvider dataProvider = DataProviderWrapper.getProvider(project);
        try {
            if (dataProvider.isExist(id)) {
                dataProvider.removeData(id);
            }
            dataProvider.addData(id, name, msg, '\t');
        } catch (Exception e) {
            ZeppelinLogger.printError("Cannot perform show table in dataviz plugin");
        }
    }
}