package com.dinhlam.sharebox.ui.passcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityPasscodeBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.extensions.screenWidth
import com.dinhlam.sharebox.extensions.vibrate
import com.dinhlam.sharebox.modelview.TextModelView
import java.util.Stack

class PasscodeActivity : BaseActivity<ActivityPasscodeBinding>() {

    companion object {
        private const val PASSCODE_LENGTH = 6
        private const val CHARACTER_CODE_EMPTY = "◦"
        private const val CHARACTER_CODE_FILLED = "•"
        private const val CHARACTER_CODE_DONE = "✓"
        private const val CHARACTER_CODE_BACKSPACE = "⌫"
    }

    override fun onCreateViewBinding(): ActivityPasscodeBinding {
        return ActivityPasscodeBinding.inflate(layoutInflater)
    }

    private val originalPasscode: String by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        intent.getStringExtra(AppExtras.EXTRA_PASSCODE) ?: ""
    }

    private val stack = Stack<Int>()

    private val codeBulletSize: Int by lazy {
        val screenWidth = screenWidth()
        val space = screenWidth - 32.dp()
        space.times(0.8f).div(PASSCODE_LENGTH).toInt()
    }

    private val passcodeAdapter = BaseListAdapter.createAdapter {
        repeat(stack.size) { number ->
            add(
                TextModelView(
                    "code_filled_$number",
                    CHARACTER_CODE_FILLED,
                    width = codeBulletSize,
                    height = codeBulletSize,
                    textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle1
                )
            )
        }


        repeat(PASSCODE_LENGTH - stack.size) { number ->
            add(
                TextModelView(
                    "code_empty_$number",
                    CHARACTER_CODE_EMPTY,
                    width = codeBulletSize,
                    height = codeBulletSize,
                    textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle1
                )
            )
        }
    }

    private val keypadAdapter = BaseListAdapter.createAdapter {
        repeat(9) { number ->
            add(
                TextModelView(
                    "text_number_$number",
                    "${number + 1}",
                    height = 70.dp(),
                    textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle1,
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        onNumberClicked(number + 1)
                    })
                )
            )
        }

        add(
            TextModelView(
                "text_done",
                CHARACTER_CODE_DONE,
                height = 70.dp(),
                textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle1,
                actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                    onDone()
                })
            )
        )

        add(
            TextModelView(
                "text_zero",
                "0",
                height = 70.dp(),
                textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle1,
                actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                    onNumberClicked(0)
                })
            )
        )

        add(
            TextModelView(
                "text_backspace",
                CHARACTER_CODE_BACKSPACE,
                height = 70.dp(),
                textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle1,
                actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                    onBackspace()
                })
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.recyclerViewCode.itemAnimator = null
        viewBinding.recyclerViewCode.adapter = passcodeAdapter
        passcodeAdapter.requestBuildModelViews()

        viewBinding.recyclerViewKeypad.adapter = keypadAdapter
        keypadAdapter.requestBuildModelViews()

        intent.getStringExtra(AppExtras.EXTRA_PASSCODE_DESCRIPTION)?.let { passcodeDescription ->
            viewBinding.textDesc.text = passcodeDescription
        }
    }

    private fun onNumberClicked(number: Int) {
        if (stack.size < PASSCODE_LENGTH) {
            stack.push(number)
            passcodeAdapter.requestBuildModelViews()
        }
    }

    private fun onBackspace() {
        if (stack.isNotEmpty()) {
            stack.pop()
            passcodeAdapter.requestBuildModelViews()
        }
    }

    private fun onDone() {
        if (stack.size < PASSCODE_LENGTH) {
            notifyPasscodeInvalid()
        } else {
            val code = stack.joinToString(separator = "")
            if (originalPasscode.isNotEmpty()) {
                handleForValidate(code)
            } else {
                handleForCreateNew(code)
            }
        }
    }

    private fun handleForValidate(code: String) {
        val encryptCode = code.md5()
        if (originalPasscode != encryptCode) {
            notifyPasscodeInvalid()
            stack.clear()
            passcodeAdapter.requestBuildModelViews()
        } else {
            val returnIntent = Intent()
            returnIntent.putExtras(intent)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    private fun handleForCreateNew(code: String) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AppExtras.EXTRA_PASSCODE, code)
        })
        finish()
    }

    private fun notifyPasscodeInvalid() {
        vibrate(500)
        viewBinding.cardLock.startAnimation(
            AnimationUtils.loadAnimation(
                this, R.anim.lock_shake
            )
        )
        viewBinding.recyclerViewCode.startAnimation(
            AnimationUtils.loadAnimation(
                this, R.anim.passcode_shake
            )
        )
    }
}