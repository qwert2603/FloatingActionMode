# FloatingActionMode

[![](https://www.jitpack.io/v/qwert2603/FloatingActionMode.svg)](https://www.jitpack.io/#qwert2603/FloatingActionMode)

Floating Action Mode (FAM) is the custom view for context actions on Android. (*minSdkVersion* ***15***)

FAM can be dragged over screen or it can be fixed at the top or bottom (with disabled dragging).

## [Demo Video (Fixed)](https://www.youtube.com/watch?v=1tn0MQV0ZrQ)
## [Demo Video (Dragging)](https://www.youtube.com/watch?v=PbQ8N7pWGt4)

## Fixed
![Art](https://github.com/qwert2603/FloatingActionMode/blob/master/art/qq2.png)


## Dragging
![Art](https://github.com/qwert2603/FloatingActionMode/blob/master/art/qq.png)

## XML-Attributes

FAM has following XML-attributes (they also may be changed programmatically):

* ***fam_opened*** defines whether FAM opened when created. (false by default)

* ***fam_content_res*** is LayoutRes that represents content of FAM (e.g. some buttons). (no content by default)
* ***fam_can_close*** defines whether FAM has button for closing itself. (true by default)

* ***fam_close_icon*** is DrawableRes for closing button. (has default value)

* ***fam_can_drag*** defines whether FAM has button for gragging itself. (true by default)

* ***fam_drag_icon*** is DrawableRes for dragging button. (has default value)

* ***fam_can_dismiss*** defines whether FAM may be dismissed (and closed) if transtationX while dragging is big enough. (true by default)

* ***fam_dismiss_threshold*** is fraction that used to solve threshold translationX for dismissing. (0.4f by default)

* ***fam_minimize_direction*** defines minimize direction of FAM. This attribute may have following values (nearest by default):
 * *top* - FAM will be translated to the top border of parent (excluding offsets) while minimizing.
 * *bottom* - FAM will be translated to the bottom border of parent (excluding offsets) while minimizing.
 * *nearest* - FAM will be translated to the nearest (top or bottom) border of parent (excluding offsets) while minimizing.

* ***fam_animation_duration*** defines duration of minimize/maximize animations. (400 by default)

FAM has ***OnCloseListener*** that allows to get callback when FAM was dismissed or closed by user.

## Using in CoordinatorLayout

FAM has its special CoordinatorLayout.Behavior and can be used in CoordinatorLayout.
FloatingActionModeBehavior allows to offset FAM of AppBarLayout and Snackbar.SnackbarLayout.
Also it allows to minimize/maximize FAM on scroll.

FAM has no background by default, so you can use any one you want.

*android:translationZ="8dp"* can be used for shadow.

*android:animateLayoutChanges="true"* can be used to animate contentChanges (*fam_content_res* attribute).
FAM animates *fam_close_button* and *fam_drag_button* changes by default.

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
        app:fam_animation_duration="@integer/action_mode_animation_duration"
        app:fam_can_dismiss="true"
        app:fam_can_drag="true"
        app:fam_content_res="@layout/user_list_action_mode_2"
        app:fam_dismiss_threshold="0.35"
        app:fam_drag_icon="@drawable/ic_drag_white_24dp"
        app:fam_minimize_direction="nearest"/>

</android.support.design.widget.CoordinatorLayout>
```

## Download

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
