package pl.grzegorziwanek.altimeter.app.newgraph;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.grzegorziwanek.altimeter.app.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Grzegorz Iwanek on 31.01.2017. That's it.
 */

public class AddNewGraphFragment extends Fragment implements AddNewGraphContract.View {
    @BindView(R.id.current_elevation_label) TextView mCurrElevationTextView;
    @BindView(R.id.current_latitude_value) TextView mCurrLatitudeTextView;
    @BindView(R.id.current_longitude_value) TextView mCurrLongitudeTextView;
    @BindView(R.id.max_height_numbers) TextView mMaxElevTextView;
    @BindView(R.id.min_height_numbers) TextView mMinElevTextView;
    @BindView(R.id.location_label) TextView mCurrAddressTextView;
    @BindView(R.id.distance_numbers) TextView mDistanceTextView;
    @BindView(R.id.reset_button) ImageButton mRefreshButton;
    @BindView(R.id.pause_button) ImageButton mPlayPauseButton;
    @BindView(R.id.graph_view) GraphViewWidget mGraphViewWidget;

    private AddNewGraphContract.Presenter mPresenter;

    public AddNewGraphFragment() {}

    public static AddNewGraphFragment newInstance() {
        return new AddNewGraphFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_graph, container, false);

        ButterKnife.bind(this, view);
        initiateButtonsTags();

        return view;
    }

    @Override
    public void setPresenter(@NonNull AddNewGraphContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @OnClick(R.id.pause_button)
    public void onPlayPauseButtonClick() {
        int tag = getButtonTagAsInt(mPlayPauseButton);
        switch (tag) {
            case R.drawable.ic_play_arrow_black_24dp:
                mPresenter.startLocationRecording();
                break;
            case R.drawable.ic_pause_black_24dp:
                mPresenter.stopLocationRecording();
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.reset_button)
    public void onResetButtonClick() {
        mPresenter.resetSessionData();
    }

    private void initiateButtonsTags() {
        mRefreshButton.setTag(R.drawable.ic_refresh_black_24dp);
        mPlayPauseButton.setTag(R.drawable.ic_play_arrow_black_24dp);
    }

    private int getButtonTagAsInt(ImageButton imageButton) {
        return Integer.parseInt(imageButton.getTag().toString());
    }

    @Override
    public void setButtonTag(int buttonTag) {
        mPlayPauseButton.setTag(buttonTag);
    }

    @Override
    public void setButtonPicture(int imageId) {
        mPlayPauseButton.setBackgroundResource(imageId);
    }

    @Override
    public void setAddressTextView(String address) {
        mCurrAddressTextView.setText(address);
    }

    @Override
    public void setElevationTextView(String elevation) {
        mCurrElevationTextView.setText(elevation);
    }

    @Override
    public void setMinHeightTextView(String minHeight) {
        mMinElevTextView.setText(minHeight);
    }

    @Override
    public void setDistanceTextView(String distance) {
        mDistanceTextView.setText(distance);
    }

    @Override
    public void setMaxHeightTextView(String maxHeight) {
        mMaxElevTextView.setText(maxHeight);
    }

    @Override
    public void setLatTextView(String latitude) {
        mCurrLatitudeTextView.setText(latitude);
    }

    @Override
    public void setLongTextView(String longitude) {
        mCurrLongitudeTextView.setText(longitude);
    }

    @Override
    public void drawGraph(ArrayList<Location> locations) {
        mGraphViewWidget.deliverGraph(locations);
    }

    @Override
    public void resetGraph() {
        mGraphViewWidget.clearData();
    }
}