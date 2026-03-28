    package com.example.hackadash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import okhttp3.Request
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.w3c.dom.Text
import java.io.IOException

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "Logs"
private const val ARG_PARAM2 = "ff"

    data class Course(
        val name: String,
        val id: String
    )

    data class Things(
        val name: String,
        val email: String,
        val userId: String
    )

class Control : Fragment() {

    private var Logs: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            Logs = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btRun = view.findViewById<Button>(R.id.bT_CRun)
        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val logwin = view.findViewById<EditText>(R.id.tF_CLogs)
        val arg1text = view.findViewById<TextView>(R.id.tV_CArg1)
        val arg2text = view.findViewById<TextView>(R.id.tV_CArg2)

        val functions = arrayOf("Choose a function" ,"GetClassroomsIDs", "GetClassroomMembersData", "SyncSheet&Classroom")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, functions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        btRun.setOnClickListener {
            val function = spinner.selectedItem.toString()

            val URL = Configuration.AppStorage.loadURL(requireContext()).toString()
            val CID = Configuration.AppStorage.loadCID(requireContext()).toString()
            call(URL,function, CID, logwin)
            Toast.makeText(requireContext(), "Running please wait...", Toast.LENGTH_SHORT).show()

        }
    }

    private fun call(URL: String, action: String, CID: String?, LOGwin: EditText){
        val cl = OkHttpClient()
        var Urlreq = "$URL?action=$action"
        if (action == "SyncSheet&Classroom" || action == "GetClassroomMembersData"){
            Urlreq += "&cid=$CID"
        }
        val request = Request.Builder().url(Urlreq).build()
        cl.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Connection Failed", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                activity?.runOnUiThread {
                    val gson = Gson()
                    if (action == "GetClassroomsIDs") {
                        val type = object : TypeToken<List<Course>>() {}.type
                        val list: List<Course> = gson.fromJson(json, type)
                        var courses = ""
                        activity?.runOnUiThread {
                            for (course in list) {
                                courses += "--> Found Course: (${course.name}) and the ID is (${course.id})\n\n"
                            }
                        }
                        LOGwin.setText(courses)
                    } else if (action == "GetClassroomMembersData"){
                        val type = object : TypeToken<List<Things>>() {}.type
                        val list: List<Things> = gson.fromJson(json, type)
                        var IDs = ""

                        activity?.runOnUiThread {
                            for (id in list){
                                IDs += "--> Name: (${id.name}) and Email: (${id.email}) and ID: (${id.userId})\n\n"
                            }
                        }
                        LOGwin.setText(IDs)
                    } else if (action == "SyncSheet&Classroom") {
                        LOGwin.setText(json)

                    } else {
                        Toast.makeText(requireContext(), "Please choose a function", Toast.LENGTH_LONG).show()

                    }
                    Logs = LOGwin.text.toString()
                }
            }
        }
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(Logs: String, param2: String) =
            Control().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, Logs)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}