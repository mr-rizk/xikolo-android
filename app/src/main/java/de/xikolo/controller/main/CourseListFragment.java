package de.xikolo.controller.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.CourseActivity;
import de.xikolo.controller.CourseDetailsActivity;
import de.xikolo.controller.helper.NotificationController;
import de.xikolo.controller.helper.RefeshLayoutController;
import de.xikolo.controller.main.adapter.CourseListAdapter;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class CourseListFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener,
        CourseListAdapter.OnCourseButtonClickListener {

    public static final String TAG = CourseListFragment.class.getSimpleName();

    public static final String ARG_FILTER = "arg_filter";

    public static final String FILTER_ALL = "filter_all";
    public static final String FILTER_MY = "filter_my";

    private static final String KEY_COURSES = "courses";

    private String mFilter;
    private SwipeRefreshLayout mRefreshLayout;

    private AbsListView mAbsListView;
    private CourseListAdapter mCourseListAdapter;

    private NotificationController mNotificationController;

    private List<Course> mCourses;

    private CourseModel mCourseModel;

    public CourseListFragment() {
        // Required empty public constructor
    }

    public static CourseListFragment newInstance(String filter) {
        CourseListFragment fragment = new CourseListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mFilter = getArguments().getString(ARG_FILTER);
        }
        if (savedInstanceState != null) {
            mCourses = savedInstanceState.getParcelableArrayList(KEY_COURSES);
        }
        setHasOptionsMenu(true);

        mCourseModel = new CourseModel(getActivity(), jobManager, databaseHelper);
    }

    private void requestCourses(final boolean userRequest, final boolean includeProgress) {
        Result<List<Course>> result = new Result<List<Course>>() {
            @Override
            protected void onSuccess(List<Course> result, DataSource dataSource) {
                mCourses = result;

                if (!NetworkUtil.isOnline(getActivity()) && dataSource.equals(DataSource.LOCAL) && result.size() == 0) {
                    mNotificationController.setTitle(R.string.notification_no_network);
                    mNotificationController.setSummary(R.string.notification_no_network_with_offline_mode_summary);
                    mNotificationController.setNotificationVisible(true);
                    mRefreshLayout.setRefreshing(false);
                    mCourseListAdapter.clear();
                } else {
                    if (isMyCoursesFilter()) {
                        ArrayList<Course> removeList = new ArrayList<Course>();
                        for (Course course : mCourses) {
                            if (!course.is_enrolled) {
                                removeList.add(course);
                            }
                        }
                        mCourses.removeAll(removeList);
                    }

                    updateView();
                }
            }

            @Override
            protected void onWarning(WarnCode warnCode) {
                if (warnCode == WarnCode.NO_NETWORK && userRequest) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                }
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                mCourses = null;

                if (errorCode == ErrorCode.NO_RESULT) {
                    ToastUtil.show(getActivity(), getActivity().getString(R.string.toast_no_courses)
                            + " " + getActivity().getString(R.string.toast_no_network));
                } else if (errorCode == ErrorCode.NO_NETWORK && userRequest) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                }

                updateView();
            }
        };

        if (isMyCoursesFilter() && !UserModel.isLoggedIn(getActivity())) {
            mCourses = null;

            mNotificationController.setTitle(R.string.notification_please_login);
            mNotificationController.setSummary(R.string.notification_please_login_summary);
            mNotificationController.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_PROFILE);
                }
            });
            mNotificationController.setNotificationVisible(true);
            mRefreshLayout.setRefreshing(false);
        } else {
            if (mCourses == null || mCourses.size() == 0) {
                mNotificationController.setProgressVisible(true);
            } else {
                mRefreshLayout.setRefreshing(true);
            }
            mCourseModel.getCourses(result, includeProgress);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCourses != null) {
            outState.putParcelableArrayList(KEY_COURSES, (ArrayList<Course>) mCourses);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_courses, container, false);

        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        RefeshLayoutController.setup(mRefreshLayout, this);

        mCourseListAdapter = new CourseListAdapter(getActivity(), this);

        mAbsListView = (AbsListView) layout.findViewById(R.id.listView);
        mAbsListView.setAdapter(mCourseListAdapter);

        mNotificationController = new NotificationController(layout);
        mNotificationController.setInvisible();

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mFilter.equals(FILTER_ALL)) {
            mActivityCallback.onFragmentAttached(NavigationAdapter.NAV_ID_ALL_COURSES, getString(R.string.title_section_all_courses));
        } else if (mFilter.equals(FILTER_MY)) {
            mActivityCallback.onFragmentAttached(NavigationAdapter.NAV_ID_MY_COURSES, getString(R.string.title_section_my_courses));
        }

        if (mCourses != null && mCourses.size() > 0) {
            updateView();
        } else {
            requestCourses(false, false);
        }
    }

    private void updateView() {
        if (isAdded()) {
            mNotificationController.setInvisible();
            mRefreshLayout.setRefreshing(false);
            if (isMyCoursesFilter() && !UserModel.isLoggedIn(getActivity())) {
                mCourses = null;

                mNotificationController.setTitle(R.string.notification_please_login);
                mNotificationController.setSummary(R.string.notification_please_login_summary);
                mNotificationController.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_PROFILE);
                    }
                });
                mNotificationController.setNotificationVisible(true);
            } else if (isMyCoursesFilter() && (mCourses == null || mCourses.size() == 0)) {
                mNotificationController.setTitle(R.string.notification_no_enrollments);
                mNotificationController.setSummary(R.string.notification_no_enrollments_summary);
                mNotificationController.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_ALL_COURSES);
                    }
                });
                mNotificationController.setNotificationVisible(true);
            }

            if (mCourses != null) {
                mCourseListAdapter.updateCourses(mCourses);
            } else {
                mCourseListAdapter.clear();
            }
            mActivityCallback.updateDrawer();
        }
    }

    @Override
    public void onRefresh() {
        requestCourses(true, false);
    }

    @Override
    public void onEnrollButtonClicked(final Button button, Course course) {
        Result<Void> result = new Result<Void>() {
            @Override
            protected void onSuccess(Void result, DataSource dataSource) {
                requestCourses(false, false);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                button.setText(getActivity().getString(R.string.btn_enroll_me));
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                } else if (errorCode == ErrorCode.NO_AUTH) {
                    ToastUtil.show(getActivity(), R.string.toast_please_log_in);
                    mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_PROFILE);
                }
            }
        };
        button.setText("...");
        mCourseModel.addEnrollment(result, course);
    }

    private boolean isMyCoursesFilter() {
        return mFilter.equals(CourseListFragment.FILTER_MY);
    }

    private boolean isAllCoursesFilter() {
        return mFilter.equals(CourseListFragment.FILTER_ALL);
    }

    @Override
    public void onEnterButtonClicked(Course course) {
        if (UserModel.isLoggedIn(getActivity())) {
            Intent intent = new Intent(getActivity(), CourseActivity.class);
            Bundle b = new Bundle();
            b.putParcelable(CourseActivity.ARG_COURSE, course);
            intent.putExtras(b);
            startActivity(intent);
        } else {
            ToastUtil.show(getActivity(), R.string.toast_please_log_in);
            mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_PROFILE);
        }
    }

    @Override
    public void onDetailButtonClicked(Course course) {
        Intent intent = new Intent(getActivity(), CourseDetailsActivity.class);
        Bundle b = new Bundle();
        b.putParcelable(CourseActivity.ARG_COURSE, course);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mActivityCallback.isDrawerOpen())
            inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
