package com.example.hackadash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ScrollView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


private const val ARG_PARAM1 = "info"

class Studentsdata : Fragment() {
    var list: List<Map<String, Any>>? = null
//    var filtlist: MutableList<Map<String, Any>> = mutableListOf()

    private var info: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            info = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_studentsdata, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = Configuration.AppStorage.GetSheet(requireContext())
        val infoV = view.findViewById<TextView>(R.id.tV_Dinfo)
        val search = view.findViewById<SearchView>(R.id.searchView)
        val sugglist = view.findViewById<ListView>(R.id.listView)
        val btFetch = view.findViewById<Button>(R.id.bt_Dfetch)
        val graph = view.findViewById<GraphView>(R.id.graph)
        val scroll = view.findViewById<ScrollView>(R.id.scrollView2)
        infoV.text = info
        val webapp = Configuration.AppStorage.loadURL(requireContext()).toString()
        btFetch.setOnClickListener {
            call(webapp)
        }

        val names = list?.map { it["Full Name"].toString() } ?: listOf()
        val adapsearch = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            names
        )
        sugglist.adapter = adapsearch
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(Q: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newT: String?): Boolean {
                adapsearch.filter.filter(newT)
                return true
            }
        })


        sugglist.setOnItemClickListener { _, _, position, _ ->
            val selected = adapsearch.getItem(position)
            val wholeinfo = list?.find { it["Full Name"] == selected }

            if (wholeinfo != null) {
                info = "Member's Data:\n"
                for ((K, information) in wholeinfo) {
                    info += "-> ${K}:\n    ${information}\n\n"
                }
                infoV.text = info

                val ser = LineGraphSeries<DataPoint>()
                var totpoints = 60.0

                for (i in 1..32) {
                    val session = "Points ${i}"

                    val points = wholeinfo[session]?.toString()?.toDoubleOrNull() ?: 0.0
                    totpoints += points

                    ser.appendData(DataPoint(i.toDouble(), totpoints), true, 40)

                }

                graph.removeAllSeries()
                graph.addSeries(ser)
                styleGraph(graph, ser, "Total Points Over Time")


            }


        }


    }

    private fun call(URL: String) {
        val cl = OkHttpClient()
        var Urlreq = "$URL?action=GetSheetData"
        val request = Request.Builder().url(Urlreq).build()
        cl.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Connection Failed", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                activity?.runOnUiThread {
                    val gson = Gson()
                    val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                    list = gson.fromJson(json, type)
                    Configuration.AppStorage.savesheet(requireContext(), list)
                    val data = list?.map { it["Full Name"].toString() } ?: listOf()
                    val adapsearch = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        data
                    )
                    Toast.makeText(context, "Data Updated!", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

    private fun styleGraph(graph: GraphView, series: LineGraphSeries<DataPoint>, title: String) {
        graph.title = title
        graph.titleColor = android.graphics.Color.WHITE
        series.thickness = 10
        series.color = android.graphics.Color.parseColor("#00BCD4")
        series.isDrawDataPoints = true
        series.dataPointsRadius = 9f
        val gridRenderer = graph.gridLabelRenderer
        gridRenderer.horizontalAxisTitle = "Sessions"
        gridRenderer.verticalAxisTitle = "Total Points"
        gridRenderer.horizontalAxisTitleColor = android.graphics.Color.GRAY
        gridRenderer.verticalAxisTitleColor = android.graphics.Color.GRAY
        gridRenderer.numHorizontalLabels = 6
        gridRenderer.numVerticalLabels = 7
        gridRenderer.labelHorizontalHeight = 50
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(32.0)
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(150.0)

    }

    companion object {

        @JvmStatic
        fun newInstance(info: String, param2: String) =
            Studentsdata().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, info)
                }
            }
    }
}