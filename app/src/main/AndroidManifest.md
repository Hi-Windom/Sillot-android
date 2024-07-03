
### singleInstancePerTask

`singleInstancePerTask`是Android 12（API 级别 31）引入的一个新的Activity启动模式。这个启动模式与`singleInstance`非常相似，但有一些关键的区别。
`singleInstance`:
- Activity是它所在任务中的唯一实例。
- Activity有它自己的任务栈，这个栈中只包含这个Activity的一个实例。
- 任何启动这个Activity的操作都会使这个实例成为当前任务的焦点，而不会创建新的实例。
  `singleInstancePerTask`:
- Activity是它所在任务中的唯一实例。
- Activity会创建一个新的任务栈，这个栈中只包含这个Activity的一个实例。
- 与`singleInstance`不同的是，`singleInstancePerTask`允许Activity有多个实例，每个实例都在不同的任务栈中。
  简而言之，`singleInstance`确保整个系统中只有一个Activity的实例，而`singleInstancePerTask`则确保每个任务中只有一个Activity的实例，但可以有多个这样的任务栈存在。这意味着可以有多个`singleInstancePerTask`的Activity实例，只要它们各自在不同的任务栈中。
  这个新的启动模式为开发者提供了更大的灵活性，可以在需要时为特定的Activity创建新的任务，同时保持Activity的独立性。

当你从MainActivity启动SingleInstancePerTaskActivity，然后点击SingleInstancePerTaskActivity中的按钮返回MainActivity，
再次从MainActivity启动SingleInstancePerTaskActivity时，你会注意到SingleInstancePerTaskActivity的实例并没有被创建，
而是之前已经创建的实例被带到了前台。这表明，尽管我们多次尝试启动SingleInstancePerTaskActivity，但系统中只有一个实例，并且它是在它自己的任务中的。

这与singleInstance模式的行为相似，但是singleInstancePerTask允许有多个实例，只要它们各自在不同的任务中。
如果你在MainActivity中再次点击按钮启动SingleInstancePerTaskActivity，并且这次使用了带有FLAG_ACTIVITY_NEW_TASK标志的Intent，
你将会创建一个新的任务和一个新的SingleInstancePerTaskActivity实例。这展示了singleInstancePerTask与singleInstance的主要区别：
singleInstancePerTask允许在同一设备上存在多个实例，只要它们在不同的任务中。