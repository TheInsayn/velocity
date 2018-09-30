package com.android.mathias.velocity.util

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.mathias.velocity.R
import com.android.mathias.velocity.db.DBManager
import com.android.mathias.velocity.ext.IBottomSheetListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetCreateRoutes : BottomSheetDialogFragment() {

    private lateinit var mTxtName: EditText
    private lateinit var mTxtHint: TextView
    private lateinit var mBtnSave: Button

    private var mDuplicateName: Boolean = false
    private var mClickListener: IBottomSheetListener? = null

    fun newInstance(): BottomSheetCreateRoutes {
        return BottomSheetCreateRoutes()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sheet_save_route, container, false)

        mTxtName = view.findViewById(R.id.txt_dialog_route_name)
        mTxtHint = view.findViewById(R.id.txt_dialog_name_hint)
        mBtnSave = view.findViewById(R.id.btn_save_route_name)

        val routes = DBManager.getRoutes(context!!, null)

        mTxtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                for (r in routes) {
                    mDuplicateName = r.name!!.contentEquals(charSequence)
                    if (mDuplicateName) break
                }
                if (!mDuplicateName && !mBtnSave.isEnabled) {
                    mBtnSave.isEnabled = true
                    mTxtHint.visibility = View.INVISIBLE
                } else if (mDuplicateName && mBtnSave.isEnabled) {
                    mBtnSave.isEnabled = false
                    mTxtHint.visibility = View.VISIBLE
                }
                mBtnSave.setOnClickListener{mClickListener!!.onRouteNameSaved(mTxtName.text.toString())}
            }
        })
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mClickListener = context as IBottomSheetListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement BottomSheetListener")
        }

    }
}
