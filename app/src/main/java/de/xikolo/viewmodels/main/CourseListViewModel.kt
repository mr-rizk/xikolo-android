package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Course
import de.xikolo.models.CourseDate
import de.xikolo.models.dao.CourseDao
import de.xikolo.models.dao.DatesDao
import de.xikolo.models.dao.EnrollmentDao
import de.xikolo.network.jobs.ListCoursesJob
import de.xikolo.network.jobs.ListDatesJob
import de.xikolo.utils.SectionList
import de.xikolo.viewmodels.base.BaseViewModel

class CourseListViewModel(private val filter: CourseListFilter) : BaseViewModel() {

    private val coursesDao = CourseDao(realm)
    private val datesDao = DatesDao(realm)

    val enrollmentCount
        get() = EnrollmentDao.Unmanaged.count()

    val enrolledCourses: LiveData<List<Course>> by lazy {
        coursesDao.allEnrolled()
    }

    val dates: LiveData<List<CourseDate>> by lazy {
        datesDao.futureDates()
    }

    val todaysDateCount: Long
        get() {
            return datesDao.dateCountToday()
        }

    val nextSevenDaysDateCount: Long
        get() {
            return datesDao.dateCountNextSevenDays()
        }

    val futureDateCount: Long
        get() {
            return datesDao.dateCountFuture()
        }

    val nextDate: CourseDate?
        get() {
            return datesDao.nextDate()
        }

    val courses: LiveData<List<Course>> by lazy {
        coursesDao.all()
    }

    val sectionedCourseList: SectionList<String, List<Course>>
        get() {
            return if (filter == CourseListFilter.ALL) {
                courseListFilterAll
            } else {
                courseListFilterMyWithDates
            }
        }

    private val courseListFilterAll: SectionList<String, List<Course>>
        get() {
            val courseList = SectionList<String, List<Course>>()
            var subList: List<Course>
            if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                subList = CourseDao.Unmanaged.allFuture()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.getInstance().getString(R.string.header_future_courses),
                        subList
                    )
                }
                subList = CourseDao.Unmanaged.allCurrentAndPast()
                if (subList.isNotEmpty()) {
                    courseList.add(App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                    )
                }
            } else {
                subList = CourseDao.Unmanaged.allCurrentAndFuture()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                        subList
                    )
                }
                subList = CourseDao.Unmanaged.allPast()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                    )
                }
            }
            return courseList
        }

    private val courseListFilterMyWithDates: SectionList<String, List<Course>>
        get() {
            val courseList = SectionList<String, List<Course>>()

            courseList.add(
                App.getInstance().getString(R.string.course_dates_title),
                listOf(Course())
            )

            var subList = CourseDao.Unmanaged.allCurrentAndPastWithEnrollment()
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.getInstance().getString(R.string.header_my_current_courses),
                    subList
                )
            }
            subList = CourseDao.Unmanaged.allFutureWithEnrollment()
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.getInstance().getString(R.string.header_my_future_courses),
                    subList
                )
            }
            return courseList
        }

    fun searchCourses(query: String): List<Course> =
        CourseDao.Unmanaged.search(query, filter == CourseListFilter.MY)


    override fun onFirstCreate() {
        requestCourseList(false)
        requestDateList(false)
    }

    override fun onRefresh() {
        requestCourseList(true)
        requestDateList(true)
    }

    fun requestCourseList(userRequest: Boolean) {
        ListCoursesJob(networkState, userRequest).run()
    }

    private fun requestDateList(userRequest: Boolean) {
        ListDatesJob(networkState, userRequest).run()
    }
}

