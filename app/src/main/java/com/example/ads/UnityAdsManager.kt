package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

/**
 * Production-ready Unity Ads manager for Ludo Club.
 * Configured with live production credentials, robust error handling,
 * and automatic ad reload mechanisms.
 */
object UnityAdsManager {
    private const val TAG = "UnityAdsManager"

    // Live Production Unity Ads Credentials
    const val UNITY_GAME_ID = "800404254"
    const val REWARDED_PLACEMENT_ID = "BP_Rewarded_Android"
    const val BANNER_PLACEMENT_ID = "BP_Banner_Android"
    private const val TEST_MODE = false // Live production mode

    @Volatile
    var isInitialized = false
        private set

    @Volatile
    private var isInitializing = false

    @Volatile
    var isRewardedLoaded = false
        private set

    @Volatile
    private var isRewardedLoading = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val initCallbacks = mutableListOf<() -> Unit>()

    /**
     * Initializes Unity Ads with the live Game ID in production mode.
     */
    fun initialize(context: Context, onComplete: () -> Unit = {}) {
        if (isInitialized) {
            onComplete()
            return
        }

        synchronized(this) {
            if (isInitialized) {
                onComplete()
                return
            }
            initCallbacks.add(onComplete)
            if (isInitializing) {
                return
            }
            isInitializing = true
        }

        val appContext = context.applicationContext
        Log.i(TAG, "Initializing Unity Ads with Game ID: $UNITY_GAME_ID in live production mode (testMode=$TEST_MODE)")

        try {
            UnityAds.initialize(
                appContext,
                UNITY_GAME_ID,
                TEST_MODE,
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        Log.i(TAG, "Unity Ads initialized successfully in live production mode")
                        isInitialized = true
                        isInitializing = false

                        // Automatically pre-load the first rewarded ad so it is ready immediately
                        preloadRewardedAd(appContext)

                        // Trigger all pending initialization listeners
                        dispatchInitCallbacks()
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError,
                        message: String
                    ) {
                        Log.e(TAG, "Unity Ads initialization failed: $error - $message")
                        isInitializing = false
                        dispatchInitCallbacks()

                        // Automatically schedule a retry after 10 seconds to recover from network drops
                        mainHandler.postDelayed({
                            if (!isInitialized) {
                                initialize(appContext)
                            }
                        }, 10_000L)
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception initializing Unity Ads", e)
            isInitializing = false
            dispatchInitCallbacks()
        }
    }

    private fun dispatchInitCallbacks() {
        val callbacksToRun: List<() -> Unit>
        synchronized(this) {
            callbacksToRun = ArrayList(initCallbacks)
            initCallbacks.clear()
        }
        mainHandler.post {
            callbacksToRun.forEach { callback ->
                try {
                    callback()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error running init callback", e)
                }
            }
        }
    }

    /**
     * Pre-loads the rewarded video ad for the given placement ID.
     * Ensures an ad is always cached and ready to play without latency.
     */
    fun preloadRewardedAd(context: Context? = null) {
        if (!isInitialized) {
            if (context != null) {
                initialize(context) { preloadRewardedAd(context) }
            }
            return
        }

        if (isRewardedLoaded || isRewardedLoading) {
            Log.d(TAG, "Rewarded ad already loaded or currently loading")
            return
        }

        isRewardedLoading = true
        Log.d(TAG, "Preloading rewarded ad with placement ID: $REWARDED_PLACEMENT_ID")

        try {
            UnityAds.load(
                REWARDED_PLACEMENT_ID,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String) {
                        Log.i(TAG, "Rewarded ad successfully loaded and ready for placement: $placementId")
                        isRewardedLoaded = true
                        isRewardedLoading = false
                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String,
                        error: UnityAds.UnityAdsLoadError,
                        message: String
                    ) {
                        Log.w(TAG, "Rewarded ad failed to load ($placementId): $error - $message")
                        isRewardedLoaded = false
                        isRewardedLoading = false

                        // Automatically retry loading after 15 seconds to ensure reliability across sessions
                        mainHandler.postDelayed({
                            if (!isRewardedLoaded && !isRewardedLoading) {
                                preloadRewardedAd(context)
                            }
                        }, 15_000L)
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during rewarded ad load", e)
            isRewardedLoading = false
            isRewardedLoaded = false
        }
    }

    /**
     * Shows a rewarded video ad.
     * If already cached, displays immediately.
     * If not yet cached, loads on demand and shows immediately upon load completion.
     * Automatically triggers a fresh preload when finished or skipped.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (coinsEarned: Long) -> Unit,
        onAdFailed: (errorMessage: String) -> Unit
    ) {
        if (!isInitialized) {
            Log.d(TAG, "Unity Ads not initialized prior to show. Initializing now...")
            initialize(activity) {
                showRewardedAdInternal(activity, onRewardEarned, onAdFailed)
            }
            return
        }
        showRewardedAdInternal(activity, onRewardEarned, onAdFailed)
    }

    private fun showRewardedAdInternal(
        activity: Activity,
        onRewardEarned: (coinsEarned: Long) -> Unit,
        onAdFailed: (errorMessage: String) -> Unit
    ) {
        if (isRewardedLoaded) {
            // Ad is preloaded, show directly
            executeShow(activity, onRewardEarned, onAdFailed)
            return
        }

        // On-demand load & show
        isRewardedLoading = true
        Log.d(TAG, "Ad not cached; performing on-demand load for $REWARDED_PLACEMENT_ID...")
        try {
            UnityAds.load(
                REWARDED_PLACEMENT_ID,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String) {
                        Log.i(TAG, "On-demand rewarded ad loaded for $placementId. Showing immediately.")
                        isRewardedLoaded = true
                        isRewardedLoading = false
                        activity.runOnUiThread {
                            executeShow(activity, onRewardEarned, onAdFailed)
                        }
                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String,
                        error: UnityAds.UnityAdsLoadError,
                        message: String
                    ) {
                        Log.e(TAG, "On-demand rewarded ad failed to load ($placementId): $error - $message")
                        isRewardedLoaded = false
                        isRewardedLoading = false
                        activity.runOnUiThread {
                            onAdFailed("Live ad currently unavailable ($message). Please try again shortly.")
                        }
                        // Schedule automatic reload
                        mainHandler.postDelayed({ preloadRewardedAd(activity.applicationContext) }, 10_000L)
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Error loading ad on demand", e)
            isRewardedLoading = false
            activity.runOnUiThread {
                onAdFailed("Ad network error: ${e.localizedMessage ?: "Please try again later"}")
            }
        }
    }

    private fun executeShow(
        activity: Activity,
        onRewardEarned: (coinsEarned: Long) -> Unit,
        onAdFailed: (errorMessage: String) -> Unit
    ) {
        isRewardedLoaded = false // Mark consumed

        val showOptions = UnityAdsShowOptions()
        try {
            UnityAds.show(
                activity,
                REWARDED_PLACEMENT_ID,
                showOptions,
                object : IUnityAdsShowListener {
                    override fun onUnityAdsShowStart(placementId: String) {
                        Log.i(TAG, "Unity Rewarded Ad started playback: $placementId")
                    }

                    override fun onUnityAdsShowClick(placementId: String) {
                        Log.d(TAG, "Unity Rewarded Ad clicked: $placementId")
                    }

                    override fun onUnityAdsShowComplete(
                        placementId: String,
                        state: UnityAds.UnityAdsShowCompletionState
                    ) {
                        Log.i(TAG, "Unity Rewarded Ad show completed with state: $state")
                        activity.runOnUiThread {
                            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                Log.i(TAG, "Ad fully watched! Granting 500 coin reward.")
                                onRewardEarned(500L)
                            } else {
                                Log.w(TAG, "Ad was skipped before completion.")
                                onAdFailed("Ad was skipped. Complete the full video to claim free coins.")
                            }
                        }
                        // Immediately reload for the next session/request
                        preloadRewardedAd(activity.applicationContext)
                    }

                    override fun onUnityAdsShowFailure(
                        placementId: String,
                        error: UnityAds.UnityAdsShowError,
                        message: String
                    ) {
                        Log.e(TAG, "Unity Rewarded Ad show failure ($placementId): $error - $message")
                        activity.runOnUiThread {
                            onAdFailed("Unable to display ad ($message). Please try again.")
                        }
                        // Automatically reload
                        preloadRewardedAd(activity.applicationContext)
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception showing Unity ad", e)
            activity.runOnUiThread {
                onAdFailed("Ad playback error: ${e.localizedMessage ?: "Unknown"}")
            }
            preloadRewardedAd(activity.applicationContext)
        }
    }

    /**
     * Creates and configures a BannerView with the given banner placement ID (BP_Banner_Android),
     * setting up listener callbacks and automatic retry logic.
     */
    fun createBannerView(
        activity: Activity,
        widthDp: Int = 320,
        heightDp: Int = 50,
        onBannerLoaded: (() -> Unit)? = null,
        onBannerFailed: ((String) -> Unit)? = null
    ): BannerView {
        val bannerSize = UnityBannerSize(widthDp, heightDp)
        val bannerView = BannerView(activity, BANNER_PLACEMENT_ID, bannerSize)

        bannerView.listener = object : BannerView.Listener() {
            override fun onBannerLoaded(view: BannerView?) {
                Log.i(TAG, "Unity Banner ad loaded successfully (Placement: $BANNER_PLACEMENT_ID)")
                activity.runOnUiThread {
                    onBannerLoaded?.invoke()
                }
            }

            override fun onBannerShown(view: BannerView?) {
                Log.d(TAG, "Unity Banner ad shown: $BANNER_PLACEMENT_ID")
            }

            override fun onBannerClick(view: BannerView?) {
                Log.d(TAG, "Unity Banner ad clicked")
            }

            override fun onBannerFailedToLoad(view: BannerView?, errorInfo: BannerErrorInfo?) {
                val errorMsg = errorInfo?.errorMessage ?: "Banner failed to load"
                Log.w(TAG, "Unity Banner failed to load: $errorMsg (code: ${errorInfo?.errorCode})")
                activity.runOnUiThread {
                    onBannerFailed?.invoke(errorMsg)
                }

                // Automatic reload after 15 seconds to ensure banner ad stays active
                mainHandler.postDelayed({
                    try {
                        view?.load()
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error reloading banner", e)
                    }
                }, 15_000L)
            }

            override fun onBannerLeftApplication(view: BannerView?) {
                Log.d(TAG, "User left application from banner ad")
            }
        }

        return bannerView
    }
}
