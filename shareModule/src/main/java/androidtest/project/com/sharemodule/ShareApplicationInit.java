package androidtest.project.com.sharemodule;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import androidtest.project.com.aninterface.AppInit;


/**
 * @author liuboyu  E-mail:545777678@qq.com
 * @Date 2019-08-24
 * @Description share模块初始化
 */
public class ShareApplicationInit implements AppInit {
    @Override
    public void applicationInit(@NonNull Application application) {
        Log.e("spi_study", "share模块初始化");
    }
}
