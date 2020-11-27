package com.example.studydemo

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.studydemo.activity.*
import com.example.studydemo.activity.fragment.MyFragmentActivity
import com.example.studydemo.activity.launchmode.LaunchActivityA
import com.example.studydemo.utils.ImageUtils
import com.example.studydemo.utils.PermissionUtil
import com.example.studydemo.utils.ToastKeeper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_lottie.setOnClickListener(this)
        btn_coordinator.setOnClickListener(this)
        btn_annotation.setOnClickListener(this)
        btn_toast.setOnClickListener(this)
        btn_camera.setOnClickListener(this)
        btn_leak.setOnClickListener(this)
        btn_send_cmd.setOnClickListener(this)
        btn_agora.setOnClickListener(this)
        btn_screen_shot.setOnClickListener(this)
        btn_timer.setOnClickListener(this)
        btn_scroll_view.setOnClickListener(this)
        btn_save_img.setOnClickListener(this)
        btn_launch_mode.setOnClickListener(this)
        btn_fragment_crash.setOnClickListener(this)
        btn_remove.setOnClickListener(this)
        btn_dialog.setOnClickListener(this)

    }

    var index = 0

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_lottie -> startActivity(Intent(this, LottieActivity::class.java))
            R.id.btn_coordinator -> startActivity(Intent(this, CoordinatorActivity::class.java))
            R.id.btn_toast -> {
                index += 1
                val content = index % 10
                Log.e("ToastTest", "index=" + index + "  index%10=" + content)
                ToastKeeper.getInstance().createBuilder(this).setMessage("" + content)
                    .setDuration(ToastKeeper.DURATION_SHORT).show()
            }
            R.id.btn_camera -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Android 5.0及以上
//                    startActivity(Intent(this, CameraPreviewActivity::class.java))
                    startActivity(Intent(this, CameraViewActivity::class.java))
                } else {
                    startActivity(Intent(this, CameraPreviewLowerActivity::class.java))
                }
            }

            R.id.btn_leak -> startActivity(Intent(this, LeakTestActivity::class.java))

            R.id.btn_send_cmd -> CMDMsgManager.getInstance().receiveOnEnterRoom(666, "是我呀")

            R.id.btn_agora -> {

            }

            R.id.btn_screen_shot -> {
                startActivity(Intent(this, ScreenShotActivity::class.java))
            }

            R.id.btn_timer -> {
                startActivity(Intent(this, CountDownTimerActivity::class.java))
            }

            R.id.btn_scroll_view -> {
                startActivity(Intent(this, ScrollViewActivity::class.java))
            }

            R.id.btn_save_img -> {
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg)
                if (PermissionUtil.hasWritePermission(this) && PermissionUtil.hasReadPermission(this)) {
                    ImageUtils.saveBitmapToCamera(this, bitmap, "img_123")
                } else {
                    Toast.makeText(this, "没有存储权限", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.btn_launch_mode -> {
                startActivity(Intent(this, LaunchActivityA::class.java))

//                intent = Intent()
//                intent.action = "android.intent.action.VIEW"
//                intent.addCategory("android.intent.category.DEFAULT")
//                intent.data = Uri.parse("lpdm://dm")
//                startActivity(intent)

            }

            R.id.btn_fragment_crash -> {
                startActivity(Intent(this, MyFragmentActivity::class.java))
            }

            R.id.btn_remove -> {
                startActivity(Intent(this, RemoveActivity::class.java))
            }

            R.id.btn_dialog -> {
                startActivity(Intent(this, DialogActivity::class.java))
            }

        }

    }
}
