package de.xikolo.controllers.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.base.BaseMetaRecyclerViewAdapter
import de.xikolo.models.Course
import de.xikolo.models.CourseDate
import de.xikolo.models.DateOverview
import de.xikolo.utils.TimeUtil
import java.text.DateFormat
import java.util.*

class DateListAdapter(private val onDateClickListener: OnDateClickListener?) : BaseMetaRecyclerViewAdapter<DateOverview, CourseDate>() {

    companion object {
        val TAG: String = DateListAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_META   ->
                OverviewViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.container_date_overview, parent, false)
                )
            ITEM_VIEW_TYPE_HEADER ->
                HeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
                )
            else                  ->
                CourseDateViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_date_list, parent, false)
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder     -> bindHeaderViewHolder(holder, position)
            is OverviewViewHolder   -> {
                val dateOverview = super.contentList.get(position) as DateOverview
                holder.numberOfDatesToday.text = dateOverview.todaysDateCount.toString()
                holder.numberOfDatesWeek.text = dateOverview.nextSevenDaysDateCount.toString()
                holder.numberOfAllDates.text = dateOverview.futureDateCount.toString()
            }
            is CourseDateViewHolder -> {
                val courseDate = super.contentList.get(position) as CourseDate

                holder.textType.text = courseDate.getTypeString(App.getInstance())

                holder.textCourse.text = Course.get(courseDate.courseId).title
                holder.container.setOnClickListener {
                    onDateClickListener?.onCourseClicked(courseDate.courseId)
                }

                holder.textDate.text = DateFormat.getDateTimeInstance(
                    DateFormat.YEAR_FIELD or DateFormat.MONTH_FIELD or DateFormat.DATE_FIELD,
                    DateFormat.SHORT,
                    Locale.getDefault()
                ).format(courseDate.date)

                holder.textDateTitle.text = courseDate.title

                courseDate.date?.time?.let {
                    holder.textTimeLeft.text = String.format(
                        App.getInstance().getString(R.string.time_left),
                        TimeUtil.getTimeLeftString(
                            it - Date().time,
                            App.getInstance()
                        )
                    )
                }
            }
        }
    }

    interface OnDateClickListener {

        fun onCourseClicked(courseId: String?)
    }

    class OverviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.textNumberOfDatesToday)
        lateinit var numberOfDatesToday: TextView

        @BindView(R.id.textNumberOfDatesWeek)
        lateinit var numberOfDatesWeek: TextView

        @BindView(R.id.textNumberOfAllDates)
        lateinit var numberOfAllDates: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }

    class CourseDateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.container)
        lateinit var container: View

        @BindView(R.id.textDate)
        lateinit var textDate: TextView

        @BindView(R.id.textType)
        lateinit var textType: TextView

        @BindView(R.id.textDateTitle)
        lateinit var textDateTitle: TextView

        @BindView(R.id.textTimeLeft)
        lateinit var textTimeLeft: TextView

        @BindView(R.id.textCourse)
        lateinit var textCourse: TextView

        init {
            ButterKnife.bind(this, view)
        }

    }

}
