package com.example.nuber

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.ReferenceQueue
import java.util.ArrayList
import java.util.HashMap


class NUberMapsActivity : SupportMapFragment(),
    OnMapReadyCallback , GoogleMap.OnMarkerClickListener{

    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation : Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuber_maps)


        checkUserLogged()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        mMap.getUiSettings().setZoomControlsEnabled(true)
        mMap.setOnMarkerClickListener(this)
        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                placeMarkerOnMap(currentLatLng)


            }
        }


    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        val titleStr =  "Hello END GAME HYPE; CM dies"//getAddress(location)  // add these two lines
        markerOptions.title(titleStr)
        mMap.addMarker(markerOptions)
    }




    private fun checkUserLogged() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(activity!!, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
////
    prefsEditor.putLong("Latitude", Double.doubleToLongBits(location.getLatitude()))

    double latitude = Double.longBitsToDouble(prefs.getLong("Latitude", 0)
/////


    fun parse(jObject: JSONObject): List<List<HashMap<String, String>>> {
        //Este método PARSEA el JSONObject que retorna del API de Rutas de Google devolviendo
        //una lista del lista de HashMap Strings con el listado de Coordenadas de Lat y Long,
        //con la cual se podrá dibujar pollinas que describan la ruta entre 2 puntos.
        var jRoutes: JSONArray? = null
        var jLegs: JSONArray? = null
        var jSteps: JSONArray? = null

        try {

            jRoutes = jObject.getJSONArray("routes")

            /** Traversing all routes  */
            for (i in 0 until jRoutes!!.length()) {
                jLegs = (jRoutes.get(i) as JSONObject).getJSONArray("legs")
                val path = ArrayList<HashMap<String, String>>()

                /** Traversing all legs  */
                for (j in 0 until jLegs!!.length()) {
                    jSteps = (jLegs.get(j) as JSONObject).getJSONArray("steps")

                    /** Traversing all steps  */
                    for (k in 0 until jSteps!!.length()) {
                        var polyline = ""
                        polyline = ((jSteps.get(k) as JSONObject).get("polyline") as JSONObject).get("points") as String
                        val list = decodePoly(polyline)

                        /** Traversing all points  */
                        for (l in list.indices) {
                            val hm = HashMap<String, String>()
                            hm["lat"] = java.lang.Double.toString(list[l].latitude)
                            hm["lng"] = java.lang.Double.toString(list[l].longitude)
                            path.add(hm)
                        }
                    }
                    Utilidades.routes.add(path)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
        }

        return Utilidades.routes
    }




    private fun decodePoly(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    }
rivate void webServiceObtenerRuta(String latitudInicial, String longitudInicial, String latitudFinal, String longitudFinal) {

    String url="https://maps.googleapis.com/maps/api/directions/json?origin="+latitudInicial+","+longitudInicial
    +"&destination="+latitudFinal+","+longitudFinal;

    jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            //Este método PARSEA el JSONObject que retorna del API de Rutas de Google devolviendo
            //una lista del lista de HashMap Strings con el listado de Coordenadas de Lat y Long,
            //con la cual se podrá dibujar pollinas que describan la ruta entre 2 puntos.
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {

                jRoutes = response.getJSONArray("routes")

                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++){
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>()

                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps")

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                    String polyline = ""
                    polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points")
                    List<LatLng> list = decodePoly(polyline)

                    /** Traversing all points */
                    for(int l=0;l<list.size();l++){
                    HashMap<String, String> hm = new HashMap<String, String>()
                    hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) )
                    hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) )
                    path.add(hm);
                }
                }
                    Utilidades.routes.add(path)
                }
                }
            } catch (JSONException e) {
                e.printStackTrace()
            }catch (Exception e){
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getApplicationContext(), "No se puede conectar "+error.toString(), Toast.LENGTH_LONG).show();
            System.out.println()
            Log.d("ERROR: ", error.toString())
        }
    }
    )

    request.add(jsonObjectRequest)
}

}
