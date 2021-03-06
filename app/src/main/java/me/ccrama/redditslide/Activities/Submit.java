package me.ccrama.redditslide.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Submit extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
    private View image;
    private View self;

    private View link;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        setContentView(R.layout.activity_submit);

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setTitle("Submit post");
        b.setBackgroundColor(Pallete.getColor("alksfjalskjf"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getColor("asldkfj")));
        }
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        self = findViewById(R.id.selftext);
        image = findViewById(R.id.image);
        link = findViewById(R.id.url);

        image.setVisibility(View.GONE);
        link.setVisibility(View.GONE);


        findViewById(R.id.selftextradio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                link.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.imageradio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                link.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.linkradio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                link.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.selImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), 1);
            }
        });

        DoEditorActions.doActions(((EditText) findViewById(R.id.bodytext)), findViewById(R.id.innersend2), getSupportFragmentManager());

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FloatingActionButton)findViewById(R.id.send)).hide();
                new AsyncDo().execute();
               }
        });

    }
    private String URL;
    private void setImage(final String URL){
        this.URL = URL;


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.imagepost).setVisibility(View.VISIBLE);
                ((Reddit)getApplication()).getImageLoader().displayImage(URL, ((ImageView) findViewById(R.id.imagepost)) );

            }
        });
    }

    private class AsyncDo extends AsyncTask<Void, Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (self.getVisibility() == View.VISIBLE) {

                    try {
                        String s = new AccountManager(Authentication.reddit).submit(new AccountManager.SubmissionBuilder(((EditText) findViewById(R.id.bodytext)).getText().toString(), ((EditText) findViewById(R.id.subreddittext)).getText().toString(), ((EditText) findViewById(R.id.titletext)).getText().toString())).getFullName();
                        new OpenRedditLink(Submit.this, "reddit.com/r/" +((EditText) findViewById(R.id.subreddittext)).getText().toString() + "/comments/" + s.substring(3, s.length()));

                        Submit.this.finish();

                    } catch (final ApiException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialogWrapper.Builder(Submit.this).setTitle("Uh oh, an error occured!").setMessage("Error: " + e.getExplanation() + "\nWould you like to try again?").setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ((FloatingActionButton)findViewById(R.id.send)).show();

                                    }
                                }).create().show();
                            }
                        });

                    }
                } else if (link.getVisibility() == View.VISIBLE) {

                    try {
                        String s = new AccountManager(Authentication.reddit).submit(new AccountManager.SubmissionBuilder(new URL(((EditText) findViewById(R.id.urltext)).getText().toString()), ((EditText) findViewById(R.id.subreddittext)).getText().toString(), ((EditText) findViewById(R.id.titletext)).getText().toString())).getFullName();
                        new OpenRedditLink(Submit.this, "reddit.com/r/" +((EditText) findViewById(R.id.subreddittext)).getText().toString() + "/comments/" + s.substring(3, s.length()));

                        Submit.this.finish();

                    } catch (final ApiException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(e instanceof ApiException) {
                                    new AlertDialogWrapper.Builder(Submit.this).setTitle("Uh oh, an error occured!").setMessage("Error: " + e.getExplanation() + "\nWould you like to try again?").setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();

                                        }
                                    }).create().show();
                                } else {
                                    new AlertDialogWrapper.Builder(Submit.this).setTitle("Uh oh, an error occured!").setMessage("Error: Invalid URL\nWould you like to try again?").setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();

                                        }
                                    }).create().show();
                                }
                            }
                        });

                    }
                } else if (image.getVisibility() == View.VISIBLE) {
                    try {
                        String s = new AccountManager(Authentication.reddit).submit(new AccountManager.SubmissionBuilder(new URL(URL), ((EditText) findViewById(R.id.subreddittext)).getText().toString(), ((EditText) findViewById(R.id.titletext)).getText().toString())).getFullName();

                        new OpenRedditLink(Submit.this, "reddit.com/r/" +((EditText) findViewById(R.id.subreddittext)).getText().toString() + "/comments/" + s.substring(3, s.length()));

                        Submit.this.finish();


                    } catch (final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(e instanceof ApiException) {
                                    new AlertDialogWrapper.Builder(Submit.this).setTitle("Uh oh, an error occured!").setMessage("Error: " + ((ApiException) e).getExplanation() + "\nWould you like to try again?").setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();

                                        }
                                    }).create().show();
                                } else {
                                    new AlertDialogWrapper.Builder(Submit.this).setTitle("Uh oh, an error occured!").setMessage("Error: Invalid URL\nWould you like to try again?").setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();

                                        }
                                    }).create().show();
                                }
                            }
                        });

                    }
                }
            } catch(Exception e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(Submit.this).setTitle("Uh oh, an error occured!").setMessage("An error occured. Would you like to try again").setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((FloatingActionButton)findViewById(R.id.send)).show();

                            }
                        }).create().show();

                    }
                });
            }
                return null;

        }
    }
    private class UploadImgur extends AsyncTask<Bitmap, Void, JSONObject> {


        private final ProgressDialog dialog = new ProgressDialog(Submit.this);
        public Bitmap b;

        @Override
        protected void onPostExecute(final JSONObject result) {
            dialog.dismiss();
            try {
                String url = result.getJSONObject("data").getString("link");
                setImage(url);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Uploading image to Imgur...");
            dialog.setCancelable(false);
            dialog.show();
        }


        @Override
        protected JSONObject doInBackground(Bitmap... sub) {

            Bitmap bitmap = sub[0];
            b = bitmap;

// Creates Byte Array from picture
            URL url;
            try {

                url = new URL("https://api.imgur.com/3/image");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                String data = URLEncoder.encode("image", "UTF-8") + "="
                        + URLEncoder.encode(getImageLink(bitmap), "UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Client-ID " + "bef87913eb202e9");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                conn.connect();
                StringBuilder stb = new StringBuilder();
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the response
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    stb.append(line).append("\n");
                }
                wr.close();
                rd.close();
                return new JSONObject(stb.toString());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

    }
    private String getImageLink(Bitmap b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, baos); // Not sure whether this should be jpeg or png, try both and see which works best

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    new UploadImgur().execute(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
  }