package androidtest.project.com.spi_study;

import android.app.Application;

import androidtest.project.com.apt_library.AppInitTools;

/**
 * @author liuboyu  E-mail:545777678@qq.com
 * @Date 2019-08-24
 * @Description 自定义 Application
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppInitTools.getInstance().initAllModuelApplication(this);
    }
}
