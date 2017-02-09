package pl.grzegorziwanek.altimeter.app.altitudegraph;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import pl.grzegorziwanek.altimeter.app.model.Session;
import pl.grzegorziwanek.altimeter.app.model.database.source.SessionDataSource;
import pl.grzegorziwanek.altimeter.app.model.database.source.SessionRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link SessionFragment}), retrieves the data and updates the
 * UI as required.
 */
class SessionPresenter implements SessionContract.Presenter {

    private final SessionRepository mSessionRepository;
    private final SessionContract.View mSessionView;
    private boolean mFirstLoad = true;

    SessionPresenter(@NonNull SessionRepository sessionRepository, @NonNull SessionContract.View sessionView) {
        mSessionRepository = checkNotNull(sessionRepository, "sessionRepository cannot be null");
        mSessionView = checkNotNull(sessionView, "sessionView cannot be null");
        mSessionView.setPresenter(this);
    }

    @Override
    public void start() {
        loadSessions(false);
    }

    private void createTask(String title, String description) {
        Session newSession = new Session(title, description);
        mSessionRepository.createNewSession(newSession, null);
    }

    @Override
    public void loadSessions(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadSessions(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link SessionDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadSessions(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mSessionView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mSessionRepository.refreshSessions();
        }

        //TODO-> setting espresso here -> to analyse
        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        //EspressoIdlingResource.increment(); // App is busy until further notice

        //getting sessions from database repository
        mSessionRepository.getSessions(new SessionDataSource.LoadSessionsCallback() {
            @Override
            public void onSessionLoaded(List<Session> sessions) {
                List<Session> sessionsToShow = new ArrayList<Session>();
                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                //TODO-> esspresso, add
//               if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
//                    EspressoIdlingResource.decrement(); // Set app as idle.
//                }

                // The view may not be able to handle UI updates anymore
                if (!mSessionView.isActive()) {
                    return;
                }
                if (showLoadingUI) {
                    mSessionView.setLoadingIndicator(false);
                }

                processSessions(sessions);
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mSessionView.isActive()) {
                    return;
                }
                mSessionView.showLoadingSessionError();
            }
        });
    }

    private void processSessions(List<Session> sessions) {
        if (sessions.isEmpty()) {
            // Show a message indicating there are no tasks for that filter type.
            mSessionView.showEmptySessions(sessions);
        } else {
            // Show the list of tasks
            mSessionView.showSessions(sessions);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    //TODO-> mCurrentFiltering
    private void showFilterLabel() {
    }

    @Override
    public void addNewSession() {
        mSessionView.showAddSession();
    }

    @Override
    public void deleteCheckedSessions(ArrayList<String> sessionsId) {
        mSessionRepository.deleteSessions(sessionsId, false);
        mSessionView.showCheckedSessionsDeleted();
        loadSessions(false);
    }

    @Override
    public void deleteAllSessions(ArrayList<String> sessionsId) {
        mSessionRepository.deleteSessions(sessionsId, true);
        mSessionView.showAllSessionsDeleted();
        loadSessions(false);
    }
}
