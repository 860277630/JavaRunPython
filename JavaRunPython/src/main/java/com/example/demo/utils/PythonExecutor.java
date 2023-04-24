package com.example.demo.utils;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * java调用python的执行器
 */
// 开启后使用注入形式，关闭后使用new的形式
//@Component
public class PythonExecutor {
    private static final Logger logger = LoggerFactory.getLogger(PythonExecutor.class);
    private static final String OS = System.getProperty("os.name");

    //private static final String WINDOWS_PATH = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1) + "py/automl/";  // windows为获取项目根路径即可
    private static final String WINDOWS_PATH = System.getProperty("user.dir")+"\\src\\main\\resources\\PyBat\\";  // windows为获取项目根路径即可
    private static final String LINUX_PATH = "/ai/xx";// linux为python文件所在目录

    private static ExecutorService taskPool = new ThreadPoolExecutor(8, 16
            , 200L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(600)
            , new ThreadFactoryBuilder()
            .setNameFormat("thread-自定义线程名-runner-%d").build());

    /**
     * 执行python文件【异步 无需等待py文件执行完毕】
     *
     * @param fileName python文件地址
     * @param params   参数,多个参数用英式逗号分割,没有就填写null
     * @throws IOException
     */
    public static void execPythonFile(String fileName, String envir,String... params) {
        taskPool.submit(() -> {
            try {
                exec(fileName,envir, params);
            } catch (IOException e) {
                logger.error("读取python文件 fileName=" + fileName + " 异常", e);
            }
        });

    }

    /**
     * 执行python文件 【同步 会等待py执行完毕】
     *
     * @param fileName python文件地址
     * @param params   参数,多个参数用英式逗号分割,没有就填写null
     * @param envir    运行环境,python.exe路径，可以是anaconda等环境下面的，使用默认填写null
     * @throws IOException
     */
    public static void execPythonFileSync(String fileName, String envir,String... params) {
        try {
            execSync(fileName,envir,params);
        } catch (IOException e) {
            logger.error("读取python文件 fileName=" + fileName + " 异常", e);
        }
    }

    private static void exec(String fileName, String envir,String... params) throws IOException {
        logger.info("读取python文件 init fileName={}&path={}", fileName, WINDOWS_PATH);
        List<String> args = initParams(fileName,envir, params);
        Process process;
        process = new ProcessBuilder(args.toArray(new String[args.size()])).start();
        new Thread(() -> {
            logger.info("读取python文件 开始 fileName={}", fileName);
            BufferedReader errorReader = null;
            // 脚本执行异常时的输出信息
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            List<String> errorString = read(fileName, errorReader);
            logger.info("读取python文件 异常 fileName={}&errorString={}", fileName, errorString);
        }).start();

        new Thread(() -> {
            // 脚本执行正常时的输出信息
            BufferedReader inputReader = null;
            inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> returnString = read(fileName, inputReader);
            logger.info("读取python文件 fileName={}&returnString={}", fileName, returnString);
        }).start();

        try {
            logger.info("读取python文件 wait fileName={}", fileName);
            process.waitFor();
        } catch (InterruptedException e) {
            logger.error("读取python文件 fileName=" + fileName + " 等待结果返回异常", e);
        }
        logger.info("读取python文件 fileName={} == 结束 ==", fileName);
    }

    private static void execSync(String fileName,String envir, String... params) throws IOException {
        logger.info("同步读取python文件 init fileName={}&path={}&environment={}", fileName, WINDOWS_PATH,envir);
        //然后把非null的元素 集合在一起
        List<String> args = initParams(fileName,envir, params);
        Process process;
        process = new ProcessBuilder(args.toArray(new String[args.size()])).start();
        taskPool.submit(() -> {
            logger.info("读取python文件 开始 fileName={}", fileName);
            BufferedReader errorReader = null;
            // 脚本执行异常时的输出信息
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            List<String> errorString = read(fileName, errorReader);
            logger.info("读取python文件 异常信息 fileName={}&errorString={}", fileName, errorString);
        });

        taskPool.submit(() -> {
            // 脚本执行正常时的输出信息
            BufferedReader inputReader = null;
            inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> returnString = read(fileName, inputReader);
            logger.info("读取python文件 正常信息 fileName={}&returnString={}", fileName, returnString);
        });

        try {
            logger.info("同步读取python文件 wait fileName={}", fileName);
            process.waitFor();
        } catch (InterruptedException e) {
            logger.error("同步读取python文件 fileName=" + fileName + " 等待结果返回异常", e);
        }
        logger.info("同步读取python文件 fileName={} == 结束 ==", fileName);
    }

    private static List<String> initParams(String fileName,String envir, String... params){
        List<String> args = new ArrayList<>();
        if (OS.startsWith("Windows")) {
            // windows执行脚本需要使用 cmd.exe /c 才能正确执行脚本
            args.add("cmd.exe");
            args.add("/c");
            if(Objects.isNull(envir)){
                args.add("python");
            }else {
                args.add(envir);
            }
            args.add(WINDOWS_PATH + fileName);
            if(Objects.nonNull(params)){
                for (String param : params) {
                    args.add(param);
                }
            }
        } else {
            // linux执行脚本一般是使用python3 + 文件所在路径
            if(Objects.isNull(envir)){
                args.add("python3");
            }else {
                args.add(envir);
            }
            args.add(LINUX_PATH + fileName);
            if(Objects.nonNull(params)){
                for (String param : params) {
                    args.add(param);
                }
            }
        }
        return args;
    }

    private static List<String> read(String fileName, BufferedReader reader) {
        List<String> resultList = Lists.newArrayList();
        String res = "";
        while (true) {
            try {
                if (!((res = reader.readLine()) != null)) break;
            } catch (IOException e) {
                logger.error("读取python文件 fileName=" + fileName + " 读取结果异常", e);
            }
            resultList.add(res);
        }
        return resultList;
    }

}
