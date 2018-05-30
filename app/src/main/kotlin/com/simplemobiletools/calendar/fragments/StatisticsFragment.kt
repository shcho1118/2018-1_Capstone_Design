package com.simplemobiletools.calendar.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.EventActivity
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.calendar.adapters.EventListAdapter
import com.simplemobiletools.calendar.extensions.*
import com.simplemobiletools.calendar.helpers.EVENT_ID
import com.simplemobiletools.calendar.helpers.EVENT_OCCURRENCE_TS
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.models.Event
import com.simplemobiletools.calendar.models.ListEvent
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.fragment_statistics.view.*
import org.joda.time.DateTime
import java.util.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast

class StatisticsFragment : MyFragmentHolder(), RefreshRecyclerViewListener, OnItemSelectedListener {
    private var mEvents: List<Event> = ArrayList()
    private var prevEventsHash = 0
    private var totalEvents = 0
    private var finishedEvents = 0
    lateinit var mView: View
    private var mPie: PieChart? = null
    private var mPie2: PieChart? = null
    lateinit var spinner: Spinner
    lateinit var spinner2: Spinner
    private val cal = Calendar.getInstance()
    private var selectedYear = cal.get(Calendar.YEAR) // needs to be changed later
    private var selectedMonth = cal.get(Calendar.MONTH) + 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_statistics, container, false)
        //val placeholderText = String.format(getString(R.string.two_string_placeholder), "${getString(R.string.no_upcoming_events)}\n", getString(R.string.add_some_events))
        //mView.calendar_empty_list_placeholder.text = placeholderText
        mView.calendar_schedule_performance.setTextColor(activity!!.config.textColor)
        mView.calendar_schedule_proportion.setTextColor(activity!!.config.textColor)
        mView.year.setTextColor(activity!!.config.textColor)
        mView.month.setTextColor(activity!!.config.textColor)
        mView.background = ColorDrawable(context!!.config.backgroundColor)

        var listYear: ArrayList<String> = ArrayList()
        for(i in 0..6)
            listYear.add( (selectedYear-3+i).toString() )

        spinner = mView.year_spinner
        spinner.setOnItemSelectedListener(this);
        val adapter = ArrayAdapter<String>(context, R.layout.spinner_item, listYear)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.setAdapter(adapter)
        val spinnerPosition = adapter.getPosition(selectedYear.toString())
        spinner.setSelection(spinnerPosition)

        spinner2 = mView.month_spinner
        spinner2.setOnItemSelectedListener(this);
        val adapter2 = ArrayAdapter.createFromResource(context, R.array.month_array, R.layout.spinner_item)
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner2.setAdapter(adapter2)
        val spinnerPosition2 = adapter2.getPosition(selectedMonth.toString())
        spinner2.setSelection(spinnerPosition2)

        updateActionBarTitle()
        setupPieChartView()
        return mView
    }

    override fun onResume() {
        super.onResume()
        checkEvents()
    }

    override fun onPause() {
        super.onPause()
        checkEvents()
    }

    private fun checkEvents() {

        val fromTS = DateTime().withDate(selectedYear,selectedMonth,1).seconds()
        val toTS: Int
        if(selectedMonth+1>12)
            toTS = DateTime().withDate(selectedYear+1,1,1).seconds()
        else
            toTS = DateTime().withDate(selectedYear,selectedMonth+1,1).seconds()
        context!!.dbHelper.getEvents(fromTS, toTS) {
            receivedEvents(it)
        }
    }

    private fun receivedEvents(events: MutableList<Event>) {
        if (context == null || activity == null) {
            return
        }

        val filtered = context!!.getFilteredEvents(events)
        val hash = filtered.hashCode()
        if (prevEventsHash == hash) {
                return
        }

        prevEventsHash = hash
        mEvents = filtered
        val listItems = context!!.getEventListItems(mEvents)

        totalEvents = mEvents.size
        finishedEvents = 0
        var cMap = mutableMapOf<String, Int>()

        for (i in 0..mEvents.size-1){
            if(mEvents[i].isFinished)
                finishedEvents+=1
            if( !cMap.containsKey(mEvents[i].category) )
                cMap.put(mEvents[i].category, 1)
            else {
                val prev = cMap.get(mEvents[i].category)
                if(prev != null)
                    cMap.set(mEvents[i].category, prev + 1)
            }
        }

        val sortedMap = cMap.toList().sortedByDescending { (_, value) -> value }.toMap()

        if(totalEvents != 0){
            /**
             * サンプルデータ
             */

            val performance_percentage = finishedEvents.toFloat()/totalEvents.toFloat()*100f
            val value = Arrays.asList(performance_percentage, 100f-performance_percentage)
            val label = Arrays.asList("완료 일정", "미수행 일정")
            val entry = ArrayList<PieEntry>()
            for(i in value.indices) {
                entry.add( PieEntry(value.get(i), label.get(i)) )
            }

            var popularSum = 0
            var value2 = mutableListOf<Float>()
            var label2 = mutableListOf<String>()
            var limit = 0
            var categoryNum = 0
            for((category, count) in sortedMap){
                value2.add( count.toFloat()/totalEvents.toFloat()*100f )
                popularSum += count
                categoryNum += 1
                if(category == "")
                    label2.add("랜덤")
                else
                    label2.add( category )
                limit += 1
                if(limit == 3)
                    break
            }
            if(popularSum < totalEvents){
                value2.add( 100f - popularSum.toFloat()/totalEvents.toFloat()*100f )
                label2.add("기타")
                categoryNum += 1
            }
            val entry2 = ArrayList<PieEntry>()
            for(i in value2.indices) {
                entry2.add( PieEntry(value2.get(i), label2.get(i)) )
            }

            /**
             * ラベル
             */
            val dataSet = PieDataSet(entry, "")
            dataSet.colors = Arrays.asList(context!!.config.primaryColor, Color.LTGRAY)//ColorTemplate.COLORFUL_COLORS.toList()
            dataSet.setDrawValues(true)

            val pieData = PieData(dataSet)
            pieData.setValueFormatter(PercentFormatter())
            pieData.setValueTextSize(15f)
            pieData.setValueTextColor(context!!.config.textColor)

            mPie?.data = pieData

            val dataSet2 = PieDataSet(entry2, "")
            var colors = mutableListOf<Int>()
            for(i in 0..categoryNum-1) {
                if(categoryNum == 1) {
                    colors.add(ColorTemplate.COLORFUL_COLORS[0])
                    break
                }
                if(i<categoryNum-1)
                    colors.add(ColorTemplate.COLORFUL_COLORS[i])
                else
                    colors.add(Color.LTGRAY)
            }
            dataSet2.colors = colors
            dataSet2.setDrawValues(true)

            val pieData2 = PieData(dataSet2)
            pieData2.setValueFormatter(PercentFormatter())
            pieData2.setValueTextSize(15f)
            pieData2.setValueTextColor(context!!.config.textColor)

            mPie2?.data = pieData2
        }

        activity?.runOnUiThread {
            if(totalEvents != 0){
                mPie?.setVisibility(View.VISIBLE)
                mPie2?.setVisibility(View.VISIBLE)
            }
            else {
                mPie?.setVisibility(View.GONE)
                mPie2?.setVisibility(View.GONE)
            }
            mView.calendar_schedule_performance.text = "${getString(R.string.schedule_performance)} ${finishedEvents.toString()}/${totalEvents.toString()}"
            //mView.calendar_schedule_performance.text = "${selectedYear.toString()} year ${selectedMonth.toString()} month"
            mPie?.invalidate()
            var proportionString = "${getString(R.string.schedule_proportion)}\n"
            for((category, count) in sortedMap){
                proportionString += "\nCategory: $category, Count: $count"
            }
            mView.calendar_schedule_proportion.text = proportionString
            mPie2?.invalidate()
        }
    }

    override fun refreshItems() {
        checkEvents()
    }

    override fun goToToday() {
    }

    override fun refreshEvents() {
        checkEvents()
    }

    override fun shouldGoToTodayBeVisible() = false

    override fun updateActionBarTitle() {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.schedule_statistics)
    }

    override fun getNewEventDayCode() = Formatter.getTodayCode(context!!)

    fun setupPieChartView() {
        mPie = mView.piechart_performance

        mPie?.setUsePercentValues(true)

        val desc: Description = Description()
        desc.text = ""
        mPie?.description = desc

        var legend: Legend? = mPie?.legend
        legend?.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT

        mPie?.isRotationEnabled = false

        mPie2 = mView.piechart_proportion

        mPie2?.setUsePercentValues(true)

        mPie2?.description = desc

        legend = mPie2?.legend
        legend?.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT

        mPie2?.isRotationEnabled = false
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View,
                       pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        val spinner = parent as Spinner
        if(spinner.getId() == R.id.year_spinner) {
            if(selectedYear != parent.getItemAtPosition(pos).toString().toInt()) {
                selectedYear = parent.getItemAtPosition(pos).toString().toInt()
                checkEvents()
            }
        }
        else if(spinner.getId() == R.id.month_spinner)
            if(selectedMonth != parent.getItemAtPosition(pos).toString().toInt()) {
                selectedMonth = parent.getItemAtPosition(pos).toString().toInt()
                checkEvents()
            }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }
}
