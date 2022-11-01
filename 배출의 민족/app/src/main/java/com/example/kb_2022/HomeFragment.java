package com.example.kb_2022;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private ListView Trash_List;
    private TextView Current_Result;
    private TextView Change_Weight;
    private TextView Tree_Result;
    private TextView User_Info;
    private ImageButton Refresh;
    private Context This_Activity;
    private String userID;
    private String userName;
    private String month;
    private String day;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private  JSONObject item;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CommunityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommunityFragment newInstance(String param1, String param2) {
        CommunityFragment fragment = new CommunityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if(bundle != null){
            bundle = getArguments();
            userID = bundle.getString("아이디");
            userName = bundle.getString("이름");
        }
        View Home_View = inflater.inflate(R.layout.fragment_home, container, false);
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        month = monthFormat.format(currentTime);//현재 달
        day = dayFormat.format(currentTime);//현재 달
        Trash_List = Home_View.findViewById(R.id.Main_ListView);
        This_Activity = container.getContext();
        Refresh = Home_View.findViewById(R.id.Refresh);
        Trash_List.setVerticalScrollBarEnabled(false);
        Current_Result = Home_View.findViewById(R.id.Currentweight); // 맨위 현재 무게
        Change_Weight = Home_View.findViewById(R.id.Changeweight); // 배출 변화량
        Tree_Result = Home_View.findViewById(R.id.TreeResult); //나무 결과
        month = month.replaceAll("^0+","");
        day = day.replaceAll("^0+","");
        User_Info = Home_View.findViewById(R.id.Welcome_Text);
        User_Info.setText(userName +"님 안녕하세요!");
        GetData task = new GetData();
        task.execute(userID, month);
        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
                Toast.makeText(container.getContext(),"갱신",Toast.LENGTH_SHORT).show();
                GetData taskR = new GetData();
                taskR.execute(userID, month);
            }
        });

        return Home_View;
    }
    private void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }
    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }


    private void dataSetting(String result) throws JSONException {
        ArrayList<String> chart_Data = new ArrayList<>();
        ArrayList<String> Week_Data = new ArrayList<>();
        ArrayList<BarEntry> Daily_chart = new ArrayList<>(); //일간데이터를 담는곳
        ArrayList<BarEntry> Weekly_chart = new ArrayList<>();//주간데이터를 담는곳
        ArrayList<BarEntry> Monthly_chart = new ArrayList<>();//월간데이터를 담는곳
        ArrayList<Integer> W_Data = new ArrayList<>();
        ArrayList[] Chart_List = {Daily_chart,Weekly_chart,Monthly_chart};
        String Month_Str;
        item = new JSONObject(result);
        int month1 = 0,month2 = 0,month3 = 0, month1_len = 0, month2_len = 0, month3_len = 0;
        int n_month = Integer.parseInt(month);
        JSONObject jsonmonth1 = item.getJSONObject(month);//이번달
        JSONObject jsonmonth2 = item.getJSONObject(Integer.toString(n_month - 1));//저번달
        JSONObject jsonmonth3 = item.getJSONObject(Integer.toString(n_month - 2));//저저번달
        for(int i = 0; i < jsonmonth3.length() - 1; i++){
            String TAG = "g" + "0" +  Integer.toString(n_month - 2);
            if(i + 1< 10){
                TAG = TAG + "0" + Integer.toString(i+1);
            }
            else{
                TAG = TAG + Integer.toString(i + 1);
            }
            Month_Str = jsonmonth3.getString(TAG);
            if(Month_Str == "null") {
                chart_Data.add("x");
            }
            else {
                month3 += Integer.parseInt(Month_Str);
                chart_Data.add(Month_Str);
                month3_len++; //달평균을 위해
            }
        }//저저번달
        for(int i = 0; i < jsonmonth2.length() - 1; i++){
            String TAG = "g" + "0" +  Integer.toString(n_month - 1);
            if(i + 1< 10){
                TAG = TAG + "0" + Integer.toString(i+1);
            }
            else{
                TAG = TAG + Integer.toString(i + 1);
            }
            Month_Str = jsonmonth2.getString(TAG);
            if(Month_Str == "null") {
                chart_Data.add("x");
            }
            else {
                month2 += Integer.parseInt(Month_Str);
                chart_Data.add(Month_Str);
                month2_len++; //달평균을 위해
            }
        }//저번달
        for(int i = 0; i < jsonmonth1.length() - 1; i++){
            String TAG = "g" + "0" + month;
            if(i + 1 < 10){
                TAG = TAG + "0" + Integer.toString(i+1);
            }
            else{
                TAG = TAG + Integer.toString(i + 1);
            }
            Month_Str = jsonmonth1.getString(TAG);
            if(Month_Str == "null") {
                chart_Data.add("x");
            }
            else {
                month1 += Integer.parseInt(Month_Str);
                chart_Data.add(Month_Str);
                month1_len++; //달평균을 위해
            }
        }//이번달
        int present_day = jsonmonth2.length() - 1 + jsonmonth3.length() - 1 +Integer.parseInt(day);//현재일 구함
        int Start_point = present_day - 28;
        int count = 0;
        int value = 0;
        int current_weight = 0;//오늘 무게
        int yester_weight = 0;//어제 무게
        if(chart_Data.get(present_day-1) == "x"){ //현재의 무게 구하기
            current_weight = 0;
        }
        else{
            current_weight = Integer.parseInt(chart_Data.get(present_day-1));
        }
        Current_Result.setText("오늘 배출량은 "+current_weight+"g 입니다!");
        if(chart_Data.get(present_day-2) == "x"){
            yester_weight = 0;
        }
        else{
            yester_weight = Integer.parseInt(chart_Data.get(present_day-2));

        }
        //배출량 변화
        int change_result = yester_weight - current_weight;
        double tree_change = change_result * 0.000008;
        BigDecimal bd = new BigDecimal(Math.abs(tree_change));
        DecimalFormat df = new DecimalFormat("#.######");
        if(change_result > 0){
            int unicode = 0x1F603;
            Change_Weight.setText("어제보다 "+change_result + "g 덜 배출했습니다."+getEmojiByUnicode(unicode));
            Tree_Result.setText(df.format(bd).toString()+"그루를 심었습니다!");
        }
        else if(change_result == 0){
            int unicode = 0x1F60A;
            Change_Weight.setText("어제와 동일하게 " +current_weight+"g 배출했습니다." + getEmojiByUnicode(unicode));
        }
        else{
            int unicode = 0x1F61F;
            Change_Weight.setText("어제보다 " + Math.abs(change_result) + "g 더 배출했습니다." + getEmojiByUnicode(unicode));
            Tree_Result.setText(df.format(bd).toString()+"그루가 없어졌습니다!");
        }

        for(int i = 0; i < 28; i++){
            if(i > 20){
                if(chart_Data.get(i + Start_point) == "x"){
                    Daily_chart.add(new BarEntry(Daily_chart.size() + 1, 0));
                }
                else {
                    Daily_chart.add(new BarEntry(Daily_chart.size() + 1, Integer.parseInt(chart_Data.get(i + Start_point))));
                }
            }//일간데이터 삽입
            Week_Data.add(chart_Data.get(i + Start_point));
            if(Week_Data.size() % 7 == 0){
                for(int j = 0; j < 7; j++){
                    if(Week_Data.get(j) != "x") {
                        value += Integer.parseInt(Week_Data.get(j));
                        count++;
                    }
                }
                Weekly_chart.add(new BarEntry(Weekly_chart.size()+1,value / count));
                Week_Data.clear();
                count = 0;
                value = 0;
            }
        }//주간데이터(1주차 ~ 4주차)
        Monthly_chart.add(new BarEntry(Monthly_chart.size() + 1,month3/month3_len ));//저저번달
        Monthly_chart.add(new BarEntry(Monthly_chart.size() + 1,month2/month2_len ));//저번달
        Monthly_chart.add(new BarEntry(Monthly_chart.size() + 1,month1/month1_len ));//이번달
        HomeList_Adapter List_item = new HomeList_Adapter();
        String[] array = new String[]{"일간", "주간", "월간"};
        for (int i=0; i<3; i++) {
            List_item.addItem(array[i],Chart_List[i]);//수치랑 이름 같이 넘겨주기
        }//일간 주간 월간 리스트뷰 아이템 생성
        Trash_List.setAdapter(List_item);
    }
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(This_Activity,
                    "잠시만 기다려주세요", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result == null){
                Log.d(TAG, "response - " + result);
                //실패시
            }
            else {
                //item = new JSONObject(result);
                try {
                    dataSetting(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = "http://123.215.162.92/KBServer/homelist.php";
            String postParameters = "userID=" + params[0] + "&mon=" + params[1];
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);
                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString().trim();
            } catch (Exception e) {
                errorString = e.toString();
                return null;
            }
        }
    }

}