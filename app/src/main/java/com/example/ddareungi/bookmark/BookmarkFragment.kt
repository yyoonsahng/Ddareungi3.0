package com.example.ddareungi.bookmark


import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.ddareungi.R
import com.example.ddareungi.data.Bookmark
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource
import com.example.ddareungi.map.MapFragment
import com.example.ddareungi.map.MapPresenter
import com.example.ddareungi.util.RecyclerItemTouchHelper
import com.example.ddareungi.util.checkLocationPermission
import com.example.ddareungi.util.replaceFragmentInActivity
import com.google.android.gms.location.LocationServices
import java.util.*

class BookmarkFragment : Fragment(), BookmarkContract.View, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    override lateinit var presenter: BookmarkContract.Presenter

    lateinit var progressBar: ProgressBar
    lateinit var bookmarkListView: RecyclerView
    lateinit var weatherView: LinearLayout
    lateinit var noNetworkView: LinearLayout
    lateinit var noBookmarkView: LinearLayout
    lateinit var networkRefreshBtn: TextView
    lateinit var refreshFab: FloatingActionButton

    private lateinit var bookmarkAdapter: BookmarkAdapter

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.bookmark_frag, container, false)
        with(root) {
            progressBar = findViewById(R.id.progress_circular)
            weatherView = findViewById(R.id.weatherLL)
            bookmarkListView = findViewById(R.id.bookmark)
            noNetworkView = findViewById(R.id.no_network)
            noBookmarkView = findViewById(R.id.no_bookmark)

            bookmarkAdapter = BookmarkAdapter(ArrayList())
            val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            bookmarkListView.layoutManager = layoutManager
            bookmarkListView.adapter = bookmarkAdapter
            val dividerItemDecoration1 = DividerItemDecoration(requireContext(), layoutManager.orientation)
            bookmarkListView.addItemDecoration(dividerItemDecoration1)

            bookmarkAdapter.itemClickListener = object : BookmarkAdapter.OnItemClickListener {
                override fun onItemClick(holder: BookmarkAdapter.ViewHolder, view: View, data: Bookmark, position: Int) {
                    presenter.openMapFrag(data.rentalOffice)
                }

            }

            RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this@BookmarkFragment).also {
                ItemTouchHelper(it).attachToRecyclerView(bookmarkListView)
            }

            networkRefreshBtn = (findViewById<TextView>(R.id.network_refresh_button)).also {
                it.setOnClickListener { presenter.loadData() }
            }

            refreshFab = (findViewById<FloatingActionButton>(R.id.bookmark_refresh_fab)).also {
                it.setOnClickListener { presenter.loadData() }
            }
        }
        return root
    }

    override fun showWeatherView(neighborhoodText: String, dustText: String, imageId: Int) {

        if(isAdded) {
            with(requireActivity()) {
                findViewById<TextView>(R.id.neighborhood_text).text = neighborhoodText
                findViewById<TextView>(R.id.dust_text).text = dustText
                findViewById<ImageView>(R.id.weather_image).setImageResource(imageId)

            }
        }
    }

    override fun showBookmarkedList(bookmarks: ArrayList<Bookmark>) {
        bookmarkAdapter.items = bookmarks
        bookmarkAdapter.notifyDataSetChanged()
        showViews(true, true, false, false, true)
    }

    override fun showNoBookmark(bookmarks: ArrayList<Bookmark>) {
        bookmarkAdapter.items = bookmarks
        bookmarkAdapter.notifyDataSetChanged()
        showViews(false, true, false, true, true)
    }

    override fun showCheckNetwork() {
        showViews(false, false, true, false, false)
    }

    override fun showLoadingByBikeStatus(active: Boolean, hideBike: Boolean) {
        if(hideBike)
            showViews(false, true, false, true, true)
        else
            showViews(true, true, false, false, true)

        if(active)
            progressBar.visibility = View.VISIBLE
        else
            progressBar.visibility = View.GONE
    }

    override fun showLoadingIndicator(active: Boolean, hideAll: Boolean) {
        if(hideAll)
            showViews(false, false, false, false, false)
        else
            showViews(true, true, false, false, true)

        if(active)
            progressBar.visibility = View.VISIBLE
        else
            progressBar.visibility = View.GONE
    }


    private fun showViews(showBikeList: Boolean, showWeatherView: Boolean, showNetworkView: Boolean, showNoBookmarkView: Boolean,
                          showRefreshFab: Boolean) {
        bookmarkListView.visibility = if(showBikeList) View.VISIBLE else View.GONE
        weatherView.visibility = if(showWeatherView) View.VISIBLE else View.GONE
        noNetworkView.visibility = if(showNetworkView) View.VISIBLE else View.GONE
        noBookmarkView.visibility = if(showNoBookmarkView) View.VISIBLE else View.GONE
        if(showRefreshFab) refreshFab.show() else refreshFab.hide()
    }

    override fun showLoadDataError() {
        if(context!=null)
             Toast.makeText(context, "데이터를 불러오는데 실패하였습니다", Toast.LENGTH_SHORT).show()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        val deletedBookmarkPosition = viewHolder.adapterPosition

        bookmarkAdapter.removeItem(deletedBookmarkPosition)
        presenter.deleteBookmark(deletedBookmarkPosition)
    }

    override fun showClickedBookmarkInMapFrag(dataRepository: DataRepository, clickedRentalOffice: String) {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav_view).apply {
            menu.findItem(R.id.map).isChecked = true
        }
        val mapFragment = MapFragment().also {
            (requireActivity() as AppCompatActivity).replaceFragmentInActivity(it, R.id.fragment_container, "")
        }
        val mapPresenter = MapPresenter(dataRepository, mapFragment, true, clickedRentalOffice)
    }

    private fun setUpLocation(mLocation:Location?,callback: DataSource.LoadDataCallback) {
        val lm =
            context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = object : LocationListener {
            var isGpsProvider = false
            var isNetworkProvider = false
            var isLoaded = false

            var isGpsProviderEnabled = true
            var isNetworkProviderEnabled = true

            override fun onLocationChanged(location: Location) {
                if (location.provider == LocationManager.GPS_PROVIDER) isGpsProvider = true
                if (location.provider == LocationManager.NETWORK_PROVIDER) isNetworkProvider =
                    true
                if ((!(isGpsProvider && isNetworkProvider)) && (!isLoaded)) {
                    isLoaded = true
                    mLocation!!.latitude = location.latitude
                    mLocation!!.longitude = location.longitude
                    lm.removeUpdates(this)
                    callback.onDataLoaded()
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) isGpsProviderEnabled = false
                if (provider == LocationManager.NETWORK_PROVIDER) isNetworkProviderEnabled =
                    false
                if (!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                    lm.removeUpdates(this)
                    callback.onNetworkNotAvailable()
                }

            }


        }
        if (checkLocationPermission()) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000L, 10f, locationListener)
            lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 60000L, 10f, locationListener
            )
        }

    }
    override fun initLocation(dataRepository: DataRepository) {
        var mLocation = Location("initLocation")
        val res = requireContext().resources
        mLocation.latitude = 37.540
        mLocation.longitude = 127.07

        if(checkLocationPermission()) {
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)

                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    if(it != null) {
                        mLocation = it
                        try{
                            val geocoder = Geocoder(context, Locale.KOREA)
                            val addrList = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 5)
                            var address: List<String> = listOf("대한민국", "서울특별시", "광진구", "자양동")
                            for(addr in addrList) {
                                val splitedArr = addr.getAddressLine(0).split(" ")
                                if(splitedArr[2].endsWith("구") && splitedArr[2].endsWith("동")){
                                    address = splitedArr
                                    break
                                }
                            }
                            presenter.processLocation(address[2], address[3], Scanner(res.openRawResource(R.raw.weather)), Scanner(res.openRawResource(R.raw.dust)))
                        }
                        catch (e:Exception){ }
                        dataRepository.refreshWeather(object: DataSource.LoadDataCallback{
                            override fun onDataLoaded() {
                                presenter.setIsWeather()
                                if(presenter.getIsAll()&&presenter.getIsWeather()&&presenter.getIsBike()) {
                                    showLoadingIndicator(false, false)
                                    presenter.setWeatherViews()
                                    presenter.loadBookmarks()
                                }
                            }

                            override fun onNetworkNotAvailable() {
                                showLoadingIndicator(false, false)
                                showCheckNetwork()
                                showLoadDataError()
                            }
                        } )
                    }
                    else
                        setUpLocation(mLocation,object:DataSource.LoadDataCallback{
                            override fun onDataLoaded() {
                                try{
                                    val geocoder = Geocoder(context, Locale.KOREA)
                                    val addrList = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 5)
                                    var address: List<String> = listOf("대한민국", "서울특별시", "광진구", "자양동")
                                    for(addr in addrList) {
                                       val splitedArr = addr.getAddressLine(0).split(" ")
                                        if(splitedArr[2].endsWith("구") && splitedArr[2].endsWith("동")){
                                            address = splitedArr
                                            break
                                        }
                                    }
                                    presenter.processLocation(address[2], address[3], Scanner(res.openRawResource(R.raw.weather)), Scanner(res.openRawResource(R.raw.dust)))
                                }
                                catch (e:Exception){ }


                                dataRepository.refreshWeather(object: DataSource.LoadDataCallback{
                                    override fun onDataLoaded() {
                                        presenter.setIsWeather()
                                        if(presenter.getIsAll()&&presenter.getIsWeather()&&presenter.getIsBike()) {
                                            showLoadingIndicator(false, false)
                                            presenter.setWeatherViews()
                                            presenter.loadBookmarks()
                                        }
                                    }

                                    override fun onNetworkNotAvailable() {
                                        showLoadingIndicator(false, false)
                                        showCheckNetwork()
                                        showLoadDataError()
                                    }
                                } )
                            }
                            override fun onNetworkNotAvailable() {
                                showLoadingIndicator(false, false)
                                showCheckNetwork()
                                showLoadDataError()
                            }
                        })

            }


        }

    }

    companion object {
        fun newInstance() = BookmarkFragment()
        }
}
