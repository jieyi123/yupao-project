package com.pjieyi.yupao.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;

import java.util.List;
import java.util.Map;

/**
 * @author pjieyi
 * @description
 */
public class ImportExcel {

    public static void main(String[] args) {
        // 写法4
        String fileName = "C:\\Users\\pjy17\\Downloads\\testExcel.xlsx";

        synchronousRead(fileName);

    }

    /**
     * 监听器读取
     * @param fileName
     */
    public static void readByListener(String fileName){
        // 一个文件一个reader
        try (ExcelReader excelReader = EasyExcel.read(fileName, XingQiuTableUserInfo.class, new DemoDataListener()).build()) {
            // 构建一个sheet 这里可以指定名字或者no
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            // 读取一个sheet
            excelReader.read(readSheet);
        }
    }

    /**
     * 同步读
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead(String fileName){
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuTableUserInfo> list = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo data : list) {
            System.out.println("读取到数据："+data);
        }
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
        for (Map<Integer, String> data : listMap) {
            // 返回每条数据的键值对 表示所在的列 和所在列的值
            System.out.println("读取到数据："+data);
        }
    }
}
