# 晒票模块逻辑文档

## 页面

1. 晒票显示主页的`fragment` `ShareTicketFragment.java -> SharedTicketViewController.java)`
2. 热门标签晒票显示页面 `TagSharedTicketActivity.java`
3. 晒票详情页面 `SharedTicketDetailActivity.java`
4. 晒票图片显示页面 `SharedTicketOnlyImageActivity`
5. 晒票页面 `TicketShareActivity`
6. 附近页面 `AroundSharedTicketActivity`


## BaseViewCtrlActivity

因为这几个页面的`Activity` 继承了 `BaseViewCtrlActivity`, 所以简单了解一下 `BaseViewCtrlActivity`, 
`BaseViewCtrlActivity` 主要完成这几个功能:

- 如果系统API > 19, 透明系统状态栏, 如果透明了系统状态栏, `mStatusBarTranslucentFlag` 标志则设置为 `true`
在 `BaseContentViewController` 中, 可以控制是否透明系统状态栏, 或者是否透明状态栏并显示自己的定制的状态栏

- `BaseViewCtrlActivity` 记录了用户上一次的状态, 如果用户在其他地方登陆回来后, 
如果登陆状态改变，就会回调 `onUserStateChanged(UserState oldState)` 方法, 你可以重写此方法

- `BaseContentViewController` 是用来控制整个 Activity 页面的 view， 他会在构造函数里面 `setContentView()`
主要有`setOnClickListener(int id)` `setFinishView(int id)` 几个简便方法用来设置监听 和 关闭按钮

- 重写了 `startActivity()` 方法, 设置 `mStartFlag = true` 防止快速点击时, 连续启动两次activity,
在 `onResume()` 里面重新将 `mStartFlag` 设置为 `false`.


## 晒票主页 SharedTicketFragment

在 `SharedTicketFragment` 里面只负责在进行晒票操作时在 `onActivityResult()` 里面跳到晒票页面, 
整个页面的逻辑放在了 `SharedTicketViewController` 里面, 里面的逻辑也比较简单, 一个 HorizontalListView,
一个 RecyclerView, 晒票的数据放在了 MainSharedTicketDataManager 里面, 当用户状态发生变化时, 会刷新相应的数据.


## 标签详情页面 TagSharedTicketActivity

逻辑和 SharedTicketFragment 是一样的


## 晒票详情页面 SharedTicketDetailActivity

启动 `SharedTicketDetailActivity` 需要传入晒票数据
- 晒票的 ID (long类型) 使用 Intent 传递, 或者 
- `NetworkManager.SharedTicketRespModel`的模型数据, 使用 CommonManager传递

整个页面都是一个 RecyclerView, 评论列表以上的布局都是 RecyclerView 的头部


## 晒票大图显示 SharedTicketOnlyImageActivity

通过 Intent 传入图片的 url, 显示图片, 长按图片 保存图片


## 周围晒票 AroundSharedTicketActivity

周围晒票一开始会进行定位, 如果定位失败, 就会去SharePreference里面获取上一次成功定位的数据, 
如果没有得到数据, 则显示未知位置. 定位成功后, 去获取晒票数据, 将获取到的数据模型, 封装成 `AroundSharedTicket`
其实整个显示周围的地图界面就是一个 FrameLayout, 每个 AroundSharedTicket 都是一个头像布局,
随机生成位置并把布局放进 FrameLayout 里面.


限定了一个页只能放 9 个, 每次请求的数据为20个, 则会被分成 9, 9, 2, 第三页只有2个, 会再去请求一次数据, 
如果请求到了就凑满 9 个, 如果没有数据了, 或者请求失败了, 就会进行循环, 就是从第一页里面拿 7个数据凑满一页! 
