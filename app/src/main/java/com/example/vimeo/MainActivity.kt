package com.example.vimeo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler
import com.vimeo.networking.Configuration
import com.vimeo.networking.VimeoClient
import cz.msebera.android.httpclient.Header
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var VIMEO_ACCESS_TOKEN : String ="access token"

    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null

    //Release references
    private val playWhenReady = false //If true the player auto play the media
    private val currentWindow = 0
    private val playbackPosition: Long = 0

   // val playBtn : Button?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playBtn = findViewById<Button>(R.id.button2)
        playerView = findViewById(R.id.video_view);
        playBtn.setOnClickListener(this)


        //Build vimeo configuration
        configVimeoClient()
    } //onCreate

    private fun createMediaItem(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player?.setMediaItem(mediaItem)
    }

    private fun initializePlayer() {

        //To play streaming media, you need an ExoPlayer object.
        //SimpleExoPlayer is a convenient, all-purpose implementation of the ExoPlayer interface.
        player = SimpleExoPlayer.Builder(this).build()
        playerView?.player = player
        callVimeoAPIRequest()

        //Supply the state information you saved in releasePlayer to your player during initialization.
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare()
    }

    private fun callVimeoAPIRequest() {
        val videolink = "https://player.vimeo.com/video/" + VIMDEO_ID + "/config"
        val client = AsyncHttpClient()
        val params = RequestParams()
        //params.put("key", "value");
        //    params.put("more", "data");
        client[videolink, params, object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header?>?, res: String?) {
                // called when response HTTP status is "200 OK"
                try {
                    val jsonObject = JSONObject(res)
                    val req = jsonObject.getJSONObject("request")
                    val files = req.getJSONObject("files")
                    val progressive = files.getJSONArray("progressive")
                    val array1 = progressive.getJSONObject(1)
                    val v_url = array1.getString("url")
                    Log.d("URLL ", v_url)
                    createMediaItem(v_url)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header?>?,
                res: String?,
                t: Throwable?,
            ) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        }]
    }

    private fun configVimeoClient() {
        val configBuilder: Configuration.Builder =
            Configuration.Builder(VIMEO_ACCESS_TOKEN) //Pass app access token
                .setCacheDirectory(this.cacheDir)
        VimeoClient.initialize(configBuilder.build())
    }

    override fun onClick(v: View?) {
        player?.playWhenReady = true
    }

    public override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    companion object {
        private const val VIMDEO_ID = "576653652"
    }
}