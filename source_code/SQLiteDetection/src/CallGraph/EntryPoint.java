package CallGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;

public class EntryPoint {

	Set<String> callbackClass = new HashSet<>();
	Set<String> lifeCycleClass = new HashSet<>();
	
	Map<String,Set<String>> lifeCycleMap = new HashMap<>();
	
	
	public EntryPoint()
	{

		lifeCycleClass.add("android.app.Activity");
		lifeCycleClass.add("android.app.Service");
		lifeCycleClass.add("com.google.android.gcm.GCMBaseIntentService");
		lifeCycleClass.add("com.google.android.gms.gcm.GcmListenerService");
		lifeCycleClass.add("android.content.BroadcastReceiver");
		lifeCycleClass.add("android.content.ContentProvider");
		lifeCycleClass.add("android.app.Application");
		
		Set<String> activity = new HashSet<>();
		activity.add("void onCreate(android.os.Bundle)");
		activity.add("void onStart()");
		activity.add("void onRestoreInstanceState(android.os.Bundle)");
		activity.add("void onPostCreate(android.os.Bundle)");
		activity.add("void onResume()");
		activity.add("void onPostResume()");
		activity.add("java.lang.CharSequence onCreateDescription()");
		activity.add("void onSaveInstanceState(android.os.Bundle)");
		activity.add("void onPause()");
		activity.add("void onStop()");
		activity.add("void onRestart()");
		activity.add("void onDestroy()");
		lifeCycleMap.put("android.app.Activity", activity);
		
		Set<String> service = new HashSet<>();
		service.add("void onCreate()");
		service.add("void onStart(android.content.Intent,int)");
		service.add("int onStartCommand(android.content.Intent,int,int)");
		service.add("android.os.IBinder onBind(android.content.Intent)");
		service.add("void onRebind(android.content.Intent)");
		service.add("boolean onUnbind(android.content.Intent)");
		service.add("void onDestroy()");
		lifeCycleMap.put("android.app.Service", service);
		
		Set<String> gcmIntentService = new HashSet<>();
		gcmIntentService.add("void onDeletedMessages(android.content.Context,int)");
		gcmIntentService.add("void onError(android.content.Context,java.lang.String)");
		gcmIntentService.add("void onMessage(android.content.Context,android.content.Intent)");
		gcmIntentService.add("void onRecoverableError(android.content.Context,java.lang.String)");
		gcmIntentService.add("void onRegistered(android.content.Context,java.lang.String)");
		gcmIntentService.add("void onUnregistered(android.content.Context,java.lang.String)");
		lifeCycleMap.put("com.google.android.gcm.GCMBaseIntentService", gcmIntentService);
		
		Set<String> gcmListenerService = new HashSet<>();
		gcmListenerService.add("void onDeletedMessages()");
		gcmListenerService.add("void onMessageReceived(java.lang.String,android.os.Bundle)");
		gcmListenerService.add("void onMessageSent(java.lang.String)");
		gcmListenerService.add("void onSendError(java.lang.String,java.lang.String)");
		lifeCycleMap.put("com.google.android.gms.gcm.GcmListenerService", gcmListenerService);
		
		Set<String> broadcast = new HashSet<>();
		broadcast.add("void onReceive(android.content.Context,android.content.Intent)");
		lifeCycleMap.put("android.content.BroadcastReceiver", broadcast);
		
		Set<String> contentProvider = new HashSet<>();
		contentProvider.add("boolean onCreate()");
		lifeCycleMap.put("android.content.ContentProvider",contentProvider);
				
		Set<String> application = new HashSet<>();
		application.add("void onCreate()");
		application.add("void onTerminate()");

		//application lifecycle callback
		application.add("void onActivityStarted(android.app.Activity)");
		application.add("void onActivityStopped(android.app.Activity)");
		application.add("void onActivitySaveInstanceState(android.app.Activity,android.os.Bundle)");
		application.add("void onActivityResumed(android.app.Activity)");
		application.add("void onActivityPaused(android.app.Activity)");
		application.add("void onActivityDestroyed(android.app.Activity)");
		application.add("void onActivityCreated(android.app.Activity,android.os.Bundle)");
		
		lifeCycleMap.put("android.app.Application",application);
		
		

		callbackClass.add("android.accounts.OnAccountsUpdateListener");
		callbackClass.add("android.animation.Animator$AnimatorListener");
		callbackClass.add("android.animation.LayoutTransition$TransitionListener");
		callbackClass.add("android.animation.TimeAnimator$TimeListener");
		callbackClass.add("android.animation.ValueAnimator$AnimatorUpdateListener");
		callbackClass.add("android.app.ActionBar$OnMenuVisibilityListener");
		callbackClass.add("android.app.ActionBar$OnNavigationListener");
		callbackClass.add("android.app.ActionBar$TabListener");
		callbackClass.add("android.app.Application$ActivityLifecycleCallbacks");
		callbackClass.add("android.app.DatePickerDialog$OnDateSetListener");
		callbackClass.add("android.app.FragmentBreadCrumbs$OnBreadCrumbClickListener");
		callbackClass.add("android.app.FragmentManager$OnBackStackChangedListener");
		callbackClass.add("android.app.KeyguardManager$OnKeyguardExitResult");
		callbackClass.add("android.app.LoaderManager$LoaderCallbacks");
		callbackClass.add("android.app.PendingIntent$OnFinished");
		callbackClass.add("android.app.SearchManager$OnCancelListener");
		callbackClass.add("android.app.SearchManager$OnDismissListener");
		callbackClass.add("android.app.TimePickerDialog$OnTimeSetListener");
		callbackClass.add("android.bluetooth.BluetoothProfile$ServiceListener");
		callbackClass.add("android.content.ClipboardManager$OnPrimaryClipChangedListener");
		callbackClass.add("android.content.ComponentCallbacks");
		callbackClass.add("android.content.ComponentCallbacks2");
		callbackClass.add("android.content.DialogInterface$OnCancelListener");
		callbackClass.add("android.content.DialogInterface$OnClickListener");
		callbackClass.add("android.content.DialogInterface$OnDismissListener");
		callbackClass.add("android.content.DialogInterface$OnKeyListener");
		callbackClass.add("android.content.DialogInterface$OnMultiChoiceClickListener");
		callbackClass.add("android.content.DialogInterface$OnShowListener");
		callbackClass.add("android.content.IntentSender$OnFinished");
		callbackClass.add("android.content.Loader$OnLoadCanceledListener");
		callbackClass.add("android.content.Loader$OnLoadCompleteListener");
		callbackClass.add("android.content.SharedPreferences$OnSharedPreferenceChangeListener");
		callbackClass.add("android.content.SyncStatusObserver");
		callbackClass.add("android.database.sqlite.SQLiteTransactionListener");
		callbackClass.add("android.drm.DrmManagerClient$OnErrorListener");
		callbackClass.add("android.drm.DrmManagerClient$OnEventListener");
		callbackClass.add("android.drm.DrmManagerClient$OnInfoListener");
		callbackClass.add("android.gesture.GestureOverlayView$OnGestureListener");
		callbackClass.add("android.gesture.GestureOverlayView$OnGesturePerformedListener");
		callbackClass.add("android.gesture.GestureOverlayView$OnGesturingListener");
		callbackClass.add("android.graphics.SurfaceTexture$OnFrameAvailableListener");
		callbackClass.add("android.hardware.Camera$AutoFocusCallback");
		callbackClass.add("android.hardware.Camera$AutoFocusMoveCallback");
		callbackClass.add("android.hardware.Camera$ErrorCallback");
		callbackClass.add("android.hardware.Camera$FaceDetectionListener");
		callbackClass.add("android.hardware.Camera$OnZoomChangeListener");
		callbackClass.add("android.hardware.Camera$PictureCallback");
		callbackClass.add("android.hardware.Camera$PreviewCallback");
		callbackClass.add("android.hardware.Camera$ShutterCallback");
		callbackClass.add("android.hardware.SensorEventListener");
		callbackClass.add("android.hardware.display.DisplayManager$DisplayListener");
		callbackClass.add("android.hardware.input.InputManager$InputDeviceListener");
		callbackClass.add("android.inputmethodservice.KeyboardView$OnKeyboardActionListener");
		callbackClass.add("android.location.GpsStatus$Listener");
		callbackClass.add("android.location.GpsStatus$NmeaListener");
		callbackClass.add("android.location.LocationListener");
		callbackClass.add("android.media.AudioManager$OnAudioFocusChangeListener");
		callbackClass.add("android.media.AudioRecord$OnRecordPositionUpdateListener");
		callbackClass.add("android.media.JetPlayer$OnJetEventListener");
		callbackClass.add("android.media.MediaPlayer$OnBufferingUpdateListener");
		callbackClass.add("android.media.MediaPlayer$OnCompletionListener");
		callbackClass.add("android.media.MediaPlayer$OnErrorListener");
		callbackClass.add("android.media.MediaPlayer$OnInfoListener");
		callbackClass.add("android.media.MediaPlayer$OnPreparedListener");
		callbackClass.add("android.media.MediaPlayer$OnSeekCompleteListener");
		callbackClass.add("android.media.MediaPlayer$OnTimedTextListener");
		callbackClass.add("android.media.MediaPlayer$OnVideoSizeChangedListener");
		callbackClass.add("android.media.MediaRecorder$OnErrorListener");
		callbackClass.add("android.media.MediaRecorder$OnInfoListener");
		callbackClass.add("android.media.MediaScannerConnection$MediaScannerConnectionClient");
		callbackClass.add("android.media.MediaScannerConnection$OnScanCompletedListener");
		callbackClass.add("android.media.SoundPool$OnLoadCompleteListener");
		callbackClass.add("android.media.audiofx.AudioEffect$OnControlStatusChangeListener");
		callbackClass.add("android.media.audiofx.AudioEffect$OnEnableStatusChangeListener");
		callbackClass.add("android.media.audiofx.BassBoost$OnParameterChangeListener");
		callbackClass.add("android.media.audiofx.EnvironmentalReverb$OnParameterChangeListener");
		callbackClass.add("android.media.audiofx.Equalizer$OnParameterChangeListener");
		callbackClass.add("android.media.audiofx.PresetReverb$OnParameterChangeListener");
		callbackClass.add("android.media.audiofx.Virtualizer$OnParameterChangeListener");
		callbackClass.add("android.media.audiofx.Visualizer$OnDataCaptureListener");
		callbackClass.add("android.media.effect$EffectUpdateListener");
		callbackClass.add("android.net.nsd.NsdManager$DiscoveryListener");
		callbackClass.add("android.net.nsd.NsdManager$RegistrationListener");
		callbackClass.add("android.net.nsd.NsdManager$ResolveListener");
		callbackClass.add("android.net.sip.SipRegistrationListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$ActionListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$ChannelListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$ConnectionInfoListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$GroupInfoListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$PeerListListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$ServiceResponseListener");
		callbackClass.add("android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener");
		callbackClass.add("android.os.CancellationSignal$OnCancelListener");
		callbackClass.add("android.os.IBinder$DeathRecipient");
		callbackClass.add("android.os.MessageQueue$IdleHandler");
		callbackClass.add("android.os.RecoverySystem$ProgressListener");
		callbackClass.add("android.preference.Preference$OnPreferenceChangeListener");
		callbackClass.add("android.preference.Preference$OnPreferenceClickListener");
		callbackClass.add("android.preference.PreferenceFragment$OnPreferenceStartFragmentCallback");
		callbackClass.add("android.preference.PreferenceManager$OnActivityDestroyListener");
		callbackClass.add("android.preference.PreferenceManager$OnActivityResultListener");
		callbackClass.add("android.preference.PreferenceManager$OnActivityStopListener");
		callbackClass.add("android.security.KeyChainAliasCallback");
		callbackClass.add("android.speech.RecognitionListener");
		callbackClass.add("android.speech.tts.TextToSpeech$OnInitListener");
		callbackClass.add("android.speech.tts.TextToSpeech$OnUtteranceCompletedListener");
		callbackClass.add("android.view.ActionMode$Callback");
		callbackClass.add("android.view.ActionProvider$VisibilityListener");
		callbackClass.add("android.view.GestureDetector$OnDoubleTapListener");
		callbackClass.add("android.view.GestureDetector$OnGestureListener");
		callbackClass.add("android.view.InputQueue$Callback");
		callbackClass.add("android.view.KeyEvent$Callback");
		callbackClass.add("android.view.MenuItem$OnActionExpandListener");
		callbackClass.add("android.view.MenuItem$OnMenuItemClickListener");
		callbackClass.add("android.view.ScaleGestureDetector$OnScaleGestureListener");
		callbackClass.add("android.view.SurfaceHolder$Callback");
		callbackClass.add("android.view.SurfaceHolder$Callback2");
		callbackClass.add("android.view.TextureView$SurfaceTextureListener");
		callbackClass.add("android.view.View$OnAttachStateChangeListener");
		callbackClass.add("android.view.View$OnClickListener");
		callbackClass.add("android.view.View$OnCreateContextMenuListener");
		callbackClass.add("android.view.View$OnDragListener");
		callbackClass.add("android.view.View$OnFocusChangeListener");
		callbackClass.add("android.view.View$OnGenericMotionListener");
		callbackClass.add("android.view.View$OnHoverListener");
		callbackClass.add("android.view.View$OnKeyListener");
		callbackClass.add("android.view.View$OnLayoutChangeListener");
		callbackClass.add("android.view.View$OnLongClickListener");
		callbackClass.add("android.view.View$OnSystemUiVisibilityChangeListener");
		callbackClass.add("android.view.View$OnTouchListener");
		callbackClass.add("android.view.ViewGroup$OnHierarchyChangeListener");
		callbackClass.add("android.view.ViewStub$OnInflateListener");
		callbackClass.add("android.view.ViewTreeObserver$OnDrawListener");
		callbackClass.add("android.view.ViewTreeObserver$OnGlobalFocusChangeListener");
		callbackClass.add("android.view.ViewTreeObserver$OnGlobalLayoutListener");
		callbackClass.add("android.view.ViewTreeObserver$OnPreDrawListener");
		callbackClass.add("android.view.ViewTreeObserver$OnScrollChangedListener");
		callbackClass.add("android.view.ViewTreeObserver$OnTouchModeChangeListener");
		callbackClass.add("android.view.accessibility.AccessibilityManager$AccessibilityStateChangeListener");
		callbackClass.add("android.view.animation.Animation$AnimationListener");
		callbackClass.add("android.view.inputmethod.InputMethod$SessionCallback");
		callbackClass.add("android.view.inputmethod.InputMethodSession$EventCallback");
		callbackClass.add("android.view.textservice.SpellCheckerSession$SpellCheckerSessionListener");
		callbackClass.add("android.webkit.DownloadListener");
		callbackClass.add("android.widget.AbsListView$MultiChoiceModeListener");
		callbackClass.add("android.widget.AbsListView$OnScrollListener");
		callbackClass.add("android.widget.AbsListView$RecyclerListener");
		callbackClass.add("android.widget.AdapterView$OnItemClickListener");
		callbackClass.add("android.widget.AdapterView$OnItemLongClickListener");
		callbackClass.add("android.widget.AdapterView.OnItemSelectedListener");
		callbackClass.add("android.widget.AutoCompleteTextView$OnDismissListener");
		callbackClass.add("android.widget.CalendarView$OnDateChangeListener");
		callbackClass.add("android.widget.Chronometer$OnChronometerTickListener");
		callbackClass.add("android.widget.CompoundButton$OnCheckedChangeListener");
		callbackClass.add("android.widget.DatePicker$OnDateChangedListener");
		callbackClass.add("android.widget.ExpandableListView$OnChildClickListener");
		callbackClass.add("android.widget.ExpandableListView$OnGroupClickListener");
		callbackClass.add("android.widget.ExpandableListView$OnGroupCollapseListener");
		callbackClass.add("android.widget.ExpandableListView$OnGroupExpandListener");
		callbackClass.add("android.widget.Filter$FilterListener");
		callbackClass.add("android.widget.NumberPicker$OnScrollListener");
		callbackClass.add("android.widget.NumberPicker$OnValueChangeListener");
		callbackClass.add("android.widget.NumberPicker$OnDismissListener");
		callbackClass.add("android.widget.PopupMenu$OnMenuItemClickListener");
		callbackClass.add("android.widget.PopupWindow$OnDismissListener");
		callbackClass.add("android.widget.RadioGroup$OnCheckedChangeListener");
		callbackClass.add("android.widget.RatingBar$OnRatingBarChangeListener");
		callbackClass.add("android.widget.SearchView$OnCloseListener");
		callbackClass.add("android.widget.SearchView$OnQueryTextListener");
		callbackClass.add("android.widget.SearchView$OnSuggestionListener");
		callbackClass.add("android.widget.SeekBar$OnSeekBarChangeListener");
		callbackClass.add("android.widget.ShareActionProvider$OnShareTargetSelectedListener");
		callbackClass.add("android.widget.SlidingDrawer$OnDrawerCloseListener");
		callbackClass.add("android.widget.SlidingDrawer$OnDrawerOpenListener");
		callbackClass.add("android.widget.SlidingDrawer$OnDrawerScrollListener");
		callbackClass.add("android.widget.TabHost$OnTabChangeListener");
		callbackClass.add("android.widget.TextView$OnEditorActionListener");
		callbackClass.add("android.widget.TimePicker$OnTimeChangedListener");
		callbackClass.add("android.widget.ZoomButtonsController$OnZoomListener"); 
		
	}
	
	public boolean isEntry(SootClass currentClass,String subSignature)
	{
		String className = currentClass.getName();
		if(callbackClass.contains(className))
				return true;
		else if(lifeCycleClass.contains(className))
		{
			if(lifeCycleMap.get(className).contains(subSignature))
				return true;
			else
				return false;
		}
		else
		{
			if(currentClass.hasSuperclass())
				return isEntry(currentClass.getSuperclass(),subSignature);
			else
				return false;
		}
	}
}
