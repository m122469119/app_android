#比价模块逻辑文档

| 日期   |   作者 |   变更  |
|------ | --------| ------ |
| 2016/02/26  |   徐生福 |  比价模块  |
| 2016/03/10  |   徐生福 |  比价模块变MD  |

## 页面

## 比价模块	
 - 	[PriceActivityNew比价](#jump1)
 - 	[CinemaActivityNew 城市选择列表](#jump2)
 - 	[SearchActivity 搜索页面](#jump3)
 - 	[SearchResultActivity 搜索结果页面](#jump4)



## <span id="jump1">PriceActivityNew比价</span>

 `PriceActivityNew.java` 和`PriceFragment_New.java`逻辑和代码一样，区别只是主页(`PriceFragment_New`)和二级页面(`PriceActivityNew`)的区别。

此页面的逻辑是：首先加载影片的数据，影片数据加载完成后定位当前位置最近的影院。选择第一个影院，当前影片在该影院七天的场次。

- `setLayoutView()` 设置`Activity`的布局。

- `initVariable()` 初始化数据，接受传过来的参数。

- `findViews()` 找出控件和初始化控件。

- `setListener()` 设置控件的监听事件。

- `initData()`所有数据开始加载的方法。下拉刷新也调用此方法。

- `init()`从接口加载`Gallery`数据方法。

- `initPosterFilm()` `Gallery` 数据加载好后刷新数据的方法，并且定位所选的影片，触发`Gallery`的`Selection`方法。

- `initLocal()` 获取定位当前位置的第一个影院。

- `initCinemaFristData()`从接口获取当前位置的一个影院.

- `onActivityResult()` 当换影院的时候，点击切换回传影院的参数在这处理。

- `MyHandler`当影片和影院都加载好就触发`Handler`加载当天的场次。

- `InitTitlePager()` 初始化七天日期显示的布局和`RecyclerView` 

- `initprceData()` 从接口加载七天的场次，`HashMap`做缓存。



## <span id="jump2">CinemaActivityNew 城市选择列表</span>	

此页面的逻辑是：首先加载城市的数据并缓存，下次先读取城市缓存数据，如果不存在在从接口加载,获取到城市数据后，解析后把地区和城市存到一个对象中，

首次进去加载定位到的城市，如果没有定位到加载北京数据。

- `setLayoutView()` 设置`Activity`的布局。

- `initVariable()` 初始化数据，接受传过来的参数。

- `findViews()` 找出控件和初始化控件。

- `setListener()` 设置控件的监听事件。

- `initData()`所有数据开始加载的方法。

- `InitTitlePager()` 初始化七天日期显示的布局

- `initPopupCityWindow()` 初始化城市`popupWindow`.
 
- `initPopupRegionWindow()` 初始化地区`popupWindow`

- `initCinemaData()` 加载七天日期显示的布局

- `initDataCity()` 从接口加载城市数据

- `initCityView()` 用数据填充城市控件

- `initRegionView()` 从数据筛选地区数据并填充地区控件

- `loadData(boolean isFirst, int position)` 从接口加载影院列表数据。

- `endRefresh()` 上拉加载更多方法


## <span id="jump3">SearchActivity 搜索页面</span>	

此页面的逻辑是：首先加载热词的数据，并查询数据库看有对应的搜索历史没，有数据就显示三条。

- `setLayoutView()` 设置`Activity`的布局。

- `initVariable()` 初始化数据，接受传过来的参数。

- `findViews()` 找出控件和初始化控件。

- `setListener()` 设置控件的监听事件。

- `initData()`所有数据开始加载的方法。

- `initFilm()` 从接口获取电影的热词

- `initReview()`从接口获取影评的热词

- `initAuthor()`从接口获取影评人的热词

- `DeleteData()` 删除数据库对应的搜索历史

- `Refused(int i)` 刷新搜索历史 `i` 代表最多显示几条



## <span id="jump4">SearchResultActivity 搜索结果页面</span>	

此页面的逻辑是：接收到传递的热词的数据，并从接口加载数据并显示。

- `setLayoutView()` 设置`Activity`的布局。

- `initVariable()` 初始化数据，接受传过来的参数。

- `findViews()` 找出控件和初始化控件。

- `setListener()` 设置控件的监听事件。

- `initData()`所有数据开始加载的方法。

- `loadInit()` 初始化电影搜索所需的`adapter`

- `loadReviewInit()` 初始化影评搜索所需的`adapter`

- `loadAuthorInit()` 初始化影评人搜索所需的`adapter`

- `loadFilmData()`从接口加载电影所需的数据

- `loadReviewData()` 从接口加载影评所需的数据

- `loadAuthorData()`从接口加载影评人所需的数据

- `endRefresh()`上拉加载更多方法

