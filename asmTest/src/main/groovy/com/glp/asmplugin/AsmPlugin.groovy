package com.glp.asmplugin

import com.android.build.gradle.AppExtension

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Description:
 * 获取project中的AppExtension类型extension，然后注册我们自己定义的Transform。
 *
 * 啥是AppExtension，我们app的gradle中最上面都有这个插件apply plugin: 'com.android.application'
 * 如果依赖了这个插件，AppExtension就存在。
 *
 * @author glp* @date 2021-11-20
 */
public class AsmPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        println '----------- 开始注册 >>>>> -----------'
        AsmTransform transform = new AsmTransform()
        android.registerTransform(transform)
    }
}
