package pl.grzegorziwanek.altimeter.app.recordingsession;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.grzegorziwanek.altimeter.app.map.MapActivity;
import pl.grzegorziwanek.altimeter.app.R;
import pl.grzegorziwanek.altimeter.app.data.GraphPoint;

import static com.google.common.base.Preconditions.checkNotNull;
import static pl.grzegorziwanek.altimeter.app.utils.NoticeDialogFragment.*;

/**
 * Created by Grzegorz Iwanek on 31.01.2017. That's it.
 */

public class RecordingSessionFragment extends Fragment implements RecordingSessionContract.View,
        NoticeDialogFragmentV4.NoticeDialogListener {

    @BindView(R.id.current_elevation_label) TextView mCurrElevationTextView;
    @BindView(R.id.current_latitude_value) TextView mCurrLatitudeTextView;
    @BindView(R.id.current_longitude_value) TextView mCurrLongitudeTextView;
    @BindView(R.id.max_height_numbers) TextView mMaxElevTextView;
    @BindView(R.id.min_height_numbers) TextView mMinElevTextView;
    @BindView(R.id.location_label) TextView mCurrAddressTextView;
    @BindView(R.id.distance_numbers) TextView mDistanceTextView;
    @BindView(R.id.reset_button) ImageButton mRefreshButton;
    @BindView(R.id.pause_button) ImageButton mPlayPauseButton;
    @BindView(R.id.lock_button) ImageButton mLockButton;
    @BindView(R.id.map_button) ImageButton mMapButton;
    @BindView(R.id.graph_view) GraphViewWidget mGraphViewWidget;
    @BindView(R.id.gps_button) ImageButton mGpsButton;
    @BindView(R.id.network_button) ImageButton mNetworkButton;
    @BindView(R.id.barometer_button) ImageButton mBarometerButton;
    @BindView(R.id.gps_value_label) TextView mGpsValueTextView;
    @BindView(R.id.network_value_label) TextView mNetworkValueTextView;
    @BindView(R.id.barometer_value_label) TextView mBarometerValueTextView;

    private RecordingSessionContract.Presenter mPresenter;
    private ShareActionProvider mShareActionProvider;

    public RecordingSessionFragment() {}

    public static RecordingSessionFragment newInstance() {
        return new RecordingSessionFragment();
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

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.activityDestroyedUnsubscribeRx();
    }

    @Override
    public void setPresenter(@NonNull RecordingSessionContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_share_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        show();
        return true;
    }

    private void show() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "SHARE"));
    }

    @OnClick(R.id.pause_button)
    public void onPlayPauseButtonClick() {
        int tag = getButtonTagAsInt(mPlayPauseButton);
        switch (tag) {
            case R.drawable.ic_play_arrow_black_24dp:
                mPresenter.callStartLocationRecording();
                break;
            case R.drawable.ic_pause_black_24dp:
                mPresenter.pauseLocationRecording();
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.reset_button)
    public void onResetButtonClick() {
        showUpDialog("Reset session. Are you sure?");
    }

    @OnClick(R.id.lock_button)
    public void onLockButtonCLick() {
        showUpDialog("Lock session. Recording will be terminated. Are you sure?");
    }

    @OnClick(R.id.map_button)
    public void onMapButtonClick() {
        showUpDialog("Generate map?");
    }

    @OnClick(R.id.gps_button)
    public void onGpsButtonClick() {
        int tag = getButtonTagAsInt(mGpsButton);
        switch (tag) {
            case R.drawable.ic_gps_lock_24dp:
                if (isRunning()) {
                    showStopSession();
                } else {
                    mPresenter.enableGps();
                }
                break;
            case R.drawable.ic_gps_open_24dp:
                if (isRunning()) {
                    showStopSession();
                } else {
                    mPresenter.disableGps();
                }
                break;
        }
    }

    @OnClick(R.id.network_button)
    public void onNetworkButtonClick() {
        int tag = getButtonTagAsInt(mNetworkButton);
        switch (tag) {
            case R.drawable.ic_network_lock_24dp:
                if (isRunning()) {
                    showStopSession();
                } else {
                    mPresenter.enableNetwork();
                }
                break;
            case R.drawable.ic_network_open_24dp:
                if (isRunning()) {
                    showStopSession();
                } else {
                    mPresenter.disableNetwork();
                }
                break;
        }
    }

    @OnClick(R.id.barometer_button)
    public void onBarometerButtonClick() {
        int tag = getButtonTagAsInt(mBarometerButton);
        switch (tag) {
            case R.drawable.ic_barometer_lock_24dp:
                if (isRunning()) {
                    showStopSession();
                } else {
                    mPresenter.enableBarometer();
                }
                break;
            case R.drawable.ic_barometer_open_24dp:
                if (isRunning()) {
                    showStopSession();
                } else {
                    mPresenter.disableBarometer();
                }
                break;
        }
    }

    private boolean isRunning() {
        return getButtonTagAsInt(mPlayPauseButton)
                == R.drawable.ic_pause_black_24dp;
    }

    private void showStopSession() {
        showMessage("You must stop session first.");
    }

    private void showUpDialog(String title) {
        Bundle args = new Bundle();
        args.putString("title", title);
        DialogFragment ndf = new NoticeDialogFragmentV4();
        ndf.setArguments(args);
        ndf.show(getChildFragmentManager(), "NoticeDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(String callbackCode) {
        switch (callbackCode) {
            case "Reset session. Are you sure?":
                mPresenter.resetSessionData();
                break;
            case "Lock session. Recording will be terminated. Are you sure?":
                mPresenter.lockSession();
                break;
            case "Generate map?":
                mPresenter.openSessionMap();
                break;
        }
    }

    private void initiateButtonsTags() {
        mPlayPauseButton.setTag(R.drawable.ic_play_arrow_black_24dp);
        mGpsButton.setTag(R.drawable.ic_gps_lock_24dp);
        mNetworkButton.setTag(R.drawable.ic_network_lock_24dp);
        mBarometerButton.setTag(R.drawable.ic_barometer_lock_24dp);
    }

    private int getButtonTagAsInt(ImageButton imageButton) {
        return Integer.parseInt(imageButton.getTag().toString());
    }

    @Override
    public void setButtonTagAndPicture(int pictureId) {
        switch (pictureId) {
            case R.drawable.ic_play_arrow_black_24dp:
            case R.drawable.ic_pause_black_24dp:
                mPlayPauseButton.setTag(pictureId);
                mPlayPauseButton.setBackgroundResource(pictureId);
                break;
            case R.drawable.ic_gps_lock_24dp:
            case R.drawable.ic_gps_open_24dp:
                mGpsButton.setTag(pictureId);
                mGpsButton.setBackgroundResource(pictureId);
                break;
            case R.drawable.ic_network_lock_24dp:
            case R.drawable.ic_network_open_24dp:
                mNetworkButton.setTag(pictureId);
                mNetworkButton.setBackgroundResource(pictureId);
                break;
            case R.drawable.ic_barometer_lock_24dp:
            case R.drawable.ic_barometer_open_24dp:
                mBarometerButton.setTag(pictureId);
                mBarometerButton.setBackgroundResource(pictureId);
                break;
        }
    }

    @Override
    public void checkDataSourceOpen() {
        if (isAnyDataSourceOpen()) {
            mPresenter.startLocationRecording();
        } else {
            showMessage("Turn on at least one data source");
        }
    }

    private boolean isAnyDataSourceOpen() {
        return getButtonTagAsInt(mGpsButton) == R.drawable.ic_gps_open_24dp
                || getButtonTagAsInt(mNetworkButton) == R.drawable.ic_network_open_24dp
                || getButtonTagAsInt(mBarometerButton) == R.drawable.ic_barometer_open_24dp;
    }

    @Override
    public void showSessionLocked() {
        showMessage("Session locked");
    }

    @Override
    public void showRecordingPaused() {
        showMessage("Paused");
    }

    @Override
    public void showRecordingData() {
        showMessage("Recording data");
    }

    @Override
    public void showSessionMap(@NonNull String sessionId) {
        Intent intent = new Intent(getContext(), MapActivity.class);
        intent.putExtra("sessionId", sessionId);
        startActivity(intent);
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
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
    public void setGpsTextView(String gpsAlt) {
        mGpsValueTextView.setText(gpsAlt);
    }

    @Override
    public void setNetworkTextView(String networkAlt) {
        mNetworkValueTextView.setText(networkAlt);
    }

    @Override
    public void setBarometerTextView(String barometerAlt) {
        mBarometerValueTextView.setText(barometerAlt);
    }

    @Override
    public void drawGraph(ArrayList<GraphPoint> graphPoints) {
        mGraphViewWidget.deliverGraph(graphPoints);
    }

    @Override
    public void resetGraph() {
        mGraphViewWidget.clearData();
    }
}

