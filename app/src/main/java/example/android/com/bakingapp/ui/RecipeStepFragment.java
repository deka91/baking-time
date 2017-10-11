package example.android.com.bakingapp.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import example.android.com.bakingapp.R;
import example.android.com.bakingapp.model.Recipe;
import example.android.com.bakingapp.model.Step;

import static example.android.com.bakingapp.util.MyConstants.SELECTED_RECIPE;
import static example.android.com.bakingapp.util.MyConstants.SELECTED_STEP;
import static example.android.com.bakingapp.util.MyConstants.STACK_RECIPE_STEP_DETAIL;

/**
 * Created by Deniz Kalem on 06.10.17.
 */

public class RecipeStepFragment extends Fragment
{
  private SimpleExoPlayerView simpleExoPlayerView;
  private SimpleExoPlayer simpleExoPlayer;
  private Step step;
  private ArrayList<Step> steps;
  private int position;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.recipe_step_detail_fragment_body_part, container, false);
    TextView textView = (TextView) rootView.findViewById(R.id.recipe_step_detail_text);

    if(savedInstanceState != null)
    {
      step = savedInstanceState.getParcelable(SELECTED_STEP);
      steps = savedInstanceState.getParcelableArrayList("steps");
    } else
    {
      step = getArguments().getParcelable(SELECTED_STEP);
      steps = getArguments().getParcelableArrayList("steps");
    }

    if(step == null)
    {
      Recipe recipe = getArguments().getParcelable(SELECTED_RECIPE);
//      Recipe recipe = savedInstanceState.getParcelable(SELECTED_RECIPE);
      steps = (ArrayList<Step>) recipe.getSteps();
      step = steps.get(0);
    }

    textView.setText(step.getDescription());
    textView.setVisibility(View.VISIBLE);

    simpleExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.playerView);
    simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

    String videoURL = step.getVideoURL();

    String imageUrl = step.getThumbnailURL();
    if(!imageUrl.isEmpty())
    {
      Uri builtUri = Uri.parse(imageUrl).buildUpon().build();
      ImageView thumbImage = (ImageView) rootView.findViewById(R.id.thumbImage);
      thumbImage.setVisibility(View.VISIBLE);
      Picasso.with(getContext()).load(builtUri).into(thumbImage);
    }

    if(!videoURL.isEmpty())
    {

      initializePlayer(Uri.parse(step.getVideoURL()));

      if(rootView.findViewWithTag("sw600dp-land-recipe_step_detail") != null)
      {
        getActivity().findViewById(R.id.fragment_container2).setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
      } else if(isInLandscapeMode(getContext()))
      {
        textView.setVisibility(View.GONE);
      }
    } else
    {
      simpleExoPlayer = null;
      simpleExoPlayerView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
    }

    ImageButton back = (ImageButton) rootView.findViewById(R.id.ib_back);
    ImageButton next = (ImageButton) rootView.findViewById(R.id.ib_next);

    back.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        position = step.getId() - 1;
        if(position >= 0 && steps.get(position).getId() >= 0)
        {
          if(simpleExoPlayer != null)
          {
            simpleExoPlayer.stop();
          }

          click(position);
        }
      }
    });

    next.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        position = step.getId() + 1;
        if(position < steps.size())
        {
          if(simpleExoPlayer != null)
          {
            simpleExoPlayer.stop();
          }
          click(position);
        }
      }
    });

    return rootView;
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    releasePlayer();
  }

  @Override
  public void onStop()
  {
    super.onStop();
    releasePlayer();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    releasePlayer();
  }


  private void initializePlayer(Uri mediaUri)
  {
    if(simpleExoPlayer == null)
    {
      // Create an instance of the ExoPlayer.
      TrackSelector trackSelector = new DefaultTrackSelector();
      LoadControl loadControl = new DefaultLoadControl();
      simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);
      simpleExoPlayerView.setPlayer(simpleExoPlayer);
      // Prepare the MediaSource.
      String userAgent = Util.getUserAgent(getContext(), "BakingApp");
      MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
        getActivity(), userAgent), new DefaultExtractorsFactory(), null, null);
      simpleExoPlayer.prepare(mediaSource);
      simpleExoPlayer.setPlayWhenReady(true);
      simpleExoPlayerView.setVisibility(View.VISIBLE);
    }
  }

  public boolean isInLandscapeMode(Context context)
  {
    return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
  }

  @Override
  public void onSaveInstanceState(Bundle currentState)
  {
    super.onSaveInstanceState(currentState);
    currentState.putParcelable(SELECTED_STEP, step);
  }

  private void releasePlayer()
  {
    if(simpleExoPlayer != null)
    {
      simpleExoPlayer.stop();
      simpleExoPlayer.release();
      simpleExoPlayer = null;
    }
  }

  public void click(Integer i)
  {
    RecipeStepFragment fragment = new RecipeStepFragment();
    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

    Bundle bundle = new Bundle();
    bundle.putParcelable(SELECTED_STEP, steps.get(i));
    bundle.putParcelableArrayList("steps", steps);
    fragment.setArguments(bundle);

    if(RecipeFragment.isTablet && getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
    {
      fragmentManager.beginTransaction()
        .replace(R.id.fragment_container2, fragment).addToBackStack(STACK_RECIPE_STEP_DETAIL)
        .commit();
    } else
    {
      fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment).addToBackStack(STACK_RECIPE_STEP_DETAIL)
        .commit();
    }
  }


}