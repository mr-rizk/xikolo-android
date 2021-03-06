package de.xikolo.controllers.dialogs.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import butterknife.ButterKnife
import com.yatatsu.autobundle.AutoBundle

abstract class BaseDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            // restore
            AutoBundle.bind(this, savedInstanceState)
        } else {
            AutoBundle.bind(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        AutoBundle.pack(this, outState)
    }

}
