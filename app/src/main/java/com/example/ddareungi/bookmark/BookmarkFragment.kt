package com.example.ddareungi.bookmark


import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.ddareungi.R
import com.example.ddareungi.data.Bookmark
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.util.RecyclerItemTouchHelper
import com.example.ddareungi.util.replaceFragmentInActivity
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.internal.it
import kotlinx.android.synthetic.main.bookmark_frag.*
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

    override fun showWeatherView(dustText: String, imageId: Int) {
        dust_text.text = dustText
        weather_image.setImageResource(imageId)
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

    override fun showLoadingIndicator(active: Boolean, hideAll: Boolean) {
        if(hideAll)
            showViews(false, false, false, false, false)
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
        Toast.makeText(context, "데이터를 불러오는데 실패하였습니다", Toast.LENGTH_SHORT).show()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        val deletedBookmarkPosition = viewHolder.adapterPosition

        bookmarkAdapter.removeItem(deletedBookmarkPosition)
        presenter.deleteBookmark(deletedBookmarkPosition)
    }

    override fun showClickedBookmarkInMapFrag(dataRepository: DataRepository, clickedRentalOffice: String) {
//        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav_view).apply {
//            menu.findItem(R.id.map)
//                .setChecked(true)
//        }
//        MapFragment().also {
//            (requireActivity() as AppCompatActivity).replaceFragmentInActivity(it, R.id.container)
//        }

    }

    @SuppressLint("MissingPermission")
    override fun initLocation(locationPermissionGranted: Boolean) {
        var mLocation = Location("initLocation")
        val res = requireContext().resources
        mLocation.latitude = 37.540
        mLocation.longitude = 127.07

        if(locationPermissionGranted) {
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                mLocation = it
            }
        }
        val geocoder = Geocoder(context, Locale.KOREA)
        val addrList = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 1)
        val addr = addrList.first().getAddressLine(0).split(" ")
        presenter.processLocation(addr[2], addr[3], Scanner(res.openRawResource(R.raw.weather)), Scanner(res.openRawResource(R.raw.dust)))
    }

    companion object {
        fun newInstance() = BookmarkFragment()
        }
}
