## Android 动态服务SPI

### 一、什么是SPI
SPI即Service Provider Interfaces。Java的接口可以有多种实现方式，为便于代码灵活，有时需要动态加载实现类。这就是SPI机制. SPI机制非常简单, 步骤如下:

1. 定义接口和接口的实现类
2. 创建resources/META-INF/services目录
3. 在上述Service目录下，创建一个以接口名(类的全名) 命名的文件, 其内容是实现类的类名 (类的全名)。
>在services目录下创建的文件是androidtest.project.com.aninterface.AppInit 文件中的内容为AAppInit接口的实现类, 可能是androidtest.project.com.sharemodule.ShareApplicationInit
4. 在java代码中使用ServcieLoader来动态加载并调用内部方法.

### 二、在Android中能干嘛？
随着自己开发的应用的版本迭代，新功能不断增多，随之引入的第三方库也不可避免地多了起来，你可能就会发现自己应用Application中各种框架的初始化代码也在逐渐臃肿起来：什么推送啦，分享啦，统计啦，定位啦...另外还有你自己封装的一些工具和框架。这些七七八八加起来，可能最终你的Application会变成这样：
```
/**
 * @author liuboyu  E-mail:545777678@qq.com
 * @Date 2019-08-24
 * @Description 自定义 Application
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化推送
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
            //注册成功会返回device token
            }

            @Override
            public void onFailure(String s, String s1) {
            }
        });
        //初始化统计
        UMConfigure.init(this, "xxxxxxxxxxxxxx", "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        //初始化分享
        PlatformConfig.setWeixin("xxxxxxxxxxxxxx", "xxxxxxxxxxxxxx");
        PlatformConfig.setSinaWeibo("xxxxxxxxxxxxxx", "xxxxxxxxxxxxxx", "http://sns.whalecloud.com");
        PlatformConfig.setYixin("xxxxxxxxxxxxxx");
        PlatformConfig.setQQZone("xxxxxxxxxxxxxx", "xxxxxxxxxxxxxx");
        PlatformConfig.setTwitter("xxxxxxxxxxxxxx", "xxxxxxxxxxxxxx");
        //初始化定位
        LocationClient mLocationClient = new LocationClient(context);
        mLocationClient.setLocOption(getLocationOption());
        mLocationClient.registerLocationListener(new MyLocationListener());
        mLocationClient.start();
        mLocationClient.requestLocation();
        //初始化glide
        DisplayOption options = DisplayOption.builder().setDefaultResId(R.drawable.ic_default)
                .setErrorResId(-1).setLoadingResId(-1);
        imageDisplayLoader.setDefaultDisplayOption(options);
        //初始化自己的一些工具
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

        });
    }
}
```
从个人开发经验上说，这些应用程序级别的框架，作用的时间贯穿APP的整个生命周期，所以都会要求你在一开始的时候就进行初始化

### 二、优化它，盘它！
SPI在平时我们用到的会比较少，但是在Android模块开发中就会比较有用，不同的模块可以基于接口编程，每个模块有不同的实现service provider,然后通过SPI机制自动注册到一个配置文件中，就可以实现在程序运行时扫描加载同一接口的不同service provider。这样模块之间不会基于实现类硬编码，可插拔。
我们试着通过 SPI 的方式解决这个问题
#### 创建项目
- 创建Android Module命名为app 依赖 spi_library,shareModule,pushModule
- 创建Android library Module 命名为 interface
- 创建Android library Module 命名为 shareModule 依赖 interface
- 创建Android library Module 命名为 pushModule 依赖 interface
- 创建Android library Module 命名为 spi_library 依赖 interface

#### Module职责
- interface：接口模块
- spi_library：spi 工具类，一键初始化
- shareModule：share模块
- pushModule：push模块

#### 实现
##### 1、创建接口
```
/**
 * @author liuboyu  E-mail:545777678@qq.com
 * @Date 2019-08-24
 * @Description Application 初始化接口
 */
public interface AppInit {
    void applicationInit(@NonNull Application application);
}
```

##### 2、子模块实现自己的功能，并在自己模块声明自己的配置文件
###### share 模块
```
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
```

配置文件内容
```
androidtest.project.com.sharemodule.ShareApplicationInit
```

###### push 模块
```
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
```
配置文件内容
```
androidtest.project.com.pushmodule.PushApplicationInit
```

##### 3、通过ServiceLoader来加载接口
关键的代码其实就是下面几行，通过ServiceLoader来加载接口的不同实现类，然后会得到迭代器，在迭代器中可以拿到不同实现类全限定名，然后通过反射动态加载实例就可以调用applicationInit方法了。
```
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
```
##### 4、项目初始化
我们仅仅通过一行代码便可实现各个模块的初始化操作，如果后续引入其他的模块，比如bugly啊，统计啊，定位啊，直接新建子模块初始化自己的application，并声明配置文件即可，对于主工程完全无感址，
也就达到了 **模块之间不会基于实现类硬编码，可插拔** 的目的。
```
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
```

看一下运行结果
```
08-24 16:30:51.481 1387-1387/androidtest.project.com.spi_study E/spi_study: share模块初始化
08-24 16:30:51.482 1387-1387/androidtest.project.com.spi_study E/spi_study: push模块初始化
```

### 三、源码分析
主要工作都是在ServiceLoader中，这个类在java.util包中。先看下几个重要的成员变量:
- PREFIX就是配置文件所在的包目录路径；
- service就是接口名称，在我们这个例子中就是AppInit；
- loader就是类加载器，其实最终都是通过反射加载实例；
- providers就是不同实现类的缓存，key就是实现类的全限定名，value就是实现类的实例；
- lookupIterator就是内部类LazyIterator的实例。

```
private static final String PREFIX = "META-INF/services/";

// The class or interface representing the service being loaded
private Class<S> service;

// The class loader used to locate, load, and instantiate providers
private ClassLoader loader;

// Cached providers, in instantiation order
private LinkedHashMap<String,S> providers = new LinkedHashMap<>();

// The current lazy-lookup iterator
private LazyIterator lookupIterator;
```
上面提到SPI的几个关键步骤：
```
ServiceLoader<AppInit> loader = ServiceLoader.load(AppInit.class);
Iterator<AppInit> mIterator = loader.iterator();
while (mIterator.hasNext()) {
    mIterator.next().applicationInit(application);
}
```
先看下第一个步骤load：ServiceLoader提供了两个静态的load方法,如果我们没有传入类加载器，ServiceLoader会自动为我们获得一个当前线程的类加载器，最终都是调用构造函数。
```
public static <S> ServiceLoader<S> load(Class<S> service) {
        ClassLoader cl =Thread.currentThread().getContextClassLoader();
        return ServiceLoader.load(service, cl);
}

public static <S> ServiceLoader<S> load(Class<S> service,ClassLoader loader)
{
        return new ServiceLoader<>(service, loader);
}
```
在构造函数中工作很简单就是清除实现类的缓存，实例化迭代器。
```
public void reload() {
    providers.clear();
    lookupIterator = new LazyIterator(service, loader);
}

private ServiceLoader(Class<S> svc, ClassLoader cl) {
    service = svc;
    loader = cl;
    reload();
}

private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
}
```

**注意了，我们在外面通过ServiceLoader.load(Display.class)并不会去加载service provider，也就是懒加载的设计模式，这也是Java SPI的设计亮点。**

那么service provider在什么地方进行加载？我们接着看第二个步骤loader.iterator(),其实就是返回一个迭代器。我们看下官方文档的解释,这个就是懒加载实现的地方，首先会到providers中去查找有没有存在的实例，有就直接返回，没有再到LazyIterator中查找。
```
* Lazily loads the available providers of this loader's service.
```

```
*
* <p> The iterator returned by this method first yields all of the
* elements of the provider cache, in instantiation order.  It then lazily
* loads and instantiates any remaining providers, adding each one to the
* cache in turn.
```

```
public Iterator<S> iterator() {
        return new Iterator<S>() {

            Iterator<Map.Entry<String,S>> knownProviders
                = providers.entrySet().iterator();

            public boolean hasNext() {
                if (knownProviders.hasNext())
                    return true;
                return lookupIterator.hasNext();
            }

            public S next() {
                if (knownProviders.hasNext())
                    return knownProviders.next().getValue();
                return lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
}
```

我们一开始providers中肯定是没有缓存的实例的，接着会到LazyIterator中查找，去看看LazyIterator,先看下hasNext()方法。
>1. 首先拿到配置文件名fullName,我们这个例子中是com.example.Display
>2. 通过类加载器获得所有模块的配置文件Enumeration<URL> configs configs
>3. 依次扫描每个配置文件的内容，返回配置文件内容Iterator<String> pending，每个配置文件中可能有多个实现类的全限定名，所以pending也是个迭代器。

```
public boolean hasNext() {
    if (nextName != null) {
        return true;
    }
    if (configs == null) {
        try {
            String fullName = PREFIX + service.getName();
            if (loader == null)
                configs = ClassLoader.getSystemResources(fullName);
            else
                configs = loader.getResources(fullName);
        } catch (IOException x) {
            fail(service, "Error locating configuration files", x);
        }
    }
    while ((pending == null) || !pending.hasNext()) {
        if (!configs.hasMoreElements()) {
            return false;
        }
        pending = parse(service, configs.nextElement());
    }
    nextName = pending.next();
    return true;
}
```

在上面hasNext()方法中拿到的nextName就是实现类的全限定名，接下来我们去看看具体实例化工作的地方next():

>1. 首先根据nextName，Class.forName加载拿到具体实现类的class对象
>2. Class.newInstance()实例化拿到具体实现类的实例对象
>3. 将实例对象转换service.cast为接口
>4. 将实例对象放到缓存中，providers.put(cn, p)，key就是实现类的全限定名，value是实例对象。
>5. 返回实例对象

```
public S next() {
    if (!hasNext()) {
        throw new NoSuchElementException();
    }
    String cn = nextName;
    nextName = null;
    Class<?> c = null;
    try {
        c = Class.forName(cn, false, loader);
    } catch (ClassNotFoundException x) {
        fail(service,
             "Provider " + cn + " not found", x);
    }
    if (!service.isAssignableFrom(c)) {
        ClassCastException cce = new ClassCastException(
                service.getCanonicalName() + " is not assignable from " + c.getCanonicalName());
        fail(service,
             "Provider " + cn  + " not a subtype", cce);
    }
    try {
        S p = service.cast(c.newInstance());
        providers.put(cn, p);
        return p;
    } catch (Throwable x) {
        fail(service,
             "Provider " + cn + " could not be instantiated: " + x,
             x);
    }
    throw new Error();          // This cannot happen
}
```

### 四、总结

通过Java的SPI机制也有一点缺点就是在运行时通过反射加载类实例，这个对性能会有点影响。但是瑕不掩瑜，SPI机制可以实现不同模块之间方便的面向接口编程，拒绝了硬编码的方式，解耦效果很好。用起来也简单，只需要在目录META-INF/services中配置实现类就行。
文中的栗子已上传github，有需要的可以参考：[github demo链接](https://github.com/Bob-liuboyu/SPI_Study)



