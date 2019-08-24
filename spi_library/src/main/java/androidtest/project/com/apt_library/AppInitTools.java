package androidtest.project.com.apt_library;

import android.app.Application;
import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.ServiceLoader;

import androidtest.project.com.aninterface.AppInit;


/**
 * @author liuboyu  E-mail:545777678@qq.com
 * @Date 2019-08-24
 * @Description application 初始化工具类
 */
public class AppInitTools {
    private static AppInitTools manager;

    private AppInitTools() {

    }

    public static AppInitTools getInstance() {
        if (manager == null) {
            synchronized (AppInitTools.class) {
                if (manager == null) {
                    manager = new AppInitTools();
                }
            }
        }
        return manager;
    }


    /**
     * 初始化各个moduel的Application
     * @param application
     */
    public void initAllModuelApplication(@NonNull Application application) {
        ServiceLoader<AppInit> loader = ServiceLoader.load(AppInit.class);
        Iterator<AppInit> mIterator = loader.iterator();
        while (mIterator.hasNext()) {
            mIterator.next().applicationInit(application);
        }
    }
}
