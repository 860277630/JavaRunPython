package com.example.demo.Test;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @program: JavaRunPython
 * @description: 用Runtime.getRuntime().exec()
 * @author: wjl
 * @create: 2023-04-23 16:14
 **/
public class Test01 {

    // 入参：无
    // 输出值：有
    @Test
    public void test01() {
        Process proc;
        try {
            proc = Runtime.getRuntime().exec("python "+getPath()+"demo1.py");// 执行py文件
            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 入参：有
    // 输出值：有
    @Test
    public void test02() {
        int a = 18;
        int b = 23;
        try {
            String[] args1 = new String[] { "python", getPath()+"demo2.py", String.valueOf(a), String.valueOf(b) };
            Process proc = Runtime.getRuntime().exec(args1);// 执行py文件

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取文件绝对路径
    private String getPath(){
        String path = System.getProperty("user.dir")+"\\src\\main\\resources\\PyBat\\";
        return path;
    }




}
