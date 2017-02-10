package pl.grzegorziwanek.altimeter.app.model.database.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import pl.grzegorziwanek.altimeter.app.model.Session;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Grzegorz Iwanek on 27.01.2017.
 */

public class SessionDataSource implements pl.grzegorziwanek.altimeter.app.model.database.source.SessionDataSource {

    private static SessionDataSource INSTANCE = null;
    private SessionDbHelper mSessionDbHelper;

    //Private to prevent direct instantiation.
    private SessionDataSource(@NonNull Context context) {
        checkNotNull(context);
        mSessionDbHelper = new SessionDbHelper(context);
    }

    public static SessionDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionDataSource(context);
        }
        return INSTANCE;
    }

    @Override
    public void createNewSession(@NonNull Session session, @NonNull SaveSessionCallback callback) {
        checkNotNull(session);
        SQLiteDatabase db = mSessionDbHelper.getWritableDatabase();

        String insertOrIgnore = mSessionDbHelper.queryInsertOrIgnore(session.getId());
        db.execSQL(insertOrIgnore);
        db.close();

        callback.onNewSessionSaved(session.getId());
    }

    @Override
    public void createRecordsTable(@NonNull Session session) {
        checkNotNull(session);
        SQLiteDatabase db = mSessionDbHelper.getWritableDatabase();
        int oldVersion = db.getVersion();
        int newVersion = oldVersion + 1;
        SessionDbHelper.setOnUpgrade(session.getId());
        mSessionDbHelper.onUpgrade(db, oldVersion, newVersion);
        db.close();
    }

    @Override
    public void updateSessionData(@NonNull Session session) {
        checkNotNull(session);
        SQLiteDatabase db = mSessionDbHelper.getWritableDatabase();

        ContentValues valuesSession = getSessionValues(session);
        ContentValues valuesRecord = getRecordValues(session);

        String rowSelection = SessionDbContract.SessionEntry.COLUMN_NAME_ENTRY_ID + "=" + "\"" + session.getId() + "\"";
        updateRowsDb(db, SessionDbContract.SessionEntry.TABLE_NAME, valuesSession, rowSelection);

        String tableNameRecords = "\"" + session.getId() +"\"";
        insertToDb(db, tableNameRecords, valuesRecord);
        db.close();
    }

    private ContentValues getRecordValues(Session session) {
        ContentValues vRecord = new ContentValues();
        vRecord.put(SessionDbContract.RecordsEntry.COLUMN_NAME_LATITUDE, session.getLatitude());
        vRecord.put(SessionDbContract.RecordsEntry.COLUMN_NAME_LONGITUDE, session.getLongitude());
        vRecord.put(SessionDbContract.RecordsEntry.COLUMN_NAME_ALTITUDE, session.getCurrentElevation());
        vRecord.put(SessionDbContract.RecordsEntry.COLUMN_NAME_DATE, session.getCurrentLocation().getTime());
        vRecord.put(SessionDbContract.RecordsEntry.COLUMN_NAME_ADDRESS, session.getAddress());
        vRecord.put(SessionDbContract.RecordsEntry.COLUMN_NAME_DISTANCE, session.getDistance());
        return vRecord;
    }

    private ContentValues getSessionValues(Session session) {
        ContentValues vSession = new ContentValues();
        vSession.put(SessionDbContract.SessionEntry.COLUMN_NAME_CURRENT_ALTITUDE, session.getCurrentElevation());
        vSession.put(SessionDbContract.SessionEntry.COLUMN_NAME_MAX_HEIGHT, session.getMaxHeight());
        vSession.put(SessionDbContract.SessionEntry.COLUMN_NAME_MIN_HEIGHT, session.getMinHeight());
        vSession.put(SessionDbContract.SessionEntry.COLUMN_NAME_CURRENT_ADDRESS, session.getAddress());
        vSession.put(SessionDbContract.SessionEntry.COLUMN_NAME_CURRENT_DISTANCE, session.getDistance());
        return vSession;
    }

    private void insertToDb(SQLiteDatabase db, String tableName, ContentValues values) {
        db.insert(tableName, null, values);
    }

    private void updateRowsDb(SQLiteDatabase db, String tableName, ContentValues values, String where) {
        db.update(tableName, values, where, null);
    }

    @Override
    public void getSessions(@NonNull LoadSessionsCallback callback) {
        List<Session> sessions = new ArrayList<>();
        SQLiteDatabase db = mSessionDbHelper.getReadableDatabase();

        String[] projection = {
                SessionDbContract.SessionEntry.COLUMN_NAME_ENTRY_ID,
                SessionDbContract.SessionEntry.COLUMN_NAME_CURRENT_ALTITUDE,
                SessionDbContract.SessionEntry.COLUMN_NAME_MAX_HEIGHT,
                SessionDbContract.SessionEntry.COLUMN_NAME_MIN_HEIGHT,
                SessionDbContract.SessionEntry.COLUMN_NAME_CURRENT_ADDRESS,
                SessionDbContract.SessionEntry.COLUMN_NAME_CURRENT_DISTANCE
        };

        Cursor c = db.query(
                SessionDbContract.SessionEntry.TABLE_NAME, projection, null, null, null, null, null
        );

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String itemId = c.getString(c.getColumnIndexOrThrow(SessionDbContract.SessionEntry.COLUMN_NAME_ENTRY_ID));
//                String itemLatitude = c.getString(c.getColumnIndexOrThrow(SessionDbContract.RecordsEntry.COLUMN_NAME_LATITUDE));
//                String itemLongitue = c.getString(c.getColumnIndexOrThrow(SessionDbContract.RecordsEntry.COLUMN_NAME_LONGITUDE));
//                String itemAltitude = c.getString(c.getColumnIndexOrThrow(SessionDbContract.SessionEntry.COLUMN_NAME_ALTITUDE));
//                String itemDate = c.getString(c.getColumnIndexOrThrow(SessionDbContract.SessionEntry.COLUMN_NAME_DATE));
//                String itemRadius = c.getString(c.getColumnIndexOrThrow(SessionDbContract.SessionEntry.COLUMN_NAME_RADIUS));
                Session session = new Session("DADA", "WRWA", itemId);
                sessions.add(session);
            }
        }
        if (c != null) {
            c.close();
        }

        db.close();

        callback.onSessionLoaded(sessions);
    }

    @Override
    public void clearSessionData(@NonNull String sessionId) {

    }

    @Override
    public void deleteSessions(ArrayList<String> sessionsId, boolean isDeleteAll) {
        SQLiteDatabase db = mSessionDbHelper.getWritableDatabase();
        for (String sessionId : sessionsId) {
            db.execSQL(mSessionDbHelper.queryDeleteTables(sessionId));
            db.execSQL(mSessionDbHelper.queryDeleteRows(sessionId));
        }
        db.close();
    }

    @Override
    public void refreshSessions() {
        // Not required because the {@link SessionRepository} handles the logic of refreshing the
        // tasks from all the available data sources. This instance is used as a member of
        // {@link SessionRepository}
    }
}