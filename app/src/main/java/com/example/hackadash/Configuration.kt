package com.example.hackadash

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private const val ARG_Webapp_Link = "webApp"
private const val ARG_Classroom_ID = "cID"
class Configuration : Fragment() {
    private var webApp: String? = null
    private var cID: String? = null


    object AppStorage {
        private const val PREF = "HackaDashpref"
        private const val KURL = "Type Link here"
        private const val KCID = "Type ClassroomID here"
        private const val KSHEET = "here"

        fun save(context: Context, url: String?, cid: String?){
            val shared = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            shared.edit {
                putString(KURL, url)
                putString(KCID, cid)
                apply()
            }
        }

        fun savesheet(con: Context, sheet: List<Map<String, Any>>?){
            val shared = con.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            val jsonsheet = Gson().toJson(sheet)
            shared.edit {
                putString(KSHEET, jsonsheet)
                apply()
            }
        }

        fun GetSheet (con: Context): List<Map<String, Any>>? {
            val shared = con.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            val jsonsheet = shared.getString(KSHEET, null) ?: return null

            val type = object : TypeToken<List<Map<String, Any>>>() {}.type

            return Gson().fromJson(jsonsheet, type)
        }



        fun loadURL(context: Context): String?{
            val shared = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            return shared.getString(KURL, null)
        }

        fun loadCID(context: Context): String?{
            val shared = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            return shared.getString(KCID, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webApp = AppStorage.loadURL(requireContext())
        cID = AppStorage.loadCID(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tF_cfgwebApp = view.findViewById<EditText>(R.id.tF_cfgWebapp)
        val tF_cfgCID = view.findViewById<EditText>(R.id.tF_cfgCID)
        val  btSave = view.findViewById<Button>(R.id.bt_cfgSave)

        tF_cfgwebApp.setText(webApp)
        tF_cfgCID.setText(cID)

        btSave.setOnClickListener {
            Toast.makeText(requireContext(),"Settings Saved",Toast.LENGTH_SHORT).show()
            webApp = tF_cfgwebApp.text.toString().trim()
            cID = tF_cfgCID.text.toString().trim()
            AppStorage.save(requireContext(), webApp, cID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_configuration, container, false)

    }


    companion object {

        @JvmStatic
        fun newInstance(webApp: String, cID: String) =
            Configuration().apply {
                arguments = Bundle().apply {
                    putString(ARG_Webapp_Link, webApp)
                    putString(ARG_Classroom_ID, cID)
                }
            }
    }
}