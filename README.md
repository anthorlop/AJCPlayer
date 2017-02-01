# AJCPlayer
Audio and Video Player for Android with HLS and DASH support

## Description
El objetivo de esta librería es desacoplar el reproductor de vídeo y audio de las aplicaciones permitiendo personalizar desde tu aplicación la vista del reproductor.

## Instructions

### Dependencies
Add dependencies to build.gradle:
```gradle
// AJCPlayer dependencies
compile 'es.lombrinus.projects.mods:AJCPlayer:1.0'
compile 'es.lombrinus.projects.mods:AJCNotification:1.0' // if you want notifications
compile 'es.lombrinus.projects.mods:AJCast:1.0' // if you want chromecast
```

### Main Interface
Tiene como interface principal AJCPlayer que define los métodos:
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
