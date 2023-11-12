package com.example.studydemo.print;

/**
 * Description:
 *
 * @author glp
 * @date 2023/10/26
 */
public interface IPurePrintTask {

    void appendText(String str);

    void updatePrintIndex();

    void finishPrint();
}