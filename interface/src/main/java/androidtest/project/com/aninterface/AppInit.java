package androidtest.project.com.aninterface;

import android.app.Application;
import android.support.annotation.NonNull;

/**
 * @author liuboyu  E-mail:545777678@qq.com
 * @Date 2019-08-24
 * @Description Application 初始化接口
 */
public interface AppInit {
    void applicationInit(@NonNull Application application);
}