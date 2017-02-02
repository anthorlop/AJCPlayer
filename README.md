# AJCPlayer
Audio and Video Player for Android with HLS and DASH support. Based on ExoPlayer

## Simple code
```gradle

repositories {
    maven { url 'https://github.com/anthorlop/mvn-android/raw/master/' }
}

// AJCPlayer gradle dependencies
compile 'es.lombrinus.projects.mods:AJCPlayer:1.0'
compile 'es.lombrinus.projects.mods:AJCNotification:1.0' // if you want notifications
compile 'es.lombrinus.projects.mods:AJCast:1.0' // if you want chromecast
```

In activity layout:
```xml
<FrameLayout
    android:id="@+id/videoSurfaceContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_centerInParent="true">
    <com.google.android.exoplayer.AspectRatioFrameLayout
        android:id="@+id/video_frame"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center">
        <SurfaceView
            android:id="@+id/videoSurface"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"/>
    </com.google.android.exoplayer.AspectRatioFrameLayout>
</FrameLayout>
```
Activity class:
Create an instance of AudioPlayer or VideoPlayer:
```java
AJCPlayer videoPlayer = new VideoPlayer(this, new MediaPlayer());
```
If you are playing video you have to load SurfaceHolder:
```java
// load SurfaceHolder
SurfaceHolder holder = mSurfaceView.getHolder();
DisplayMetrics metrics = new DisplayMetrics();
this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
holder.setFixedSize(metrics.widthPixels, (int) ((float) metrics.widthPixels / (float) 16 / (float) 9));
holder.addCallback(this);
```
And your activity has to implement SurfaceHolder.Callback. You can play video on surfaceCreated(...) method: 
```java
// views you want to hide automatically when content is playing
View viewBot = findViewById(R.id.audioPlayerLayoutBottom);
View viewTop = findViewById(R.id.audioPlayerLayoutTop);
CList viewsToHide = new CList(viewBot, viewTop);

// we send FrameLayout as param because AJCPlayer uses this view to detect click and double click
FrameLayout mFrameLayout = (FrameLayout) findViewById(R.id.videoSurfaceContainer);
        
final VideoPlayerView videoPlayerView = new VideoPlayerView(mFrameLayout, surfaceHolder, currentPositionTextView, durationTextView, seekBar, viewsToHide); // you could send currentPosition, duration or seekbar views as null

final VideoPlayerOptions options = new VideoPlayerOptions(ActivityInfo.SCREEN_ORIENTATION_SENSOR, true, true, true);
final Controls controls = new Controls(plays, pauses, stops);
controlBarManager = new VideoControlBarManager(context, controls, new LoadingView(){...}, new OnDoubleClick{...}, videoPlayerView, options); //LoadingView you can hide/show your progress bar. LoadingView and OnDoubleClick could be null
videoPlayer.addEventListener(controlBarManager);
```
Create Asset and Play video
```java
Asset asset = new Asset(idString, urlString, ContentType.VIDEO);
videoPlayer.play(asset, true); // autoplay=true
```
_____
## Summary. Step to Step
1. Add dependencies to build.gradle
2. In order to use AJCPlayer yo have to instantiate VideoPlayer or AudioPlayer. (you can use Dagger optionally)
3. Create the activity Layout with FrameLayout, com.google.android.exoplayer.AspectRatioFrameLayout and SurfaceView.
4. Load surfaceHolder and setCallback. When 'onSurfaceCreated' method is called we can play video.
5. Get Views in our Activity to set onclick events. (f.e.: Pause click should call to AJCPlayer Pause method)
6. Get Views in our Activity to send as param in VideoControlBarManager constructor. Although we have set buttons onclick events we have to send it to Player which will work with them to hide/show if playback state change. 
7. Add PlayEventListener to player (VideoControlBarManager, NotificationPlayerManager, SubtitleManager). You can create new implementations.
8. Create Asset with video ID, video URL and Content Type (Video/audio) and call AJCPlayer play(Asset..., true) method to play content.

## Images

### Fullscreen DASH / HLS
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/01.png" width="425"/> 
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/04.png" width="425"/>

### Video MP4 / HLS / DASH inside ScrollView
<details>
   <summary>Show/Hide images</summary>
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/02.png" width="280"/>
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/03.png" width="280"/>
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/05.png" width="280"/>
</details>
## Description
The purpose of this library is to decouple the video player and audio from the applications allowing to customize the view of the player from your application.
<details>
   <summary>Show in spanish</summary>
El objetivo de esta librería es desacoplar el reproductor de vídeo y audio de las aplicaciones permitiendo personalizar desde tu aplicación la vista del reproductor.
</details>
## Instructions

### Dependencies
Include in 'build.gradle' file:
```gradle

repositories {
    maven { url 'https://github.com/anthorlop/mvn-android/raw/master/' }
}

// AJCPlayer dependencies
compile 'es.lombrinus.projects.mods:AJCPlayer:1.0'
compile 'es.lombrinus.projects.mods:AJCNotification:1.0' // if you want notifications
compile 'es.lombrinus.projects.mods:AJCast:1.0' // if you want chromecast
```

### Main Interface
AJCPlayer is the main interface.
<details>
   <summary>AJCPlayer methods (Click to expand)</summary>
   
* **play(Asset, boolean autoplay):** Play content from asset (url). You can start content automatically.
* **play(Asset, int starPosition):** Play content from asset (url). You can start content from a specified position.
* **play():** Play content previously loaded / resume
* **setOptions(PlaybackSettings settings)** Set options to play content. For example force Player to use MediaPlayer or add ContentTypes to determine if loaded content is DASH or HLS). Not required.
* **isPlaying():** Check if content is currently playing
* **isLoading():** Check if content is loading
* **pause(Asset asset):** To pause current content
* **release(Asset asset):** To reset player process
* **addEventListener(PlayerEventListener):** Add listener to a list. Events will be called from player to notify every change of the state
* **isPaused():** Check if content is currently paused
* **onViewSizeChanged():** Notify a dimension change
* **seekTo(position):** Seek to a position
* **removeEventListener(listener):** Remove listener from the list of listeners
* **clearEventListeners():** Clear the list of listeners
* **getCurrentPosition():** Get current position
 </details>

## How to use

### Integration
AJCPlayer has two implementations (AudioPlayer and VideoPlayer).

The sample project use [Dagger](http://square.github.io/dagger/) to view inyection. (It is not necessary to use Dagger)
If you decide to use Dagger it will be necessary to do a Build Project.

You can check the project code:
[PlayerComponent.java](https://github.com/anthorlop/AJCPlayer/blob/master/app/src/main/java/com/ajc/playerex/di/PlayerComponent.java) and [ExampleApp.java](https://github.com/anthorlop/AJCPlayer/blob/master/app/src/main/java/com/ajc/playerex/app/ExampleApp.java)

Without Dagger:
```java
AJCPlayer videoPlayer = new VideoPlayer(context, new MediaPlayer());
```

### Add Controls and events
As we saw before, AJCPlayer defines a method to add implementations of PlayerEventListener to our player. These implementations will be executed from the library when the state of our player changes, for example if the content is paused, the pause event will be thrown in all its listeners.

PlayerEventListener interface:
```java
public interface PlayerEventListener {
    void onPreparing(Asset asset, MediaPlayer mediaPlayer);
    void onPlayBegins(Asset asset, int duration);
    void onResume(Asset asset, int currentPosition);
    void onCompletion(Asset asset);
    void onPause(Asset asset, int currentPosition);
    void onForward(Asset asset, int currentPosition);
}
```
Then, we have to add listeners before play content. There are some listeners already created that we can use:

 * **VideoControlBarManager:**  Video implementation.

```java
final VideoPlayerView videoPlayerView = new VideoPlayerView(mFrameLayout, surfaceHolder, current, duration, seekBar, controller);
final VideoPlayerOptions options = new VideoPlayerOptions(ActivityInfo.SCREEN_ORIENTATION_SENSOR, true, true, true);
final Controls controls = new Controls(plays, pauses, stops);
controlBarManager = new VideoControlBarManager(context, controls, new LoadingView(){...}, new OnDoubleClick{...}, videoPlayerView, options);
videoPlayer.addEventListener(controlBarManager);
```
<details>
<summary>Show more</summary>
* **Context:** Context
* **Controls:** List of Views (play, pause, stop)
* **LoadingView:**  Interface to define showLoading() and hideLoading() methods where loading progressBar can be show or hide.
* **OnDoubleClick:** Listener to notify duoble click.
* **VideoPlayerView:**  Elements of the view. Here is where we have to send SurfaceHolder.
* **VideoPlayerOptions:** Player options.
 ```java
 /** Type of orientation. To allow rotation */
 public final Integer mScreenOrientation;
 /** Auto Hide Status Bar during playing */
 public final Boolean mHideStatusBar;
 /** Auto hide Navigation bar during playing */
 public final Boolean mHideNavigationBar;
 /** if true video dimensions change depends on the screen */
 public final Boolean mFullscreen;
 ```
</details>
_____

 * **PlayerControlBarManager:**  Audio implementation. Similar to video.
 
_____

 * **SubtitleManager** Implementation to detect and send subtitles that we should to show.
```java
final SubtitleManager subtitleManager = new SubtitleManager(activity, urlVtt, new OnSubtitleDetect(){...});
videoPlayer.addEventListener(subtitleManager);
```
_____

 * **NotificationPlayerManager** Implementation to launch automatically a notification from which user could pause, play or stop player.
```java
// small notification view
RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.audio_player_notification_little);
// big notification view
RemoteViews bigContentView = new RemoteViews(getPackageName(), R.layout.audio_player_notification);
 
// set views
PlayerNotificationControls notifControls = new PlayerNotificationControls(
        R.mipmap.ic_audio_small_icon, contentView, bigContentView,
        R.id.play_icon, R.id.pause_icon, R.id.stop_icon, R.id.progressbar_player);
 
// AJCNotification audioNotification = new PlayerNotification(context, audioPlayer);
audioPlayer.addEventListener(new NotificationPlayerManager(notifConfig, audioNotification, new NotificationCallback(){...));
```
NotificationCallback interface methods:
```java
RemoteViews notificationChange(Notification notification, int notificationId, RemoteViews remoteViews);
void notificationViewClicked(int viewId);
```
