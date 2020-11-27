package com.example.studydemo.annotation

import android.app.Activity
import android.view.View
import com.example.studydemo.annotationz.ViewById
import java.lang.RuntimeException

/**
 *
 * Description: IOC 注入 ViewUtils
 * Author: glp
 * CreateDate: 2020-06-02
 */
class ViewUtils {

    fun inject(activity: Activity) {
        inject(ViewFinder(activity), activity)
    }

    fun inject(view: View) {
        inject(ViewFinder(view), view)
    }

    fun inject(view: View, obj: Any) {
        inject(ViewFinder(view), obj)
    }

    fun inject(viewFinder: ViewFinder, obj: Any) {

    }

    /**
     * 注入属性
     */
    fun injectField(viewFinder: ViewFinder, obj: Any) {
        // object --> activity or fragment or view 是反射的类
        // viewFinder --> 只是一个view的findViewById的辅助类

        val clazz = obj.javaClass

        // 1. 获取所有的属性,包括私有和公有
        val fields = clazz.declaredFields

        fields.forEach { field ->

            // 2. 获取属性上面ViewById的值
            val annotation = field.getAnnotation(ViewById::class.java)
            if (annotation != null) {
                // 获取ViewById属性上的viewId值
                val viewId = annotation.value
                // 3. 通过findViewById获取View
                val view = viewFinder.findViewById(viewId)

                if (view != null) {
                    // 4. 反射注入View属性,设置所有属性都能注入包括私有和公有
                    field.isAccessible = true
                    try {
                        field.set(obj, view)
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                } else {
                    throw RuntimeException("Invalid @ViewInject for " + clazz.simpleName + "." + field.name)
                }
            }
        }
    }

    /**
     * 注入事件
     */
    fun injectEvent(viewFinder: ViewFinder, obj: Any) {

    }
}