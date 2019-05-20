#  android系统启动

<http://gityuan.com/2016/02/01/android-booting/>(启动概述)

- init.main(system/core/init/init.cpp)pid=1 用户空间的第一个进程
- 解析 init.rc文件
- 解析启动 Zygote进程
- zygote启动后会调用下面方法
```
App_main.main
    AndroidRuntime.start
        AndroidRuntime.startVm Java虚拟机创建
        AndroidRuntime.startReg JNI方法注册
        ZygoteInit.main (首次进入Java世界)
            registerZygoteSocket
            preload 预加载类和资源文件
            startSystemServer  通过fork方式启动system_server
            runSelectLoop 采用I/O多路复用的机制 调用runSelectLoop()，随时待命，当接收到请求创建新进程请求时立即唤醒并执行相应工作。
```
- System_server
    - 在执行StartSystemServer()方法创建完System_server进程后执行handSystemServerProcess()方法（system_server进程创建PathClassLoader类加载器）
    
    - 把 PathClassLoader通过参数传入方法 RuntimeInit.zygoteInit
        ```
        public static final void zygoteInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) throws ZygoteInit.MethodAndArgsCaller {
        
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "RuntimeInit");
            redirectLogStreams(); //重定向log输出
        
            commonInit(); // 通用的一些初始化
            nativeZygoteInit(); // zygote初始化
            applicationInit(targetSdkVersion, argv, classLoader); // [见小节3.4]
        }
        ```
       nativeZygoteInit最终调用app_main.cpp中的onZygoteInit()（Binder线程池的创建也是这个流程）
       applicationInit经过层层调用会抛出ZygoteInit.MethodAndArgsCaller(m,args),然后由ZygoteInit.main捕获异常
    - zygote.main方法调用
        ```
        public static void main(String argv[]) {
            try {
                startSystemServer(abiList, socketName); //抛出MethodAndArgsCaller异常
                ....
            } catch (MethodAndArgsCaller caller) {
                caller.run(); //此处通过反射,会调用SystemServer.main()方法 [见小节4.4]
            } catch (RuntimeException ex) {
                ...
            }
        }
        
        static class MethodAndArgsCaller implements Runnable {
            private final Method mMethod;
            private final String[] mArgs;
        
            public MethodAndArgsCaller(Method method, String[] args) {
                mMethod = method;
                mArgs = args;
            }
        
            public void run() {
                //执行SystemServer.main()
                mMethod.invoke(null, new Object[] { mArgs });
            }
        }
        ```
        ```
        public final class SystemServer {
            ...
            public static void main(String[] args) {
                //先初始化SystemServer对象，再调用对象的run()方法
                new SystemServer().run();
            }
        }
        ```
        ```
            private void run() {
                if (System.currentTimeMillis() < EARLIEST_SUPPORTED_TIME) {
                    Slog.w(TAG, "System clock is before 1970; setting to 1970.");
                    SystemClock.setCurrentTimeMillis(EARLIEST_SUPPORTED_TIME);
                }
                ...
            
                Slog.i(TAG, "Entered the Android system server!");
                EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_SYSTEM_RUN, SystemClock.uptimeMillis());
                Looper.prepareMainLooper();// 准备主线程looper
            
                //加载android_servers.so库，该库包含的源码在frameworks/base/services/目录下
                System.loadLibrary("android_servers");
            
                //检测上次关机过程是否失败，该方法可能不会返回
                performPendingShutdown();
                createSystemContext(); //初始化系统上下文
            
                //创建系统服务管理
                mSystemServiceManager = new SystemServiceManager(mSystemContext);
                LocalServices.addService(SystemServiceManager.class, mSystemServiceManager);
            
                //启动各种系统服务
                try {
                    startBootstrapServices(); // 启动引导服务
                    startCoreServices();      // 启动核心服务
                    startOtherServices();     // 启动其他服务[见小节4.6]
                } catch (Throwable ex) {
                    Slog.e("System", "************ Failure starting system services", ex);
                    throw ex;
                }
            
                //一直循环执行
                Looper.loop();
                throw new RuntimeException("Main thread loop unexpectedly exited");
            }
        ```