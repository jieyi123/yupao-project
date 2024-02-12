package com.pjieyi.yupao.easyexcel;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pjieyi
 * @description 导入星球用户到数据库
 */
public class ImportXingQiuUser {

    public static void main(String[] args) {
        String fileName = "C:\\Users\\pjy17\\Downloads\\testExcel.xlsx";
        List<XingQiuTableUserInfo> list = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        System.out.println("总数："+list.size());
        Map<String, List<XingQiuTableUserInfo>> collect = list.stream().filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                .collect(Collectors.groupingBy(userInfo -> userInfo.getUsername()));
        for (String key:collect.keySet()){
            System.out.println(collect.get(key));
            System.out.println("1");
        }
    }
}
