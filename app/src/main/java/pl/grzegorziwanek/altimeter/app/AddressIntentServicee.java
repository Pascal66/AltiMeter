package pl.grzegorziwanek.altimeter.app;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Grzegorz Iwanek on 30.11.2016.
 * Consist code responsible for reversed geocoding (getting adrress possible to retrieve through passing latitude and longitude)
 * Implements IntentService; Has to be included in manifest file in corresponding activity section;
 * Returns address through use of geocoder class;
 */
public class AddressIntentServicee extends IntentService {

    private static final String LOG_TAG = AddressIntentServicee.class.getSimpleName();
    protected ResultReceiver resultReceiver;

    public AddressIntentServicee() {
        super("EMPTY CONSTRUCTOR");
    }

    public AddressIntentServicee(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //bind and assign receiver from here and activity (through Constants)
        bindResultReceiver(intent);

        //create geocoder instance-> it will handle reversed geocoding operation
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        String errorMessage = "";

        //geocoder's getFromLocation is a key phrase-> returns list of addresses in close proximity to given coordinates
        //possible errors: No location data provided, Invalid latitude or longitude used, No geocoder available, Sorry, no address found
        List<Address> addresses = null;

        try {
            //try to assign allowed number of addresses to a list; have to
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            //thrown in case of service offline
            errorMessage = getString(R.string.service_not_available);
            Log.e(LOG_TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            //thrown in case of wrong given coordinates
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(LOG_TAG, errorMessage + ", " + "Latitude: " + location.getLatitude()
                    + " , " + "Longitude: " + location.getLongitude(), illegalArgumentException);
        }

        //check for case of no address found
        if (addresses == null || addresses.size() == 0) {
            //check if different error has already occur
            if(errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(LOG_TAG, errorMessage);
            }

            //deliver info about failure
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            //address was found
            Address address = addresses.get(0);
            ArrayList<String> addressLines = new ArrayList<>();

            //fetching each of address lines from given address object to the List
            for (int i=0; i<address.getMaxAddressLineIndex(); i++) {
                addressLines.add(address.getAddressLine(i));
            }
            Log.i(LOG_TAG, getString(R.string.address_found));

            //delivering address to the receiver
            String combinedAddress = TextUtils.join(System.getProperty("line.separator"), addressLines);
            deliverResultToReceiver(Constants.SUCCESS_RESULT, combinedAddress);
        }
    }

    private void bindResultReceiver(Intent intent) {
        resultReceiver = intent.getParcelableExtra(Constants.RECEIVER);
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }
}