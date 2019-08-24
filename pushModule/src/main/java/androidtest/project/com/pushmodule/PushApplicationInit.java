package androidtest.project.com.pushmodule;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import androidtest.project.com.aninterface.AppInit;


/**
 * @author liuboyu  E-mail:545777678@qq.com
 * @Date 2019-08-24
 * @Description push模块初始化操作
 */
public class PushApplicationInit implements AppInit {

    @Override
    public void applicationInit(@NonNull Application application) {
        Log.e("spi_study", "push模块初始化");
    }
}
