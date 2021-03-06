/*
 Copyright 2012 Xabier Pena & Urko Guinea
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.vuelosDroid.backEnd.behind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
 
/**
 * Receiver para gestionar las alarmas al encender el smartphone
 * @author Xabi
 *
 */
public class StartUpReceiver extends BroadcastReceiver {
 
	private static final String TAG = "VuelosAndroid";

	Context mContext;
	private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "StartUpReceiver - Broadcast: Se�al de inicio recibida.");
		
		mContext = context;
		Intent serviceIntent = new Intent(context, AlarmaService.class);
		serviceIntent.setAction("com.VuelosDroid.backEnd.behind.AlarmaService");
		context.startService(serviceIntent);
		
    }
 
}
