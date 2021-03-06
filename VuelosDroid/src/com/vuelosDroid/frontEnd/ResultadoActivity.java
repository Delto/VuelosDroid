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

package com.vuelosDroid.frontEnd;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.scrapper.DatosGroup;
import com.vuelosDroid.backEnd.scrapper.DatosVuelo;
import com.vuelosDroid.backEnd.scrapper.NoHayMasPaginasDeVuelosException;
import com.vuelosDroid.backEnd.scrapper.NoHayVueloException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity que contiene la lista de resultados de la b�squeda
 * @author Xabi
 *
 */
public class ResultadoActivity extends ResultadosAbstractActivity{

	DatosGroup listaVuelos;
	List<DatosVuelo> datosVuelos;
	Context context;
	ListView miLista;

	Bundle bundle = new Bundle();
	String origen ="";
	String destino = "";
	String dia = "";
	String horario = "";
	String codOrigen = "";
	String codDestino = "";
	static String tipo = "";

	int pag = 0;
	public boolean cargando = false;
	boolean carga = false;
	miAdapter adapter;
	LinearLayout lay;
	Bundle bun;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bun = savedInstanceState;
		debug("ResultadoActivity - onCreate - VuelosAndroidActivity");
		context = this;
		setContentView(R.layout.activity_resultado);
		TextView text = (TextView) findViewById(R.id.text_busqueda_resultado_vuelo);
		bundle = this.getIntent().getExtras();
		Log.w(TAG, "HOLA");

		origen = bundle.getString("origen");
		destino = bundle.getString("destino");
		horario = bundle.getString("horario");
		tipo = bundle.getString("tipo");
		dia = bundle.getString("dia");

		if(!tipo.equals(null)){
			if (tipo.contains("Destino")){
				text.setText(tipo + destino);
			}else if (tipo.contains("rigen")){
				text.setText(tipo + origen);

			}else {
				text.setText("");
			}
		}
		Log.i(TAG, "ResultadoActivity - onCreate - Seguimos vivos...");

		lay = (LinearLayout) findViewById(R.id.layout_progress_resultado);

		//Coger los codigos
		try {
			if(!origen.equals("")){
				codOrigen = origen.substring((origen.indexOf("(")+1), origen.indexOf(")"));
			}
			if(!destino.equals("")){
				codDestino = destino.substring((destino.indexOf("(")+1), destino.indexOf(")"));
			}} catch (StringIndexOutOfBoundsException e) {

			}


		if(dia.equals("Ma�ana")){
			dia = "manana";
		}
		/*		Log.e(TAG, destino);
		Log.e(TAG, horario);
		Log.e(TAG, dia);*/

		//Si viene de busqueda de un vuelo
		if(origen.equals("") && destino.equals("")){
			Log.w(TAG, "ResultadoActivity - onCreate - Viene de un vuelo");
			loadData2(bundle.getString("codigo"), bundle.getString("dia").toLowerCase());
		}
		//Si viene de la busqueda por aeropuertos de origen y destino
		else{
			Log.w(TAG, "Viene de varios");
			//listaVuelos = getInfoVuelos(codOrigen, codDestino, "-1", dia.toLowerCase(), "");
			loadData(codOrigen, codDestino, horario, dia.toLowerCase(), "", pag);
		}

		miLista = (ListView)findViewById(R.id.lista_resultados);

		//Operaciones necesarias para iniciar otra activity desde el listView
		final Intent intent = new Intent(context, VueloResultadoActivity.class);

		miLista.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bundle extras = new Bundle();
				//Log.w(TAG, datosVuelos.get(arg2).getLinkInfoVuelo());
				extras.putString("url",datosVuelos.get(arg2).getLinkInfoVuelo());
				extras.putString("codigo", "");
				extras.putString("dia", dia);
				intent.putExtras(extras);
				if(!tieneRed()){
					Toast toast1 = Toast.makeText(getApplicationContext(), "Necesitas tener red para poder continuar", Toast.LENGTH_SHORT);
					toast1.show();

				}else{
					context.startActivity(intent);
				}	
			}
		});

		miLista.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.i(TAG, "ResultadoActivity - OnScrollListener - OnScroll - onScrollStateChanged "+scrollState);
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Log.i(TAG, "ResultadoActivity - OnScrollListener - OnScroll "+firstVisibleItem + " " + visibleItemCount + " " + totalItemCount);
				if(pag>0){
					/*if (cargando && !carga && (firstVisibleItem+visibleItemCount)==totalItemCount){
						carga = true;
						ponerCargando();
						Log.e(TAG, ""+	firstVisibleItem+visibleItemCount);
					}*/
					Log.d(TAG, "ResultadoActivity - OnScrollListener - OnScroll - pag: " + pag);

					Log.d(TAG, "ResultadoActivity - OnScrollListener - OnScroll - firstVisibleItem/pag: " + firstVisibleItem/pag);
					Log.d(TAG, "ResultadoActivity - OnScrollListener - OnScroll - (totalItemCount/pag)-10): " + ((totalItemCount/pag)-10));

					if (!cargando && (firstVisibleItem/pag)>(totalItemCount/pag)-10 && (firstVisibleItem/pag)>0){
						ponerCargando();
						loadData(codOrigen, codDestino, horario, dia.toLowerCase(), "", pag);
						carga = true;
						cargando = true; 
					}

				}
			}
		});
	}   

	/**
	 * Pone el icono de cargar
	 */
	private void ponerCargando(){
		DatosVuelo dat = new DatosVuelo();
		dat.setAeropuertoOrigen("cargando");
		dat.setAeropuertoDestino("cargando");
		datosVuelos.add(dat);
		listaVuelos.setValues(datosVuelos);
		adapter.notifyDataSetChanged();
	}

	/**
	 * Quita la imagen de cargar
	 * @param listaVuelosH
	 */
	private void quitarCargando(DatosGroup listaVuelosH){
		try{
			if(!datosVuelos.isEmpty()){
				Iterator<DatosVuelo> it = datosVuelos.iterator();
				//it.next();
				DatosVuelo datos = it.next();
				while (it.hasNext()){
					datos = it.next();
					if(datos.getAeropuertoOrigen().equals("cargando")){
						it.remove();
					}

				}
				adapter.notifyDataSetChanged();

				if(listaVuelosH != null){
					listaVuelos.getValues().addAll(listaVuelosH.getValues());
					carga = false;
					datosVuelos = (List<DatosVuelo>) listaVuelos.getValues();
					adapter.notifyDataSetChanged();
				}
			}
		} catch (Exception e){
			Log.e(TAG, "ResultadoActivity - e: " + e.getMessage());
			lay = (LinearLayout) findViewById(R.id.layout_progress_resultado);
			lay.setVisibility(View.GONE);
			LinearLayout layNo = (LinearLayout) findViewById(R.id.layout_resultado_sin_resultado);
			layNo.setVisibility(View.VISIBLE);
		}
	}

	public void onClickActualizar(View v){
		onCreate(bun);
	}

	/**
	 * Handler que gestina la comuncacion con el hilo de busqueda
	 */
	private final Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			//Log.e(TAG, "ResultadoActivity - progressHandler - Dentro del Handler1");
			if (msg.obj != null) {
				if(msg.arg1 == 0){
					Log.i(TAG, "ResultadoActivity - progressHandler - 	Dentro del Handler");
					listaVuelos = (DatosGroup)msg.obj;
					datosVuelos = (List<DatosVuelo>) listaVuelos.getValues();
					//Log.d(TAG, "ResultadoActivity - progressHandler - isEmpty: " + listaVuelos.getValues().isEmpty());
					//progressDialog.dismiss();
					lay.setVisibility(View.GONE);
					Log.d(TAG, "ResultadoActivity - Dentro del Handler - msg.what " + msg.what);
					if (datosVuelos.isEmpty()){
						lay.setVisibility(View.GONE);
						LinearLayout layNo = (LinearLayout) findViewById(R.id.layout_resultado_sin_resultado);
						layNo.setVisibility(View.VISIBLE);
					}
					if(msg.what == 8){
						for (int i = 0; i < datosVuelos.size(); i++	) {
							datosVuelos.get(i).setEstadoVueloDestino("NO");
							tipo = "Cod";
							TextView text = (TextView) findViewById(R.id.text_busqueda_resultado_vuelo);
							text.setText("C�digo: " + datosVuelos.get(i).getNombreVuelo());
						}
					}
					if(msg.what == 9){
						for (int i = 0; i < datosVuelos.size(); i++	) {
							datosVuelos.get(i).setEstadoVueloDestino("NOA");
							tipo = "Cod";
							//TextView text = (TextView) findViewById(R.id.text_busqueda_resultado_vuelo);
							//text.setText("C�digo: " + datosVuelos.get(i).getNombreVuelo());
						}
					}
					adapter = new miAdapter(context, datosVuelos);
					miLista.setAdapter(adapter);
					pag++;
					Log.i(TAG, "ResultadoActivity - progressHandler - Final del handler");

				}else{
					if(!(msg.arg2==9)){
						Log.d(TAG, "ResultadoActivity - Dentro del Handler - Pagina " + msg.arg1);
						DatosGroup listaVuelosH;
						listaVuelosH = (DatosGroup) msg.obj;
						quitarCargando(listaVuelosH);
						Log.d(TAG, "ResultadoActivity - Dentro del Handler - Tama�o - " +listaVuelos.getValues().size());
						Log.d(TAG, "ResultadoActivity - Dentro del Handler - Pagina " +listaVuelos.getValues().isEmpty()+"");
						Log.d(TAG, "ResultadoActivity - Dentro del Handler - Pagina - Final del handler" +listaVuelos.getValues().size());
						//progressDialog.dismiss();
						//lay.setVisibility(View.GONE);
						pag++;
						//adapter.notifyDataSetChanged();
						cargando = false;
					}
				}
			}
			else {
				/*				if(!(msg.arg2==9)){
					//quitarCargando(null);
				}*/
				if(msg.arg2==9){
					quitarCargando(null);
					/*Log.d(TAG, "ResultadoActivity - Dentro del Handler - Tama�o - " +listaVuelos.getValues().size());
					Log.d(TAG, "ResultadoActivity - Dentro del Handler - Pagina " +listaVuelos.getValues().isEmpty()+"");
					Log.d(TAG, "ResultadoActivity - Dentro del Handler - Pagina - Final del handler" +listaVuelos.getValues().size());
					//progressDialog.dismiss();
					//lay.setVisibility(View.GONE);
					//adapter.notifyDataSetChanged();
					 */					cargando = false;
				} else {
					Log.w(TAG, "ResultadoActivity - Dentro del Handler NUll");
					lay = (LinearLayout) findViewById(R.id.layout_progress_resultado);
					lay.setVisibility(View.GONE);
					LinearLayout layNo = (LinearLayout) findViewById(R.id.layout_resultado_sin_resultado);
					layNo.setVisibility(View.VISIBLE);
					if (msg.what == 9){
						TextView text = (TextView) findViewById(R.id.text_resultado_sin);
						text.setText("Fallo al obtener los datos. Prueba a volver a hacer la b�squeda");
					}
					cargando = false;
				}
			}
		}
	};

	/**
	 * Metodo que hace la busqueda en segundo plano
	 * @param codOrigen
	 * @param codDestino
	 * @param horario
	 * @param dia
	 * @param company
	 * @param pTipo
	 */
	private void loadData(final String codOrigen, final String codDestino, final String horario, 
			final String dia, final String company, final int pTipo) {
		//Log.e(TAG, "Dentro del LoadData principio");
		new Thread(new Runnable(){
			public void run() {
				//Log.e(TAG, "Dentro del new Thread");
				Message msg = progressHandler.obtainMessage();
				try {
					Log.i(TAG, "ResultadoActivity - loadData - pag: " + pag);

					msg.obj = getInfoVuelos(codOrigen, codDestino, horario, dia, company, tipo);
					msg.arg1 = pTipo;
					DatosGroup dats = (DatosGroup)msg.obj;
					Log.d(TAG, dats.getValues().isEmpty()+"");
					Log.i(TAG, "ResultadoActivity - loadData - Dentro del LoadData antes de mandar el mensaje");
					//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
					progressHandler.sendMessage(msg);
				} catch (NoHayMasPaginasDeVuelosException e) {
					Log.e(TAG, "ResultadosActivity - loadData - NoHayMasPaginasDeVuelosExteption"+ e.toString());
					msg.arg2=9;
					msg.arg1 = pTipo;
					//DatosGroup dats = (DatosGroup)msg.obj;
					//Log.e(TAG, dats.getValues().isEmpty()+"");
					//Log.e(TAG, "Dentro del LoadData antes de mandar el mensaje");
					//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
					progressHandler.sendMessage(msg);
					/*msg.arg1 = pTipo;
					DatosGroup dats = (DatosGroup)msg.obj;
					Log.d(TAG, dats.getValues().isEmpty()+"");
					Log.i(TAG, "ResultadoActivity - loadData - Dentro del LoadData antes de mandar el mensaje");
					//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
					progressHandler.sendMessage(msg);*/
				} catch (NoHayVueloException e) {
					Log.e(TAG, "ResultadoActivity - loadData2 - noHayVueloException " + e.getMessage());
					msg.obj = null;
					msg.what = 8;
					progressHandler.sendMessage(msg);
				} catch (IOException e){
					Log.e(TAG, "ResultadoActivity - loadData2 - noHayVueloException " + e.getMessage());
					msg.obj = null;
					msg.what = 9;
					progressHandler.sendMessage(msg);
				}


			}}).start();
	}

	private void loadData2(final String pCod, final String pDia) {
		Log.i(TAG, "ResultadoActivity - loadData2 - Dentro del LoadData2 principio");
		new Thread(new Runnable(){
			public void run() {
				Log.i(TAG, "ResultadoActivity - loadData2 - Dentro del new Thread");
				Message msg = progressHandler.obtainMessage();
				try {
					msg.obj = getInfoMasVuelos(pCod, pDia);
					msg.what = 8;
				} catch (NoHayVueloException e) {
					Log.e(TAG, "ResultadoActivity - loadData2 - noHayVueloException " + e.getMessage());
					msg.obj = null;
					msg.what = 8;
				}
				Log.i(TAG, "ResultadoActivity - loadData2 - Dentro del LoadData antes de mandar el mensaje");
				//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
				progressHandler.sendMessage(msg);
			}}).start();
	}


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && tipo.contains("od")) {
			startActivity (new Intent(getApplicationContext(), BusquedaActivity.class));
			finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private static class miAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<DatosVuelo> datosVuelos;

		miAdapter(Context context, 	List<DatosVuelo> datosVuelos) {
			mInflater = LayoutInflater.from(context);
			//listaVuelos = getInfoVuelos("", "BIO", "8", "hoy", "");
			this.datosVuelos = datosVuelos;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			TextView text; 
			//TextView text2;	 
			TextView text3;
			//TextView textHoraLlegada;
			TextView textCod;
			LinearLayout lin;
			LinearLayout linDats;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_resultados, null);
			}
			lin = (LinearLayout) convertView.findViewById(R.id.layout_item_resultados_cargando);
			linDats = (LinearLayout) convertView.findViewById(R.id.layout_item_resultados_datos);


			text = (TextView) convertView.findViewById(R.id.text_lista1);
			text3 = (TextView) convertView.findViewById(R.id.text_lista3);
			textCod = (TextView) convertView.findViewById(R.id.text_item_resultados_codigo);

			Log.d(TAG, "ResultadoActivity - origen: " + datosVuelos.get(position).getAeropuertoOrigen());
			Log.d(TAG, "ResultadoActivity - destino: " +  datosVuelos.get(position).getAeropuertoDestino());
			Log.d(TAG, "ResultadoActivity - estadoo: " +  datosVuelos.get(position).getEstadoVueloOrigen());
			Log.d(TAG, "ResultadoActivity - estadod: " +  datosVuelos.get(position).getEstadoVueloDestino());
			Log.d(TAG, "ResultadoActivity - horao: " +  datosVuelos.get(position).getHoraOrigen());
			Log.d(TAG, "ResultadoActivity - horad: " +  datosVuelos.get(position).getHoraDestino());
			Log.d(TAG, "ResultadoActivity - cod: " +  datosVuelos.get(position).getNombreVuelo());
			Log.d(TAG, "ResultadoActivity - comp: " +  datosVuelos.get(position).getNombreCompany());
			Log.d(TAG, "ResultadoActivity - ter: " +  datosVuelos.get(position).getTerminalOrigen());
			if (tipo == "Cod"){
				if(position == 0){
					datosVuelos.get(position).setAeropuertoDestino("Hoy");
				} else {
					datosVuelos.get(position).setAeropuertoDestino("Ma�ana");
				}
				String or;
				String de;
				try{
					or = datosVuelos.get(position).getNombreCompany().substring(0, 
							datosVuelos.get(position).getNombreCompany().indexOf("(")-1);
					de = datosVuelos.get(position).getTerminalOrigen().substring(0, 
							datosVuelos.get(position).getTerminalOrigen().indexOf("(")-1);
				} catch (StringIndexOutOfBoundsException e){
					or = datosVuelos.get(position).getNombreCompany();
					de = datosVuelos.get(position).getTerminalOrigen();
				}
				text.setText(datosVuelos.get(position).getAeropuertoDestino());
				textCod.setText(or + "  -  " + de);
				text3.setText("Hora de salida:   " + datosVuelos.get(position).getHoraOrigen());

				return convertView;


			} 

			textCod.setText(datosVuelos.get(position).getNombreVuelo() + "  -  " + datosVuelos.get(position).getNombreCompany());
			if(tipo.contains("Destino")){
				text3.setText("Hora de llegada:   " + datosVuelos.get(position).getHoraOrigen());
				text.setText(datosVuelos.get(position).getAeropuertoDestino());

			}
			else{
				text.setText(datosVuelos.get(position).getAeropuertoDestino());

				text3.setText("Hora de salida:   " + datosVuelos.get(position).getHoraOrigen());

			}

			if(datosVuelos.get(position).getAeropuertoOrigen().equals("cargando")){
				Log.w(TAG, "ResultadoActivity - miAdapter - getView - ha entrado en cargando");
				lin.setVisibility(View.VISIBLE);
				linDats.setVisibility(View.GONE);


				//return convertView;
			} else {
				lin.setVisibility(View.GONE);
				linDats.setVisibility(View.VISIBLE);
			}

			return convertView;	 
		}


		public int getCount() {
			return datosVuelos.size();
		}


		public Object getItem(int position) {
			return position;
		}


		public long getItemId(int position) {
			return position;
		}

		/*		public void notifyDataSetChanged(boolean pCargando, int pPag){
			cargando = pCargando;
			pag = pPag;
			super.notifyDataSetChanged();

		}*/

	}
}
