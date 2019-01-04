package com.sduduzog.slimlauncher.ui.main

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.sduduzog.slimlauncher.MainActivity
import com.sduduzog.slimlauncher.R
import kotlinx.android.synthetic.main.main_bottom_sheet.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.main_fragment.*
import java.text.SimpleDateFormat
import java.util.*


class MainFragment : Fragment(), MainActivity.OnBackPressedListener {

    private lateinit var receiver: BroadcastReceiver
    private lateinit var sheetBehavior: BottomSheetBehavior<FrameLayout>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainAppsList.adapter = HomeAppsAdapter(this)
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)
        optionsView.alpha = 0.0f
        setEventListeners()
        setupBottomSheet()
    }

    override fun onStart() {
        super.onStart()
        receiver = ClockReceiver()
        activity?.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_TICK))
        sheetBehavior.state = STATE_COLLAPSED
        doBounceAnimation(ivExpand)
    }

    private fun setLightStatusBar(window: Window, view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val flags = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
            window.statusBarColor = Color.WHITE
        }
    }

    private fun clearLightStatusBar(window: Window, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val value = TypedValue()
            context.theme.resolveAttribute(R.attr.colorPrimary, value, true)
            window.statusBarColor = value.data
        }
    }

    override fun onResume() {
        super.onResume()
        val settings = context!!.getSharedPreferences(getString(R.string.prefs_settings), AppCompatActivity.MODE_PRIVATE)
        val active = settings.getInt(getString(R.string.prefs_settings_key_theme), 0)
        if (active == 0) {
            setLightStatusBar(activity!!.window, main)
        } else {
            clearLightStatusBar(activity!!.window, context!!)
        }
        updateUi()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        with(context as MainActivity) {
            this.onBackPressedListener = this@MainFragment
        }
    }

    override fun onDetach() {
        super.onDetach()
        with(context as MainActivity) {
            this.onBackPressedListener = null
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(receiver)
    }

    override fun onBackPress() {
        sheetBehavior.state = STATE_COLLAPSED
    }

    override fun onBackPressed() {
        // Do nothing
    }

    private fun setEventListeners() {
        clockTextView.setOnClickListener {
            try {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } finally {
                // Do nothing
            }
        }


        val settings = context!!.getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
        val isChecked = settings.getBoolean(getString(R.string.prefs_settings_key_app_dialer), false)
        ivCall.setOnClickListener {
            if (isChecked) {
                getCallingPermission()
            } else {
                try {
                    val intent = Intent(Intent.ACTION_DIAL)
                    startActivity(intent)
                } catch (e: Exception) {
                    // Do nothing
                }
            }
        }
        ivCall.setOnLongClickListener {
            if (isChecked) {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, null)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Do nothing
                }
            }
            true
        }
        ivExpand.setOnClickListener {
            if (sheetBehavior.state == STATE_COLLAPSED) sheetBehavior.state = STATE_HALF_EXPANDED
        }

        ivCamera.setOnClickListener {
            try {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                startActivity(intent)
            } catch (e: Exception) {
                // Do nothing
            }
        }
    }

    private fun setupBottomSheet() {
        bottomSheet.setOnClickListener {
            // Do nothing
        }
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
                val multi = 3 * p1
                optionsView.alpha = multi
                optionsView.cardElevation = p1 * 8
                optionsView.elevation = p1 * 8
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                iconTray.visibility = View.GONE
                if (newState == STATE_COLLAPSED) {
                    iconTray.visibility = View.VISIBLE
                }
            }
        })
        textView12.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainFragment_to_notesListFragment))
        settingsText.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainFragment_to_settingsFragment))

        rateAppText.setOnClickListener {
            val uri = Uri.parse("market://details?id=" + context?.packageName)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + context?.packageName)))
            }
        }
        changeLauncherText.setOnClickListener {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }
        aboutText.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainFragment_to_aboutFragment))

        deviceSettingsText.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        changeLauncherText.setOnClickListener {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }
    }

    private fun doBounceAnimation(targetView: View) {
        targetView.animate()
                .setStartDelay(500)
                .translationYBy(-20f).withEndAction {
                    targetView.animate()
                            .setStartDelay(0)
                            .translationYBy(20f).duration = 100
                }.duration = 100
    }

    fun updateUi() {
        val twenty4Hour = context?.getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
                ?.getBoolean(getString(R.string.prefs_settings_key_clock_type), false)
        val date = Date()
        if (twenty4Hour as Boolean) {
            val fWatchTime = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            clockTextView.text = fWatchTime.format(date)
            clockAmPm.visibility = View.GONE
        } else {
            val fWatchTime = SimpleDateFormat("hh:mm", Locale.ENGLISH)
            val fWatchTimeAP = SimpleDateFormat("aa", Locale.ENGLISH)
            clockTextView.text = fWatchTime.format(date)
            clockAmPm.text = fWatchTimeAP.format(date)
            clockAmPm.visibility = View.VISIBLE
        }
        val fWatchDate = SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)
        dateTextView.text = fWatchDate.format(date)
    }

    private fun getCallingPermission() {
        if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
        } else {
            Navigation.findNavController(main_content).navigate(R.id.action_mainFragment_to_dialerFragment)
        }
    }

    inner class ClockReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            updateUi()
        }
    }

    companion object {
        const val REQUEST_PHONE_CALL = 1
    }
}
