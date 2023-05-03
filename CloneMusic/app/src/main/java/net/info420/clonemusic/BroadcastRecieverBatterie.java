package net.info420.clonemusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BroadcastRecieverBatterie extends BroadcastReceiver
{

    /*
    *
    * Pour tester la fonctionnalité :
    *
    * Powershell
    * > adb shell
    * > dumpsys battery set ac 0
    * > dumpsys battery set level 5
    *
    */

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
        {
            Toast.makeText(context, R.string.pileFaible, Toast.LENGTH_LONG).show();
            Log.d("PILE", "Pile faible");
        }
    }
}
