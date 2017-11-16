package org.dmfs.bug68770560demo;

import android.Manifest;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.dmfs.android.calendarpal.views.Events;
import org.dmfs.android.contentpal.ClosableIterator;
import org.dmfs.android.contentpal.RowSet;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.rowsets.AllRows;


public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_CALENDAR }, 0);
            Toast.makeText(this, "Please grant permission and restart", Toast.LENGTH_LONG).show();
        }
        else
        {
            ContentProviderClient cpc = getContentResolver().acquireContentProviderClient(CalendarContract.AUTHORITY);
            try
            {
                // get any event
                RowSet<CalendarContract.Events> allEvents = new AllRows<>(new Events(cpc, CalendarContract.Events._ID));
                ClosableIterator<RowSnapshot<CalendarContract.Events>> eventIterator = allEvents.iterator();

                if (eventIterator.hasNext())
                {
                    // get the id of this event.
                    long eventId = Long.parseLong(eventIterator.next().values().charData(CalendarContract.Events._ID).value().toString());

                    // ------------------------------- Bug demo start ---------------------------------------------

                    // start the ACTION_EDIT intent for the selected event
                    // Expected result:
                    //   * Editor with all event details shows up (works as expected with the app "aCalendar")
                    // Actual result:
                    //   * "Google Calendar" starts the editor for a new event
                    //   * AOSP calendar opens the event but start and end dates & time are incorrect.

                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId));
                    startActivityForResult(intent, 0);

                    // ------------------------------- Bug demo end -----------------------------------------------
                }
                else
                {
                    Toast.makeText(this, "No event found, please create an event to see the demo.", Toast.LENGTH_LONG).show();
                }

                eventIterator.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (cpc != null)
            {
                cpc.release();
            }
        }
        finish();
    }
}
