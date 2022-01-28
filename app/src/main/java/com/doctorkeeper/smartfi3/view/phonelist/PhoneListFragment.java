/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctorkeeper.smartfi3.view.phonelist;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.doctorkeeper.smartfi3.R;
import com.doctorkeeper.smartfi3.view.BaseFragment;
import com.doctorkeeper.smartfi3.view.log_in.LoginDialogFragment;
import com.doctorkeeper.smartfi3.view.options.OptionsDialogFragment;
import com.doctorkeeper.smartfi3.view.phone_camera.PhoneCameraFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PhoneListFragment extends BaseFragment {

    @BindView(R.id.cloud_description)
    TextView cloudTextView;

    @BindView(R.id.btn_back)
    Button backBtn;

    @BindView(R.id.btn_logout)
    Button logoutBtn;

    @BindView(R.id.btn_options)
    Button optionsBtn;

//    @BindView(R.id.btn_options)
//    Button optionsBtn;

    private Fragment phoneListGalleyFragment;

    private final String TAG = com.doctorkeeper.smartfi3.view.phonelist.PhoneListFragment.class.getSimpleName();

    public static com.doctorkeeper.smartfi3.view.phonelist.PhoneListFragment newInstance() {
        com.doctorkeeper.smartfi3.view.phonelist.PhoneListFragment f = new com.doctorkeeper.smartfi3.view.phonelist.PhoneListFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.phone_list_frag, container, false);
        ButterKnife.bind(this, view);
//        Log.i(TAG, "CloudFragment START");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        phoneListGalleyFragment = com.doctorkeeper.smartfi3.view.phonelist.PhoneListGalleryFragment.newInstance();
        ft.replace(R.id.cloud_detail_container, phoneListGalleyFragment, null);
        ft.addToBackStack(null);
        ft.commit();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @OnClick(R.id.btn_back)
    public  void backBtnClicked(){
//        getFragmentManager().popBackStackImmediate();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.btn_logout)
    public void logoutBtnClicked(){
//        SmartFiPreference.setTnhId(BlabAPI.getActivity(), "");
//        SmartFiPreference.setTnhPwd(getActivity(),"");
//        SmartFiPreference.setHospitalId(getActivity(),"");
//        SmartFiPreference.setSfToken(getActivity(), "");
//        SmartFiPreference.setSfPatientName(getActivity(),"");

        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
        changelogTx.add(loginDialogFragment, "Login");
        changelogTx.commit();
    }

    @OnClick(R.id.btn_options)
    public void optionsBtnClicked(){

//        MadamfiveAPI.deletePhotoModelList();
        Log.i(TAG, "btn_options clicked");
//        Toast toast = Toast.makeText(getActivity(), "옵션버튼 클릭", Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        OptionsDialogFragment opt = OptionsDialogFragment.newInstance();
        changelogTx.add(opt, "Options");
        changelogTx.commit();
    }

}
