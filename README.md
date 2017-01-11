# FloatingActionMode

[![](https://www.jitpack.io/v/qwert2603/FloatingActionMode.svg)](https://www.jitpack.io/#qwert2603/FloatingActionMode)

Floating Action Mode (FAM) is the custom view for context actions on Android. (*minSdkVersion* ***15***)

## [Demo Video](https://www.youtube.com/watch?v=PbQ8N7pWGt4)

![Art](https://github.com/qwert2603/FloatingActionMode/blob/master/art/qq.png)(https://www.youtube.com/watch?v=PbQ8N7pWGt4)

## XML-Attributes

FAM has following XML-attributes (they also may be changed programmatically):

* ***opened*** defines whether FAM opened when created. (false by default)

* ***content_res*** is LayoutRes that represents content of FAM (e.g. some buttons). (no content by default)

* ***can_close*** defines whether FAM has button for closing itself. (true by default)

* ***close_icon*** is DrawableRes for closing button. (has default value)

* ***can_drag*** defines whether FAM has button for gragging itself. (true by default)

* ***drag_icon*** is DrawableRes for dragging button. (has default value)

* ***can_dismiss*** defines whether FAM may be dismissed (and closed) if transtationX while dragging is big enough. (true by default)

* ***dismiss_threshold*** is fraction that used to solve threshold translationX for dismissing. (0.4f by default)

* ***minimize_direction*** defines minimize direction of FAM. This attribute may have following values (nearest by default):
 * *none* - FAM will not be translated while minimizing.
 * *top* - FAM will be translated to the top border of parent (excluding offsets) while minimizing.
 * *bottom* - FAM will be translated to the bottom border of parent (excluding offsets) while minimizing.
 * *nearest* - FAM will be translated to the nearest (top or bottom) border of parent (excluding offsets) while minimizing.

* ***animation_duration*** defines duration of minimize/maximize animations. (400 by default)

FAM has ***OnCloseListener*** that allows to get callback when FAM was dismissed or closed by user.

## Using in CoordinatorLayout

FAM has its special CoordinatorLayout.Behavior and can be used in CoordinatorLayout.
FloatingActionModeBehavior allows to offset FAM of AppBarLayout and Snackbar.SnackbarLayout.
Also it allows to minimize/maximize FAM on scroll.

FAM has no background by default, so you can use any one you want.

*android:translationZ="8dp"* can be used for shadow.

*android:animateLayoutChanges="true"* can be used to animate contentChanges (*content_res* attribute).
FAM animates *close_button* and *drag_button* changes by default.

```
<android.support.design.widget.CoordinatorLayout>

    <android.support.design.widget.AppBarLayout>
        ...
    </android.support.design.widget.AppBarLayout>

    <android.support.v7.widget.RecyclerView
        app:layout_behavior="@string/appbar_scrolling_view_behavior"/>

    <com.qwert2603.floating_action_mode.FloatingActionMode
        android:id="@+id/floating_action_mode"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="@dimen/action_mode_margin"
        android:animateLayoutChanges="true"
        android:background="@drawable/action_mode_background"
        android:translationZ="8dp"
        app:animation_duration="@integer/action_mode_animation_duration"
        app:can_dismiss="true"
        app:can_drag="true"
        app:content_res="@layout/user_list_action_mode_2"
        app:dismiss_threshold="0.35"
        app:drag_icon="@drawable/ic_drag_white_24dp"
        app:minimize_direction="nearest"/>

</android.support.design.widget.CoordinatorLayout>
```

##Download

```
allprojects {
		repositories {
			...
			maven { url "https://www.jitpack.io" }
		}
	}
```

```
dependencies {
	        compile 'com.github.qwert2603:FloatingActionMode:x.y.z'
}
```
