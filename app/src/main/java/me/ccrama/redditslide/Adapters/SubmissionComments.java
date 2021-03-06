package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.CommentPage;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubmissionComments {
    public ArrayList<CommentNode> comments;
    private CommentNode baseComment;
    public final SwipeRefreshLayout refreshLayout;

    private String context;

    private final String fullName;
    public Submission submission;
    private final CommentPage page;

    private CommentSort defaultSorting = CommentSort.CONFIDENCE;
    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout) {
        this.fullName = fullName;
        this.page = commentPage;

        this.refreshLayout = layout;
        new LoadData(true).execute(fullName);
    }
    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout, String context) {
        this.fullName = fullName;
        this.page = commentPage;
        this.context = context;
        this.refreshLayout = layout;
        new LoadData(true).execute(fullName);
    }
    public void setSorting(CommentSort sort){
        defaultSorting = sort;
        new LoadData(false).execute(fullName);

    }
    private CommentAdapter adapter;


    public void loadMore(CommentAdapter adapter, String subreddit) {
        this.adapter = adapter;
        new LoadData(true).execute(subreddit);

    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {


            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
                    page.doData(reset);

                    refreshLayout.setRefreshing(false);
        }

        @Override
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            SubmissionRequest.Builder builder;
            if(context == null) {
                builder = new SubmissionRequest.Builder(fullName).sort(defaultSorting);
            } else {
                builder = new SubmissionRequest.Builder(fullName).sort(defaultSorting).focus(context).context(3);
            }
            try {
                submission = Authentication.reddit.getSubmission(builder.build());
                baseComment = submission.getComments();
              //  baseComment.loadFully(Authentication.reddit, 6, 30);
                comments = new ArrayList<>();
                for (CommentNode n : baseComment.walkTree()) {

                    comments.add(n);



                }
            } catch (NetworkException e ){
                //Todo reauthenticate
            }
            return null;
        }
    }


}
