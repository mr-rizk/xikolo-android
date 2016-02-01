package de.xikolo.controller.main;

import android.content.Context;

import de.xikolo.controller.BaseFragment;

public abstract class ContentFragment extends BaseFragment {

    protected OnFragmentInteractionListener mActivityCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mActivityCallback = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityCallback = null;
    }

    public interface OnFragmentInteractionListener {

        void onFragmentAttached(int id, String title);

        boolean isDrawerOpen();

        void updateDrawer();

        void selectDrawerSection(int pos);

    }

}
