package com.example.studydemo.annotationz

/**
 *
 * Description: IOC的View属性注解类
 * Author: glp
 * CreateDate: 2020-06-02
 */

// FIELD 注解只能放在属性上    METHOD 方法上  TYPE 类上  CONSTRUCTOR 构造方法上
@Target(AnnotationTarget.FIELD)
// RUNTIME 运行时检测, CLASS 编译时（butterKnife使用是这个）,  SOURCE 源码资源的时候
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewById(val value: Int)