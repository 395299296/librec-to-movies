# librec-to-movies
librec application to movies recommender system
play framework version 1.4.x

# 基于LibRec实现的电影推荐系统

# 介绍
最近在学习推荐系统，了解了一些基本概念和方法，但是要想深入系统的学习，只有亲自动手实践才能体会推荐系统的各个环节，才能对推荐算法的优缺点有真切感受，正好有童鞋给我推荐了LibRec这个开源库，这是个很棒的推荐引擎，用Java开发包含70多种算法，而且很骄傲的说，作者们也都是中国人，据说国外名校的老师授课也用的这个项目，我也很荣幸的与他们开发团队的领袖郭贵冰教授取得了联系，于是我除了对他们的开源精神表示膜拜之外还决定基于LibRec实现一个电影推荐系统。在学习任何语言时都习惯先写个“Hello World”，我觉得电影推荐就是推荐系统的“Hello World”。

# 准备
LibRec是用Java开发，所以首先得安装JDK，当前版本需要JDK1.7或者更高；其次，我准备做的是一个Web系统，因此需要一个框架，但是之前没做过Java开发，也不知道用哪个框架好，于是去知乎上大致搜了下，综合利弊我选择了play framework，结果果然好用，跟Python的Flask同样简洁高效，关于play可以去它的官网上了解更多的信息，有非常详细的教程，并且一步一步教你怎么实现一个完整的博客系统，而且每个版本都有相应的文档，当前最新是2.6版本，我下载的是1.4版本，因为play2.x跟play1.x还有本质上的差别，简便稳定起见我还是选择用老版本，安装也很简单，从官网上下载1.4的安装包，直接解压到本地目录并且添加到环境变量里即可；最后，我们还需要一个开发工具，我下载的是Eclipse Java EE Mars 4.5.0版本。

# 创建项目
选中eclipse的workspace文件夹，当然也可以是其他目录，看个人喜好，按住shift键的同时右键，单击“在此处打开命令窗口”，然后命令行中输入以下命令并回车，中间会提示让你输入项目全名，可以直接回车
play new librec-demo
结果如下所示
![image](https://github.com/395299296/librec-to-movies/documentation/images/20170516142924.png)
这时我们的项目就已经创建好了，在命令行以下命令并回车

cd librec-demo
play run

等服务运行起来之后就可以打开浏览器输入http://127.0.0.1:9000/，因为加载的资源是国外的地址，所以打开会比较慢，但是效果还是不错的

# 引用LibRec
从GitHub下载LibRec源码，可以直接下载压缩包，但是为了后续更新方便，我直接git，通过Ctrl+C结束当前play运行，cd ..回到上层目录
git clone https://github.com/guoguibing/librec.git
等源码下载完毕，就可以将其导入到eclipse中，如图

Maven会自动下载LibRec所需的依赖包，可能需要几分钟的时间，因为我是挂着翻墙软件，所以速度还算可以，否则且得等一会
当LibRec导入完成之后，接下来我们就要导入刚才创建的play项目，但是play默认是不带eclipse项目工程文件的，别急，继续执行如下命令行
cd librec-demo
play eclipsify
然后正如所显示的那样，Use File/Import/General/Existing project to import G:\eclipse\workspace\librec-demo into eclipse
最后我们将LibRec以依赖项目的方式引用到librec-demo项目中，右击项目选择属性然后将librec-core添加到依赖项目列表中，请注意这样只是为了调试方便，项目发布的时候要改成依赖库的方式

# 测试LibRec
到此为止，基本的准备工作皆已完成，激动人心的时刻即将到来，打开Application.java文件，在index方法中写上一段LibRec官网的测试示例代码
Resource resource = new Resource("rec/cf/itemknn-test.properties");
Configuration conf = new Configuration();
conf.set("dfs.data.dir", "../librec/data");
conf.addResource(resource);
RecommenderJob job = new RecommenderJob(conf);
job.runJob();
然后右击librec-demo.launch选择Run As->librec-demo运行项目

这里说明三点，首先，设置数据目录为"../librec/data"是相对目录，根据具体使用路径不同而不同，也可以改成绝对路径；其次，play项目配置默认是开发模式，服务运行起来之后当有代码修改会立即生效可以不用再重启服务；然后，要调试的话右击Connect JPDA to librec-demo.launch选择Debug As->Connect JPDA to librec-demo就可以下断点调试了。
接着上面，当项目运行起来之后，刷新http://127.0.0.1:9000/，然后可以看到eclipse控制台多了如下打印信息
15:37:39,127 INFO  ~ Dataset: ../librec/data/filmtrust/rating
15:37:39,135 INFO  ~ All dataset files [..\librec\data\filmtrust\rating\ratings_0.txt, ..\librec\data\filmtrust\rating\ratings_1.txt, ..\librec\data\filmtrust\rating\ratings_2.txt, ..\librec\data\filmtrust\rating\ratings_3.txt]
15:37:39,135 INFO  ~ All dataset files size 411942
15:37:39,135 INFO  ~ Now loading dataset file ratings_0
15:37:39,224 INFO  ~ Now loading dataset file ratings_1
15:37:39,225 INFO  ~ Now loading dataset file ratings_2
15:37:39,274 INFO  ~ Now loading dataset file ratings_3
15:37:39,350 INFO  ~ Transform data to Convertor successfully!
15:37:39,387 INFO  ~ Split data to train Set and test Set successfully!
15:37:39,388 INFO  ~ Data size of training is 28408
15:37:39,388 INFO  ~ Data size of testing is 7086
15:37:39,717 INFO  ~ Job Setup completed.
15:37:39,720 INFO  ~ Job Train completed.
15:37:40,212 INFO  ~ Job End.
15:37:40,224 INFO  ~ Evaluator value:MSE is 0.6782123336223386
15:37:40,224 INFO  ~ Evaluator value:RMSE is 0.8235364798370128
15:37:40,224 INFO  ~ Evaluator value:MAE is 0.6239966614598033
15:37:40,224 INFO  ~ Evaluator value:MPE is 0.954417160598363
15:37:40,228 INFO  ~ Result path is ../result/filmtrust/rating-itemknn-output/itemknn
从打印日志看整个推荐过程清晰明了，非常自然，是不是很强大！

# 收集数据
要展示一个推荐的电影内容给大家，前提是得有数据，于是我从GitHub上找了一些别人用过的数据集，另外我还写了个爬虫获取了电影的图片及介绍，整理后的完整数据可以在这里（http://files.liaotian2020.com/data.zip）下载
解压到librec-demo根目录，预览内容如下所示

# 数据预处理
因为用户访问页面不会等，因此需要对数据进行预处理，把所有用户的推荐结果预先生成好，可以针对具体需要每天基于历史数据集生成一次，当用户访问的时候，在已有的推荐结果上进行一次筛选。play的预处理方法是添加Bootstrap这个类，然后在doJob方法中写我们的逻辑代码，当配置文件中的application.mode=prod时表示是生产模式，doJob会在服务启动的时候运行，而在开发模式下，只有当访问请求发起的时候才会运行。

# 添加页面
后台逻辑其实还是比较简单，大量的代码已经由LibRec实现了，我们只是做一点数据维护与接口调用的事情，比较繁琐的还是前端页面，由于我并没有正式的开发过网站，对于CSS跟JavaScript只是看别人写好的网站看懂的，鉴于时间关系，我们直接下载一个已有的网站拿来改，网上有太多精美绝伦的网站，相信大家一定能找得到中意的，然后用Teleport这样的整站下载器下载前端全部源码及资源，再根据自身需求进行调整，大致的框架不变的话，只是做一些小修改，但是后来发现有个问题，用他的源码改出来的页面对于手机浏览器不适应，没办法，天下没有免费的午餐，还得将前端页面重构一遍，我用了Bootstrap——简洁、直观、强悍的前端开发框架，让web开发更迅速、简单。果真如其所说，相当强悍，浏览器的兼容问题完全不用考虑，他都已经帮你实现，我们要做的无非就是从他的官网下载源码，然后放到我们的项目中

main.css是我们自定义的样式表，然后按他布局方式，控件用法写，而且官网有很多案例，看看源码就大致清楚怎么用了，如下是切换到手机页面的预览效果

# 后台实现
当前端页面实现之后，我们根据功能模块在routes文件添加如下路由规则
GET     /                        Application.index
GET     /logout                  Application.logout
GET     /login                   Application.login
GET     /movies                  Application.movie_list
GET     /movies/{movie_id}       Application.show_movie
GET     /movies_recent/{index}   Application.show_more_movies
GET     /users/{user_id}         Application.show_user
POST    /logged_in               Application.is_logged_in
POST    /new_rating              Application.add_new_rating
前面是访问地址，后面是所映射的控制器接口，比如我们在Application的index方法添加如下逻辑，访问主页的时候会自动响应index方法

这里我们为主页传递了三个数据列表参数，一个是推荐的电影列表，一个是最近上映的电影列表，一个是评分排名列表，而所推荐的电影列表又分为用户未登陆的默认列表跟登陆后的推荐列表，推荐算法我用的是ItemKNNRecommender。

整个项目的开发流程大致就是如此，实现细节不再赘述，完整代码可以从这里（https://github.com/395299296/librec-to-movies.git）下载。
