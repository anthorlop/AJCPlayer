# AJCPlayer
Audio and Video Player for Android with HLS and DASH support

## Imágenes

### Fullscreen DASH / HLS
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/01.png" width="425"/> 
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/04.png" width="425"/>

### Video MP4 / HLS / DASH en ScrollView
<details>
   <summary>Ver/Ocultar imágenes</summary>
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/02.png" width="280"/>
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/03.png" width="280"/>
<img src="https://github.com/anthorlop/AJCPlayer/blob/develop/ScreenShots/05.png" width="280"/>
</details>
## Descripción
El objetivo de esta librería es desacoplar el reproductor de vídeo y audio de las aplicaciones permitiendo personalizar desde tu aplicación la vista del reproductor.

## Instrucciones

### Dependencias
Incluir en build.gradle:
```gradle
// AJCPlayer dependencies
compile 'es.lombrinus.projects.mods:AJCPlayer:1.0'
compile 'es.lombrinus.projects.mods:AJCNotification:1.0' // if you want notifications
compile 'es.lombrinus.projects.mods:AJCast:1.0' // if you want chromecast
```

### Main Interface
Tiene como interface principal AJCPlayer que define los métodos:
<details>
   <summary>AJCPlayer.java (Click to expand)</summary>
   ```java
    /**
     * 
     * Play content from asset url
     * 
     * @param asset contains url to play and contentType video or audio
     * @param autoPlay to start content automatically
     */
    void play(Asset asset, boolean autoPlay);

    /**
     *
     * Play content from asset url
     *
     * @param asset contains url to play and contentType video or audio
     * @param position to start content from a specified position
     */
    void play(Asset asset, int position);

    /**
     * Play content previously loaded / resume
     */
    void play();

    /**
     *
     * Set options to play content (forceMediaPlayer, contentTypes list)
     *
     * @param settings options class
     */
    void setOptions(PlaybackSettings settings);

    /**
     * Check if content is currently playing
     * 
     * @return true or false
     */
    boolean isPlaying();

    /**
     * Check if content is currently paused
     *
     * @return true or false
     */
    boolean isPaused();

    /**
     * Check if content is loading
     *
     * @return true or false
     */
    boolean isLoading();

    /**
     * To pause current content
     */
    void pause();

    /**
     * To reset player process
     */
    void release();

    /**
     * Notify a dimension change
     */
    void onViewSizeChanged();

    /**
     * Add listener to a list. Events will be called from player to notify every change of the state
     */
    void addEventListener(PlayerEventListener playerEventListener);

    /**
     * Remove listener from the list of listeners
     * @param eventListenerClass event to remove
     */
    void removeEventListener(Class eventListenerClass);

    /**
     * Clear the list of listeners
     */
    void clearEventListeners();

    /**
     * Seek to a position
     * 
     * @param position position to seek
     */
    void seekTo(int position);

    /**
     * Get current position
     * 
     * @return current position
     */
    int getCurrentPosition();
}
```
 </details>

* **play(Asset, boolean autoplay):** Inicia la reproducción del asset pasado por parámetro. Autoplay define si se desea comenzar la reproducción automáticamente o no.
* **play(Asset, int starPosition):** Inicia la reproducción del asset pasado por parámetro. StartPosition define la posición en milisegundos en la que se desea empezar la reproducción
* **play():** Reanuda la reproducción en curso
* **setOptions(PlaybackSettings settings)** Permite configurar una serie de opciones. (forzar MediaPlayer o añadir ContentTypes que determinarán si el contenido es Dash o HLS). No es necesario usarlo, pero se ofrece la opción.
* **isPlaying():** TRUE si la reproducción está en curso o FALSE si está pausada.
* **isLoading():** TRUE si la reproducción está preparándose o FALSE si ya ha iniciado.
* **pause(Asset asset):** Pausa la reproducción.
* **release(Asset asset):** Detiene la reproducción.
* **addEventListener(PlayerEventListener):** Añade un escuchador de eventos para interactuar con cualquier componente que queramos acoplarle, más tarde veremos un ejemplo con las Notificaciones.
* **isPaused():** Comprueba si esta pausada la reproducción
* **onViewSizeChanged():** Informa al player de un cambio para que actualice las dimensiones del Video.
* **seekTo(position):** Continuar la reproducción por la posición indicada en milisegundos..
* **removeEventListener(listener):** Elimina el listener pasado por párametro.
* **clearEventListeners():** Elimina todos los listeners.
* **getUrlResolved():** Obtener la URL final del contenido.
* **getCurrentPosition():** Obtener la posición actual de la reproducción.

## Uso del módulo

### Integración
AJCPlayer tiene dos implementaciones (AudioPlayer y VideoPlayer).

El constructor de ambas clases será el siguiente:
```java
VideoPlayer(Context context, MediaPlayer mediaPlayer)...
AudioPlayer(Context context, MediaPlayer mediaPlayer)...
```

En el proyecto de ejemplo se utiliza [Dagger](http://square.github.io/dagger/) para inyección de dependencias. Para ello se ha creado el componente [PlayerComponent.java](https://github.com/anthorlop/AJCPlayer/blob/master/app/src/main/java/com/ajc/playerex/di/PlayerComponent.java). Será necesario hacer un Build Project la primera vez para que la clase [ExampleApp](https://github.com/anthorlop/AJCPlayer/blob/master/app/src/main/java/com/ajc/playerex/app/ExampleApp.java) no de error. No es necesario usar Dagger, si decides no usarlo no necesitaras crear nada de esto y puedes crear una instancia de la clase VideoPlayer o AudioPlayer directamente:
```java
AJCPlayer videoPlayer = new VideoPlayer(context, new MediaPlayer());
```

### Añadir Controles y eventos
Cómo vimos antes, AJCPlayer define un método para añadir implementaciones de PlayerEventListener a nuestro reproductor. Estas implementaciones se ejecutaran conforme el estado de nuestra reproducción cambie, por ejemplo si se pausa se lanzara el evento de pause en todos sus listeners.
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
Por lo tanto, lo que tenemos que hacer antes de comenzar a reproducir es añadir los eventos que deseemos. En la librería ya tenemos algunos creados que podemos usar:

 * **VideoControlBarManager:**  Implementación para Video en la que se envía el SurfaceHolder dónde el video será mostrado.

```java
final VideoPlayerView videoPlayerView = new VideoPlayerView(mFrameLayout, surfaceHolder, current, duration, seekBar, controller);
final VideoPlayerOptions options = new VideoPlayerOptions(ActivityInfo.SCREEN_ORIENTATION_SENSOR, true, true, true);
final Controls controls = new Controls(plays, pauses, stops);
controlBarManager = new VideoControlBarManager(context, controls, new LoadingView(){...}, new OnDoubleClick{...}, videoPlayerView, options);
videoPlayer.addEventListener(controlBarManager);
```
<details>
<summary>Ver más sobre VideoControlBarManager</summary>
* **Context:** Necesario para corregir algunos problemas de acceso a las vistas cuando no estamos en el hilo principal.
* **Controls:** Lista de controles (play, pause, stop)
* **LoadingView:**  Interface que define los métodos showLoading() y hideLoading() para que sea desde nuestra aplicación dónde incluyamos el "Cargando" cómo y dónde queramos, si es que lo queremos mostrar.
* **OnDoubleClick:** Listener que avisa a nuestra aplicación cuando se haga doble click sobre el video.
* **VideoPlayerView:**  Elementos de nuestra vista que se modificaran automáticamente según el estado de la reproducción. Aquí viene incluido el SurfaceHolder.
* **VideoPlayerOptions:** Opciones del reproductor.
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

 * **SubtitleManager** Implementación para detectar y mostrar los subtitulos.
```java
final SubtitleManager subtitleManager = new SubtitleManager(activity, urlVtt, new OnSubtitleDetect(){...});
videoPlayer.addEventListener(subtitleManager);
```
