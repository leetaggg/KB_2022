package com.example.kb_2022;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private TextView Select_Day;
    private TextView Show_Gram;
    private String[] Day;
    private String Month;
    private String userID;
    private ImageView Cal_Photo;
    private Context This_Activity;
    private JSONObject item;
    private MaterialCalendarView calendarView;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
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
        View Calender_View = inflater.inflate(R.layout.fragment_calendar, container, false);
        Bundle bundle = this.getArguments();
        if(bundle != null){
            bundle = getArguments();
            userID = bundle.getString("아이디");
        }
        This_Activity = container.getContext();
        calendarView = Calender_View.findViewById(R.id.Calendar);
        Cal_Photo = Calender_View.findViewById(R.id.Cal_Photo);
        Select_Day = Calender_View.findViewById(R.id.Day);
        Show_Gram = Calender_View.findViewById(R.id.Gram);
        Random random = new Random();
        int choice = random.nextInt(5);
        if(choice == 0){
            Cal_Photo.setImageResource(R.drawable.kiki);
        }
        else if(choice == 1){
            Cal_Photo.setImageResource(R.drawable.ramu);
        }
        else if(choice == 2){
            Cal_Photo.setImageResource(R.drawable.bibi);
        }
        else if(choice == 3){
            Cal_Photo.setImageResource(R.drawable.force);
        }
        else if(choice == 4){
            Cal_Photo.setImageResource(R.drawable.cole);
        }
        calendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)));
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));
        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader);
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                String Present = date.getDate().toString();//yyyy-mm-dd 형식
                Day = Present.split("-");
                String Data;
                try {
                    Data = item.getString("g"+Day[1]+Day[2]);
                    if (Data == "null"){
                        Data = "0";
                    }
                } catch (JSONException e) {
                    Data = "0";
                    e.printStackTrace();
                }
                Select_Day.setText(Day[0] +"년 " + Day[1] + "월 " + Day[2] + "일");
                Show_Gram.setText("선택하신 날짜에 버린 쓰레기의 총합은 "+ Data +"g 입니다.");
            }
        });
        calendarView.addDecorators(new DayDecorator(container.getContext()));
        // 좌우 화살표 가운데의 연/월이 보이는 방식 커스텀
        calendarView.setTitleFormatter(new TitleFormatter() {
            @Override
             public CharSequence format(CalendarDay day){
                // CalendarDay라는 클래스는 LocalDate 클래스를 기반으로 만들어진 클래스다
                // 때문에 MaterialCalendarView에서 연/월 보여주기를 커스텀하려면 CalendarDay 객체의 getDate()로 연/월을 구한 다음 LocalDate 객체에 넣어서
                // LocalDate로 변환하는 처리가 필요하다
                LocalDate inputText = day.getDate();
                String[] calendarHeaderElements = inputText.toString().split("-");
                StringBuilder calendarHeaderBuilder = new StringBuilder();
                calendarHeaderBuilder.append(calendarHeaderElements[0])
                        .append("년")
                        .append(" ")
                        .append(calendarHeaderElements[1].replaceAll("^0+",""))//달
                        .append("월");
                Month = calendarHeaderElements[1].replaceAll("^0+","");
                GetData task = new GetData();
                task.execute(userID, Month);

                return calendarHeaderBuilder.toString();
            }
        });
        return Calender_View;
    }

    /* 선택된 요일의 background를 설정하는 Decorator 클래스 */
        private static class DayDecorator implements DayViewDecorator {

            private final Drawable drawable;

            public DayDecorator(Context context) {
                drawable = ContextCompat.getDrawable(context, R.drawable.calendar_selector);
            }

            // true를 리턴 시 모든 요일에 내가 설정한 드로어블이 적용된다
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return true;
            }

            // 일자 선택 시 내가 정의한 드로어블이 적용되도록 한다
            @Override
            public void decorate(DayViewFacade view) {
                view.setSelectionDrawable(drawable);
    //            view.addSpan(new StyleSpan(Typeface.BOLD));   // 달력 안의 모든 숫자들이 볼드 처리됨
            }
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
                try {
                    item = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = "http://123.215.162.92/KBServer/readgarbage.php";
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