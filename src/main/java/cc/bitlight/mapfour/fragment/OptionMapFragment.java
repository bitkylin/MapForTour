package cc.bitlight.mapfour.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import cc.bitlight.mapfour.BuildConfig;
import cc.bitlight.mapfour.R;
import cc.bitlight.mapfour.customclass.DeviceMessageApplication;

public class OptionMapFragment extends Fragment implements View.OnClickListener {
    DeviceMessageApplication application;

    LinearLayout layoutOptionFragmentSignIn;
    LinearLayout layoutOptionFragmentAfterLogin;
    LinearLayout layoutOptionFragmentAdminFunction;
    //登陆界面布局
    EditText editTextUserId;
    EditText editTextUserComments;
    RadioGroup rgSelectedGender;
    RadioButton male;
    RadioButton female;
    Button btnNormalLogin;
    //已登录界面布局
    ImageView imageViewOptionFragmentShowItem;
    TextView showUserId;
    TextView tvOptionFragmentShowIsAdmin;
    Button loginSucceed;
    //管理员权限布局
    Button btnUploadOnce;
    //内部类
    OptionFragmentOnclickListener mListener;
    View view;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OptionFragmentOnclickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement OptionFragmentOnclickListener");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        application = (DeviceMessageApplication) getActivity().getApplication();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("lml", "OptionMapFragment-creatview");
        view = inflater.inflate(R.layout.fragment_mapoption, container, false);
        initializeView();
        return view;
    }

    @Override
    public void onResume() {
        if (application.isLoginSucceed()) {
            layoutOptionFragmentSignIn.setVisibility(View.GONE);
            layoutOptionFragmentAfterLogin.setVisibility(View.VISIBLE);
            showUserId.setText(application.getEntityName());
            loginSucceed.setText("退出登录");
            tvOptionFragmentShowIsAdmin.setText(application.getUsersIdentity());
            tvOptionFragmentShowIsAdmin.setTextColor(getResources().getColor(R.color.red));
            imageViewOptionFragmentShowItem.setImageResource(application.getGender().equals("m") ? R.mipmap.map_portrait_man : R.mipmap.map_portrait_woman);
        }
        super.onResume();
    }


    void initializeView() {
        Log.d("lml", "NearbyPersonFragment_onCreateView");
        layoutOptionFragmentSignIn = (LinearLayout) view.findViewById(R.id.layoutOptionFragmentSignIn);
        layoutOptionFragmentAfterLogin = (LinearLayout) view.findViewById(R.id.layoutOptionFragmentAfterLogin);
        layoutOptionFragmentAdminFunction = (LinearLayout) view.findViewById(R.id.layoutOptionFragmentAdminFunction);
        //登陆界面布局
        editTextUserId = (EditText) view.findViewById(R.id.editTextOptionFragmentUserId);
        editTextUserComments = (EditText) view.findViewById(R.id.editTextOptionFragmentUserComments);
        rgSelectedGender = (RadioGroup) view.findViewById(R.id.rgSelectedGender);
        male = (RadioButton) view.findViewById(R.id.male);
        female = (RadioButton) view.findViewById(R.id.female);
        btnNormalLogin = (Button) view.findViewById(R.id.btnNormalLogin);
        //已登录界面布局
        imageViewOptionFragmentShowItem = (ImageView) view.findViewById(R.id.imageViewOptionFragmentShowItem);
        showUserId = (TextView) view.findViewById(R.id.textViewOptionFragmentShowUserId);
        tvOptionFragmentShowIsAdmin = (TextView) view.findViewById(R.id.tvOptionFragmentShowIsAdmin);
        loginSucceed = (Button) view.findViewById(R.id.btnOptionFragmentLoginSucceed);
        //管理员权限布局
        btnUploadOnce = (Button) view.findViewById(R.id.btnUploadOnce);

        //设置监听事件
        btnNormalLogin.setOnClickListener(this);
        btnUploadOnce.setOnClickListener(this);
        loginSucceed.setOnClickListener(this);
        rgSelectedGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                application.setGender(checkedId == R.id.male?"m":"w");
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //单击登录按钮，进行登录
            case R.id.btnNormalLogin:
                if (editTextUserId.getText().toString().equals("") | editTextUserComments.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "输入为空！", Toast.LENGTH_SHORT).show();
                    Log.d("lml", "OptionMapFragment:输入的信息为空");
                    break;
                }
                application.setEntityName(editTextUserId.getText().toString());
                application.setEntitySignature(editTextUserComments.getText().toString());
                Log.d("lml", "OptionMapFragment:设置的信息：用户名：" + application.getEntityName() + ";性别：" + application.getGender() + ";签名：" + application.getEntitySignature());

                layoutOptionFragmentSignIn.setVisibility(View.GONE);
                layoutOptionFragmentAfterLogin.setVisibility(View.VISIBLE);
                showUserId.setText(application.getEntityName());
                imageViewOptionFragmentShowItem.setImageResource(application.getGender().equals("m") ? R.mipmap.map_portrait_man : R.mipmap.map_portrait_woman);
                tvOptionFragmentShowIsAdmin.setText("正在获取用户信息");
                mListener.sendBroadcastToSearchNearbyInfo();
                break;

            //单击登陆成功按钮，可退出登录
            case R.id.btnOptionFragmentLoginSucceed:
                mListener.LoginSucceedToQuit();
                layoutOptionFragmentSignIn.setVisibility(View.VISIBLE);
                layoutOptionFragmentAfterLogin.setVisibility(View.GONE);
                loginSucceed.setText("正在登陆服务器");
                loginSucceed.setEnabled(false);
                break;
        }
    }

    //界面UI刷新(已登录成功)
    public void setLoginSucceed() {
        loginSucceed.setText("退出登录");
        tvOptionFragmentShowIsAdmin.setText(application.getUsersIdentity());
        tvOptionFragmentShowIsAdmin.setTextColor(getResources().getColor(R.color.red));
        loginSucceed.setEnabled(true);
    }

    //内部接口定义
    public interface OptionFragmentOnclickListener {
        //登录成功并查询周边
        void sendBroadcastToSearchNearbyInfo();
        //退出登录并停止Service中的服务
        void LoginSucceedToQuit();

    }
}
