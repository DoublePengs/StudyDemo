package com.example.studydemo;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/8/31
 */
public class Test {
    public static void main(String[] args) {
        String name = null;

        if ("张三".equals(name)) {
            System.out.println("Name is same!");
        } else {
            System.out.println("Name is different!");
        }

        javaPoetTest();
    }


    private static void javaPoetTest() {

        // import javax.lang.model.element.Modifier;  android sdk 包中没有，需要在 Java 工程
//        MethodSpec main = MethodSpec.methodBuilder("main")
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .returns(void.class)
//                .addParameter(String[].class, "args")
//                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
//                .build();
//
//        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//                .addMethod(main)
//                .build();
//
//        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
//                .build();
//
//        try {
//            javaFile.writeTo(System.out);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
