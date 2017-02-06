新年好！新的一年，新的征程！小伙伴们，继续奋斗...
这两天看了一些关于"手势"的文章，想记录下学到的一些知识点，慢慢积累...大神可以绕道了!
***
准备开船中...扬帆起航ing...
***

##### 具体实现案例：图片根据手势的开合进行放大与缩小，双击放大与缩小，以及放大后平移功能，具体看效果，见下图

![效果图.gif](http://upload-images.jianshu.io/upload_images/3150565-01454ff7d4f837ed.gif?imageMogr2/auto-orient/strip)

#### 一 .  具体分析
* 想要进行图片的放大与缩小，起码要知道图片什么时间加载完毕吧，这里就要用到一个监听（OnGlobalLayoutListener：实现这个接口即可） 用来监听ImageView加载图片完毕 ；
**注意**：此监听有的小伙伴可能在Activity的onCreate方法中为了获得控件的宽高用过，对了，就是它，来监听ViewTree的变化，但是使用时需要在onAttachedToWindow中注册监听，在onDetachedFromWindow中移除监听，具体实现看下面代码；
* 图片缩放要以手指触控的中心点进行缩放，并且缩小时需要处理边界问题，必须保证图片居中显示；这里就需要用到Matrix这个类和ScaleGestureDetector这个类；
下面先解释下Matrix这个类的使用方法：
  1. Matrix内部的值本质是个float类型的数组，为3*3的一维数组(float[9])，具体的含义为：
mScale_X      mSkew_X     mTrans_X  这三个值分别为：x轴缩放因子  x轴倾斜  x轴平移
mSkew_Y      mScale_Y    mTrans_Y  这三个值分别为：y轴倾斜  y轴缩放因子  y轴平移
MPERSP_0   MPERSP_1   MPERSP_2
在具体使用时，其实我们没有必要构建这个float[9]的数组，使用Matrix提供的api即可进行平移缩放旋转等，具体方法为（postScale,postTranslate,postRotate等）；注意：post后记得调用setImageMatrix（Matrix matrix）方法即可，具体实现看下面代码；
  2. ScaleGestureDetector这是类，是android用来专门处理多指触控的，里面有个OnScaleGestureListener内部接口，只需重写其两个参数的构造器的函数即可；OnScaleGestureListener这个接口具体实现有三个方法，切记在onScaleBegin中必须返回true，才会进入onScale()方法， 否则多指触控一直调用onScaleBegin方法 不会调用onScale和 onScaleEnd方法，具体的请看下面的代码；**注意:想要把事件传递给多指触控，需要在onTouch方法中调用mScaleGestureDetector.onTouchEvent(event)并返回true；**具体请看最下面附属的完整代码；

~~~
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		//注册onGlobalLayoutListener
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		//移除onGlobalLayoutListener
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * 捕获图片加载完成事件 onMeasure 和onDraw都不适合
	 */
	@Override
	public void onGlobalLayout() {
		//初始化的操作 一次就好  为了保证对缩放只进行一次
		if(!mOnce){
			
			//得到控件的宽和高--不一定是屏幕的宽和高 可能会有actionBar等等
			int width = getWidth() ;
			int height = getHeight(); 
			
			//得到我们的图片 以及宽和高
			Drawable drawable = getDrawable();
			if(drawable == null){
				return ;
			}
			/**
			 * 这里说下Drawable这个抽象类，具体实现类为BitmapDrawable
			 * BitmapDrawable这个类重写了getIntrinsicWidth()和getIntrinsicHeight()方法
			 * 这两个方法看字面意思就知道是什么了，就是得到图片固有的宽和高的
			 */
			int intrinsicWidth = drawable.getIntrinsicWidth();
			int intrinsicHeight = drawable.getIntrinsicHeight();
			Log.e("SCALE_IMAGEVIEW", intrinsicWidth+":intrinsicWidth");
			Log.e("SCALE_IMAGEVIEW", intrinsicHeight+":intrinsicHeight");
			// 如果图片宽度比控件宽度小  高度比控件大 需要缩小
			float scale = 1.0f ;//缩放的比例因子
			if(width>intrinsicWidth && height<intrinsicHeight){
				scale = height*1.0f/intrinsicHeight ;
			}
			// 如果图片比控件大 需要缩小
			if(width<intrinsicWidth && height>intrinsicHeight){
				scale = width*1.0f/intrinsicWidth ;
			}
			
			if((width<intrinsicWidth && height<intrinsicHeight) || (width>intrinsicWidth&&height>intrinsicHeight)){
				scale = Math.min(width*1.0f/intrinsicWidth, height*1.0f/intrinsicHeight);
			}
			
			/**
			 * 得到初始化缩放的比例
			 */
			mInitScale = scale ;
			mMidScale = 2*mInitScale ;//双击放大的值
			mMaxScale = 4*mInitScale ;//放大的最大值
			
			//将图片移动到控件的中心
			int dx = width/2 - intrinsicWidth/2 ;
			int dy = height/2 - intrinsicHeight/2 ;
			//将一些参数设置到图片或控件上 设置平移缩放 旋转
			mMatrix.postTranslate(dx, dy);
			mMatrix.postScale(mInitScale, mInitScale, width/2, height/2);//以控件的中心进行缩放
			setImageMatrix(mMatrix);
			
			mOnce = true ;
		}
	}
~~~
记录下从上面的代码中自己感觉的疑难点：
1. **Drawable是个抽象类，具体实现类为BitmapDrawable，这个类重写了getIntrinsicWidth()和getIntrinsicHeight()方法，这两个方法看字面意思就知道是什么了，就是得到图片固有的宽和高的**；
2.  ** 为了控制图片缩小时边界让图片实时居中显示，需要得到放大之后图片的宽高以及left top right bottom等值；因为我们已经有Matrix，使用Matrix，即可得到 ，请看如下代码**

~~~
	/**
	 * 获得图片放大或缩小之后的宽和高 以及 left top right bottom的坐标点，
     * 通过rect.width rect.height rect.top rect.left rect.right rect.bottom  即可得到想要的值
	 * @return
	 */
	private RectF getMatrixRectF(){
		Matrix matrix = mMatrix ;
		RectF rect = new RectF();
		Drawable drawable = getDrawable();
		if(null!=drawable){
			rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			matrix.mapRect(rect);
		}
		return rect ;
	}
~~~

* 既然要缩放，那就要知道本次在上次的基础上缩放的比例，因此需要首先知道图片已经缩放的比例；得到图片的缩放值后，就需要在ScaleGestureDetector的内部接口OnScaleGestureListener的onScale方法中处理缩放逻辑，具体实现请看下面代码：

~~~
	/**
	 * 获取图片当前的缩放值
	 * @return
	 */
	public float getScale(){
		float[] values = new float[9];
		mMatrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	//缩放区间 initScale --- maxScale
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale() ;
		//捕获用户多指触控时系统计算缩放的比例---因为有缩放区间，所以需要添加区间判断逻辑
		float scaleFactor = detector.getScaleFactor();
		Log.e("ScaleGestrueDetector", "scaleFactor:"+scaleFactor);
		if(getDrawable()==null){
			return true;
		}
		//最大最小控制
		if((scale<mMaxScale&&scaleFactor>1.0f)||(scale>mInitScale&&scaleFactor<1.0f)){
			if(scale*scaleFactor > mMaxScale){
				scaleFactor = mMaxScale/scale ;
			}
			if(scale*scaleFactor < mInitScale){
				scaleFactor = mInitScale/scale ;
			}
			mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
			//不断检测 控制白边和中心位置
			checkBorderAndCenterWhenScale();
			setImageMatrix(mMatrix);
		}
		return true;
	}
~~~
  **注意：float scaleFactor = detector.getScaleFactor() 这个方法得到的是"用户多指触控时系统根据手势计算出缩放的比例因子，得到此缩放因子后，需要乘以图片现在的缩放比例，看是否在缩放区间；detector.getFocusX(), detector.getFocusY()得到多指触控的中心的x,y坐标，用来指定缩放的中心点"**
* 双击放大与缩小功能，需要重写GestureDetector类的两个参数的构造函数，第二个参数为OnGestureListener,具体实现类为SimpleOnGestureListener，只需要重写onDoubleTap（）方法即可；
**注意：需要在onTouch()方法最上面通过此代码mGestureDetector.onTouchEvent(event)传递给GestureDetector类进行双击控制，具体请看最先面附属的完整代码**

#### 二.  具体在xml中的实现如下：
~~~
    <com.serenity.view.ScaleImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="matrix"
        android:src="@drawable/scene1" />
~~~
由于自定义的ImageView使用了Matrix，需要在xml中配置scaleType，其实不配置也行，本人在自义定的ImageView的构造函数中调用了setScaleType(ScaleType.MATRIX)方法，不管在xml怎么配置都会在代码中将scaleType设置为matrix类型；

#### 三. 由于此demo自定义的ImageView放在了ViewPager中，当图片放大了，左右滑动时会和ViewPager手势冲突，需要处理，本例子中的冲突在onTouch中做的处理，可以看下；其实处理这种冲突很简单，只需要分析出冲突在哪里，就在哪里进行处理即可；冲突一般有三种情况：
    1.外部滑动方式与内部滑动方式不一样.
    2.外部滑动方式与内部滑动方式一致.
    3.上面两种情况的嵌套.
处理冲突的原则：
**a.对于上面的第一种情况：**
记录上次记录点减去当前点得到deltaX,deltaY

    可以利用滑动路径和水平方向所形成的夹角来确定是那种滑动,如果小于45°,那自然就是横向,大于就是纵向.
    可以对比横向滑动距离和纵向滑动距离,那个大就是那个方向滑动距离大.

**b.对于第二,三种情况**

可以 根据业务写出处理规则, 比如当内部View滑动到顶部或者底部时响应外部View,我们就可以根据这个规则判断内部View有没有滑动到底, 如果有的话就不消费事件,没有的话就消费事件.具体怎么消费事件有**两种方法**.
#####1.外部拦截法
所有的事件都要经由decorView分发,所以我们可以在decorView处做文章
如果父View需要事件,就拦截事件;否则就不拦截事件.具体实现在onInterceptTouchEvent()中处理.
~~~
public boolean onInterceptTouchEvent(MotionEvent event){
    boolean interceptd = false;
    //获取当前动作所在点
    int x = (int) event.getX();
    int y = (int) event.getY();
    switch(event.getAction()){
        case MotionEvent.ACTION_DOWN:
        //默认不拦截ACTION_DOWN,因为父View一旦拦截ACTION_DOWN,那么这个系列的事件都会交由它处理.
            interceptd = false;
            break;
        case MotionEvent.ACTION_MOVE:
            if(父容器需要当前点击事件){
                interceptd = true;
            }else{
                interceptd = false;
            }
            break;
        case MotionEvent.ACTION_UP:
            //默认不拦截ACTION_UP,因为子View如果响应当前系列事件没有ACTION_UP的话无法触发onClick()方法
            interceptd = false;
            break;
        default:
            break;
    }
    //保存最后一个拦截点
    mLastXIntercept = x;
    mLastYIntercept = y;
    return interceptd;
}
~~~
#####2.内部拦截法
父容器默认不拦截任何事件,所有事件都交由子元素,子元素不需要再requestDisallowInterceptTouchEvent(boolean)操控父元素处理,和上面的方法正好相反.
~~~
public boolean dispatchTouchEvent(MotionEvent event){
    //获取当前点位置
    int x = (int) event.getX();
    int y = (int) event.getY();
    switch(event.getAction()){
        case MotionEvent.ACTION_DOWN:
            /**
            *操控父元素不拦截ACTION_DOWN,因为ACTION_DOWN不受 ACTION_DISALLOW_INTERCEPT 标记控制,
            *所以一旦父元素拦截ACTION_DOWN,这个事件系列都会被交由父元素处理.
            */
            parent.requestDisallowInterceptTouchEvent(true);
            break;
        case MotionEvent.ACTION_MOVE:
            int deltaX = X - mLastX;
            int deltaY = Y - mLastY;
            if(父容器需要此类事件){
                //让父元素可以继续拦截MOVE事件
                parent.requestDisallowInterceptTouchEvent(false);
            }
            break;
        case MotionEvent.ACTION_UP:
            break;
        default:
            break;
    }
    mLastX = x;
    mLastY = y;
    return super.dispatchTouchEvent(event);
}
~~~
父元素要做出如下处理
~~~
public boolean onInterceptTouchEvent(MotionEvent event){
    int action  = event.getAction();
    if(action == MotionEvent.ACTION_DOWN){
        return false;
    }else{
        return true;
    }
}
~~~
默认拦截除了ACTION_DOWN以外的事件.这样子元素调用requestDisallowInterceptTouchEvent(false)父元素才能继续拦截所需事件（看情况处理）；

###如有什么问题，敬请提出，十分感谢！希望越来越好，谢谢！
####如果喜欢，还请点击start，喜欢支持一下了，谢谢O(∩_∩)O~。
