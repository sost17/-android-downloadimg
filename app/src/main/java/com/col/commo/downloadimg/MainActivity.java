package com.col.commo.downloadimg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String IMG_PATH = "http://123.207.98.107/img/im1.png";
    private Button btn_getPic;
    private Button btn_abort;
    private ImageView img;
    private ProgressBar progressBar;
    private TextView tv_progress;
    private TextView tvCount;
    private Handler handler;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_getPic = (Button)findViewById(R.id.btn_load_pic);
        btn_abort = (Button)findViewById(R.id.btn_abort);
        img = (ImageView)findViewById(R.id.imageView1);
        progressBar = (ProgressBar)findViewById(R.id.progressBar1);
        tv_progress = (TextView)findViewById(R.id.tv_progress);
        tvCount = (TextView) findViewById(R.id.textView);

        final ImageLoader loader = new ImageLoader();

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x100:
                        tvCount.setText("当前count的值为：" + msg.arg1);
                        break;

                    default:
                        break;
                }
            }
        };

        btn_getPic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                loader.execute(IMG_PATH);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 1; i <= 10; i++) {
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            Message msg = Message.obtain();
//                            msg.what = 0x100;
//                            msg.arg1 = i * 10;
//                            handler.sendMessage(msg);

                        }
                        handler.sendEmptyMessage(0x101);
                    }
                }).start();

            }
        });
        btn_abort.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                loader.cancel(true);
            }
        });
        btn_abort.setEnabled(false);

    }

    private class ImageLoader extends AsyncTask<String, Integer, Bitmap>{


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            btn_getPic.setEnabled(false);
            btn_abort.setEnabled(true);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            img.setImageResource(R.drawable.jiazai);
        }


        @Override
        protected Bitmap doInBackground(String... url) {
            // TODO Auto-generated method stub
            for(int i =0 ; i <= 100; i ++){
                count += i;
//                publishProgress(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isCancelled()) {
                    return null;
                }
            }

            try {
                URL u;
                HttpURLConnection conn = null;
                InputStream in = null;
                OutputStream out = null;
                final String filename = "local_temp_image";

                try {
                    u = new URL(url[0]);
                    conn = (HttpURLConnection) u.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(false);
                    conn.setConnectTimeout(10*1000);

                    in = conn.getInputStream();
                    out = openFileOutput(filename, MODE_PRIVATE);
                    byte[] buff = new byte[8192];
                    int seg = 0;
                    final long total = conn.getContentLength();
                    long current = 0;


                    while (! isCancelled() && (seg = in.read(buff))!= -1) {
                        out.write(buff, 0, seg);

                        current += seg;
                        int progress = (int) ((float)current/(float)total * 100f);

                        publishProgress(progress);

                        SystemClock.sleep(1000);
                    }
                }finally{
                    if (conn != null) {

                        conn.disconnect();
                    }
                    if (in != null) {

                        in.close();
                    }
                    if (out != null) {

                        out.close();
                    }
                }
                return BitmapFactory.decodeFile(getFileStreamPath(filename).getAbsolutePath());

            }catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub

            super.onProgressUpdate(progress);
            progressBar.setProgress(progress[0]);
            tv_progress.setText("正在下载"+progress[0]+"%");
            tvCount.setText("当前count的值为：" + count);
        }

        @Override
        protected void onPostExecute(Bitmap image) {

            // TODO Auto-generated method stub
            super.onPostExecute(image);
            if (image != null) {
                img.setImageBitmap(image);
            }
            progressBar.setProgress(100);
            progressBar.setVisibility(View.GONE);
            tv_progress.setText("下载成功");
            btn_getPic.setEnabled(true);
            btn_abort.setEnabled(false);
        }

    }
}
