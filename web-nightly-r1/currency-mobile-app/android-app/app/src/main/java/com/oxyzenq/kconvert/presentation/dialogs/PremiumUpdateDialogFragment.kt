package com.oxyzenq.kconvert.presentation.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.oxyzenq.kconvert.R

class PremiumUpdateDialogFragment : DialogFragment() {

    interface UpdateListener {
        fun onUpdateNow()
        fun onRejectUpdate()
        fun onDontAskAgain()
    }

    companion object {
        private const val ARG_VERSION = "version"
        private const val ARG_RELEASE_NOTES = "release_notes"

        fun newInstance(version: String, releaseNotes: String): PremiumUpdateDialogFragment {
            val fragment = PremiumUpdateDialogFragment()
            val args = Bundle()
            args.putString(ARG_VERSION, version)
            args.putString(ARG_RELEASE_NOTES, releaseNotes)
            fragment.arguments = args
            return fragment
        }
    }

    private var updateListener: UpdateListener? = null
    private lateinit var version: String
    private lateinit var releaseNotes: String

    fun setUpdateListener(listener: UpdateListener) {
        this.updateListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            version = it.getString(ARG_VERSION, "")
            releaseNotes = it.getString(ARG_RELEASE_NOTES, "")
        }
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_update_premium, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set version text
        val tvVersion = view.findViewById<TextView>(R.id.tv_version)
        tvVersion.text = "Version $version is ready to install"
        
        // Set release notes if available
        val layoutReleaseNotes = view.findViewById<View>(R.id.layout_release_notes)
        val tvReleaseNotes = view.findViewById<TextView>(R.id.tv_release_notes)
        
        if (releaseNotes.isNotEmpty()) {
            layoutReleaseNotes.visibility = View.VISIBLE
            tvReleaseNotes.text = releaseNotes
        } else {
            layoutReleaseNotes.visibility = View.GONE
        }
        
        // Set button listeners
        val btnUpdateNow = view.findViewById<AppCompatButton>(R.id.btn_update_now)
        val btnRejectUpdate = view.findViewById<AppCompatButton>(R.id.btn_reject_update)
        val btnDontAskAgain = view.findViewById<AppCompatButton>(R.id.btn_dont_ask_again)
        
        btnUpdateNow.setOnClickListener {
            updateListener?.onUpdateNow()
            dismiss()
        }
        
        btnRejectUpdate.setOnClickListener {
            updateListener?.onRejectUpdate()
            dismiss()
        }
        
        btnDontAskAgain.setOnClickListener {
            updateListener?.onDontAskAgain()
            dismiss()
        }
        
        // Make dialog non-cancelable
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
