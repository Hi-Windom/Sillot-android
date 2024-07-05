### singleInstancePerTask

`singleInstancePerTask`是Android 12（API 级别 31）引入的一个新的Activity启动模式。这个启动模式与 `singleInstance`非常相似，但有一些关键的区别。
`singleInstance`:

- Activity是它所在任务中的唯一实例。
- Activity有它自己的任务栈，这个栈中只包含这个Activity的一个实例。
- 任何启动这个Activity的操作都会使这个实例成为当前任务的焦点，而不会创建新的实例。
  `singleInstancePerTask`:
- Activity是它所在任务中的唯一实例。
- Activity会创建一个新的任务栈，这个栈中只包含这个Activity的一个实例。
- 与 `singleInstance`不同的是，`singleInstancePerTask`允许Activity有多个实例，每个实例都在不同的任务栈中。
  简而言之，`singleInstance`确保整个系统中只有一个Activity的实例，而 `singleInstancePerTask`则确保每个任务中只有一个Activity的实例，但可以有多个这样的任务栈存在。这意味着可以有多个 `singleInstancePerTask`的Activity实例，只要它们各自在不同的任务栈中。
  这个新的启动模式为开发者提供了更大的灵活性，可以在需要时为特定的Activity创建新的任务，同时保持Activity的独立性。

当你从MainActivity启动SingleInstancePerTaskActivity，然后点击SingleInstancePerTaskActivity中的按钮返回MainActivity，
再次从MainActivity启动SingleInstancePerTaskActivity时，你会注意到SingleInstancePerTaskActivity的实例并没有被创建，
而是之前已经创建的实例被带到了前台。这表明，尽管我们多次尝试启动SingleInstancePerTaskActivity，但系统中只有一个实例，并且它是在它自己的任务中的。

这与singleInstance模式的行为相似，但是singleInstancePerTask允许有多个实例，只要它们各自在不同的任务中。
如果你在MainActivity中再次点击按钮启动SingleInstancePerTaskActivity，并且这次使用了带有FLAG_ACTIVITY_NEW_TASK标志的Intent，
你将会创建一个新的任务和一个新的SingleInstancePerTaskActivity实例。这展示了singleInstancePerTask与singleInstance的主要区别：
singleInstancePerTask允许在同一设备上存在多个实例，只要它们在不同的任务中。

### android:configChanges

在Android开发中，`android:configChanges`属性是在AndroidManifest.xml文件中的一个属性，它用于指定当配置发生变化时，是否需要重启Activity。如果你在该属性中声明了某个配置变化，当这个配置变化发生时，系统会调用 `onConfigurationChanged()`方法，而不是重启Activity。

以下是 `configChanges`属性支持的所有合法标志：

* **`mcc` - 移动国家代码发生变化（用户漫游到了另一个国家）。**
* **`mnc` - 移动网络代码发生变化（用户切换到了另一个运营商的网络）。**
* `locale` - 设备的本地设置变化，例如语言变化。
* `touchscreen` - 触摸屏已经改变（这通常不会发生）。
* **`keyboard` - 键盘类型发生变化，例如用户插入了外接键盘。**
* **`keyboardHidden` - 键盘的可访问性发生变化，例如用户滑出了软键盘。**
* `navigation` - 系统导航方式变化，例如使用方向键导航。
* **`screenLayout` - 屏幕布局方向发生变化。**
* `fontScale` - 全局字体大小发生变化。
* `uiMode` - UI模式发生变化，例如从正常模式变为汽车模式。
* **`orientation` - 设备方向发生变化，例如从纵向变为横向。**
* **`screenSize` - 屏幕尺寸发生变化，这不会在Android 3.2之前的版本中触发，因为在那之前这个概念不存在。**
* **`smallestScreenSize` - 最小屏幕尺寸发生变化，这用于区分正常屏幕和超大屏幕设备。**

系统语言变化、系统导航方式变化、系统字体大小变化，这些配置变化变化是动态的，不能通过 `configChanges` 属性来处理，即声明无效。

特别注意：

1、不设置Activity的android:configChanges时，切屏会重新调用各个生命周期，切横屏时会执行一次，切竖屏时会执行两次

2、设置Activity的android:configChanges="orientation"时，切屏还是会重新调用各个生命周期，切横、竖屏时只会执行一次

3、设置Activity的android:configChanges="orientation|keyboardHidden"时，切屏不会重新调用各个生命周期，只会执行onConfigurationChanged方法

但是，自从Android 3.2（API 13），在设置Activity的android:configChanges="orientation|keyboardHidden"后，还是一样会重新调用各个生命周期的。因为screen size也开始跟着设备的横竖切换而改变。所以除了设置"orientation"，还必须设置"ScreenSize"。

汐洛通常使用以下声明确保配置变化不重启活动：

```xml
<activity>
  android:configChanges="mcc|mnc|keyboard|keyboardHidden|screenLayout|orientation|screenSize|smallestScreenSize"
</activity>
```
