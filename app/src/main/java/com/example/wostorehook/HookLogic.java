package com.example.wostorehook;

import android.content.Context;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author DX
 * 注意：该类不要自己写构造方法，否者可能会hook不成功
 * 开发Xposed模块完成以后，建议修改xposed_init文件，并将起指向这个类,以提升性能
 * 所以这个类需要implements IXposedHookLoadPackage,以防修改xposed_init文件后忘记
 * Created by DX on 2017/10/4.
 */

public class HookLogic implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private final static String modulePackageName = HookLogic.class.getPackage().getName();
    private XSharedPreferences sharedPreferences;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if ("com.sinovatech.unicom.ui".equals(loadPackageParam.packageName)) {
            XposedHelpers.findAndHookMethod("com.secneo.apkwrapper.AW", loadPackageParam.classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    //获取到 Context 对象，通过这个对象来获取 classloader
                    Context context = (Context) param.args[0];
                    //获取 classloader，之后 hook 加固后的就使用这个 classloader
                    ClassLoader classLoader = context.getClassLoader();
                    //下面就是将 classloader 修改成壳的 classloader 就可以成功的 hook 了

                    Class clazz = XposedHelpers.findClass("com.wg.android.backend.GoBackend", null);
                    Method method = XposedHelpers.findMethodExact(clazz, "wgGetConfig", int.class);
                    method.setAccessible(true);
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedBridge.log("config:" + param.getResult());
                        }
                    });
                }
            });
        }
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        this.sharedPreferences = new XSharedPreferences(modulePackageName, "default");
        XposedBridge.log(modulePackageName + " initZygote");
    }
}
