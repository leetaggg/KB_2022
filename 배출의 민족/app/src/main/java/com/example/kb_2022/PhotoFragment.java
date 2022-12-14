package com.example.kb_2022;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class PhotoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button Take_Photo;
    private Button Analyze_Photo;
    private ImageView capture_image;
    private TextView result;
    private CameraSurfaceView surfaceView;
    private Bitmap Rotate_Bitmap;
    private boolean Hide = false;
    private Context thiscontext;
    private String userID;

    public PhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OptionFragment newInstance(String param1, String param2) {
        OptionFragment fragment = new OptionFragment();
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
            userID = bundle.getString("?????????");
        }
        View Photo_View = inflater.inflate(R.layout.fragment_photo, container, false);
        Take_Photo = Photo_View.findViewById(R.id.Take);
        Analyze_Photo = Photo_View.findViewById(R.id.Analyze);
        capture_image = Photo_View.findViewById(R.id.capture_Image);
        surfaceView = Photo_View.findViewById(R.id.surface_Image);
        result = Photo_View.findViewById(R.id.Result);
        result.setText("????????? ????????? ???????????????");
        Analyze_Photo.setEnabled(false);
        thiscontext = container.getContext();

        Take_Photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Take_Photo.getText().equals("?????? ?????????")){
                    capture_image.setImageResource(0);
                    Take_Photo.setText("?????? ??????");
                    result.setText("????????? ????????? ???????????????");
                    Analyze_Photo.setEnabled(false);
                }
                else {
                    capture();
                }
            }
        });
        Analyze_Photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.setText("????????? ??????????????????");
                File upfile = BitToUri(Rotate_Bitmap, userID);
                System.out.println("?????? : "+upfile);
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), upfile);
                MultipartBody.Part body = MultipartBody.Part.createFormData("myfile", upfile.getName(), requestBody);
                postInterface postInterface = andClient.getClient().create(PhotoFragment.postInterface.class);
                Call<ServerResponse> call = postInterface.imgUpload(body);
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        result.setText("?????? ???????????? ??????????????????.");
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        result.setText("?????? ???????????? ??????????????????.");
                    }
                });
                analyze analyzeIF = andClient.getClient().create(PhotoFragment.analyze.class);
                Call<AnalyzeResponse> call1 = analyzeIF.getResult(userID);
                call1.enqueue(new Callback<AnalyzeResponse>() {
                    @Override
                    public void onResponse(Call<AnalyzeResponse> call, Response<AnalyzeResponse> response) {
                        result.setText(response.body().toString());
                    }

                    @Override
                    public void onFailure(Call<AnalyzeResponse> call, Throwable t) {
                    }
                });
            }
        });
        return Photo_View;
    }
    private void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }
    public void capture() {
        surfaceView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //bytearray ???????????? ??????
                //?????????????????? ??????????????? ??????????????? ????????? ??????
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4; // 1/8???????????? ????????????
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options); //data ????????? ?????? ?????? ????????? ???????????? ???????????? ??????
                Rotate_Bitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);//?????????????????????
                Analyze_Photo.setEnabled(true);
                result.setText("?????? ?????? ????????? ???????????????");
                Take_Photo.setText("?????? ?????????");
                capture_image.setImageBitmap(Rotate_Bitmap);//???????????? ?????????
                camera.startPreview();
            }
        });
    }
    private interface postInterface{
        @Multipart
        @POST("upload.php")
        Call<ServerResponse> imgUpload(@Part MultipartBody.Part image);

    }
    private interface analyze{
        @FormUrlEncoded
        @POST("hw.py")
        Call<AnalyzeResponse> getResult(@Field("userID") String userid);
    }
    private static class andClient{
        private static final String Server_URL = "http://123.215.162.92/KBServer/";
        private static Retrofit retrofit;

        private static Retrofit getClient(){
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            if(retrofit == null){
                retrofit = new Retrofit.Builder()
                        .baseUrl(Server_URL)
                        .addConverterFactory(new NullOnEmptyConverterFactory())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
            return retrofit;
        }
    }
    private static class NullOnEmptyConverterFactory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit)
        {
            final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return new Converter<ResponseBody, Object>() {
                @Override
                public Object convert(ResponseBody body) throws IOException
                {
                    if (body.contentLength() == 0) {
                        return null;
                    }
                    return delegate.convert(body);
                }
            };
        }
    }
    private class ServerResponse{
        @SerializedName("success")
        boolean success;
        @SerializedName("error")
        String error;

        public String toString() {
            return success + error;
        }
    }
    private class AnalyzeResponse{
        @SerializedName("percent")
        String percent;
        @SerializedName("result")
        String result;


        public String toString(){
            if (result == "fail"){
                return "?????? ????????? ????????? ??????????????????. ?????? ???????????????.";
            }
            else{
                return "?????? ???????????? " + result + "??? ????????? " + percent + "% ?????????.";
            }
        }
    }
    private File BitToUri(Bitmap bitmap, String user){
        File file = new File(thiscontext.getCacheDir(), user+".jpeg");
        FileOutputStream out = null;
        try {
             out = new FileOutputStream(file);
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return file;
    }


}