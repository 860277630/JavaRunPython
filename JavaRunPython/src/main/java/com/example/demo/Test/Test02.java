package com.example.demo.Test;

import com.example.demo.utils.PythonExecutor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @program: JavaRunPython
 * @description: 相对于Test01更加底层
 * @author: wjl
 * @create: 2023-04-23 17:43
 **/
public class Test02 {

    //anaconda 运行环境
    private static final String ENVIR = "D:\\Program\\Anaconda\\envs\\paddle\\python.exe";


    //同步方法测试
    @Test
    public void test01(){
        // 无参数  默认环境
        System.err.println("=========================无参数  默认环境===================================");
        PythonExecutor.execPythonFileSync("demo1.py",null,null);
        // 有参数 默认环境
        System.err.println("=========================有参数 默认环境===================================");
        PythonExecutor.execPythonFileSync("demo2.py",null,new String[]{"18","32"});
        // 有参数  指定运行环境
        System.err.println("=========================有参数  指定运行环境===================================");
        PythonExecutor.execPythonFileSync("demo2.py",ENVIR,new String[]{"18","32"});
    }
}
class Test02Async{

    //anaconda 运行环境
    private static final String ENVIR = "D:\\Program\\Anaconda\\envs\\paddle\\python.exe";

    //异步方法测试
    public static void main(String[] args) {
        // 无参数  默认环境
        System.err.println("=========================无参数  默认环境===================================");
        PythonExecutor.execPythonFile("demo1.py",null,null);
        // 有参数 默认环境
        System.err.println("=========================有参数 默认环境===================================");
        PythonExecutor.execPythonFile("demo2.py",null,new String[]{"18","32"});
        // 有参数  指定运行环境
        System.err.println("=========================有参数  指定运行环境===================================");
        PythonExecutor.execPythonFile("demo2.py",ENVIR,new String[]{"18","32"});
    }
}
