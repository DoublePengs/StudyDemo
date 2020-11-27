package danny.jiang.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class LifeCyclePlugin implements Plugin<Project> {

    void apply(Project project) {
        System.out.println("--------->> LifeCyclePlugin gradle plugin")
    }
}